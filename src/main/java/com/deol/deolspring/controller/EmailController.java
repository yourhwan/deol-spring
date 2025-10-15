package com.deol.deolspring.controller;

import com.deol.deolspring.service.EmailVerificationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
// @CrossOrigin(origins = "http://localhost:3000") // ❌ 글로벌 CORS에서 처리하므로 제거 권장
@Validated // ✅ @RequestParam 유효성 검사(@Email 등) 활성화
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/check-email")
    public ResponseEntity<String> checkEmail(
            @RequestParam("memberEmail") @NotBlank @Email String memberEmail) {

        // ✅ 공백/대소문자 정규화
        final String email = memberEmail.trim().toLowerCase(Locale.ROOT);

        try {
            emailVerificationService.sendVerificationCode(email);
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
            // 비동기 전송이면: return ResponseEntity.accepted().body("인증 코드 발송 처리 중입니다.");
        } catch (IllegalArgumentException e) {
            // 예: 이미 사용 중인 이메일 등 비즈니스 룰 위반
            return ResponseEntity.badRequest().body("요청을 처리할 수 없습니다.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(
            @RequestParam("memberEmail") @NotBlank @Email String memberEmail,
            @RequestParam("code") @NotBlank String code) {

        // ✅ 공백/대소문자 정규화
        final String email = memberEmail.trim().toLowerCase(Locale.ROOT);
        final String trimmedCode = code.trim();

        try {
            boolean isValid = emailVerificationService.verifyCode(email, trimmedCode);
            if (isValid) {
                return ResponseEntity.ok("인증 코드가 확인되었습니다.");
            }
            // 일반 불일치
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 일치하지 않습니다.");
        } catch (IllegalStateException e) {
            // 만료
            return ResponseEntity.status(HttpStatus.GONE).body("인증 코드가 만료되었습니다. 다시 시도해 주세요.");
        } catch (IllegalArgumentException e) {
            // 기타 잘못된 요청
            return ResponseEntity.badRequest().body("요청을 처리할 수 없습니다.");
        }
    }

}
