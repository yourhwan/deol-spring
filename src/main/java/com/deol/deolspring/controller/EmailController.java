package com.deol.deolspring.controller;

import com.deol.deolspring.service.EmailVerificationService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
@CrossOrigin(origins = "http://localhost:3000")
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam @Email String memberEmail) {
        try {
            emailVerificationService.sendVerificationCode(memberEmail);
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PostMapping("/verify")
//    public ResponseEntity<String> verifyCode(@RequestParam @Email String memberEmail, @RequestParam String code) {
//        boolean isValid = emailVerificationService.verifyCode(memberEmail, code);
//        System.out.println(memberEmail);
//        System.out.println(code);
//        return isValid ? ResponseEntity.ok("인증 코드가 확인되었습니다.") : ResponseEntity.badRequest().body("인증 코드가 유효하지 않습니다.");
//    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestParam @Email String memberEmail, @RequestParam String code) {
        try {
            boolean isValid = emailVerificationService.verifyCode(memberEmail, code);
            return ResponseEntity.ok("인증 코드가 확인되었습니다.");
        } catch (IllegalStateException e) {
            // 인증 코드 만료 처리
            return ResponseEntity.status(HttpStatus.GONE).body("인증 코드가 만료되었습니다. 다시 시도해 주세요.");
        } catch (IllegalArgumentException e) {
            // 인증 코드 불일치 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 일치하지 않습니다.");
        }
    }

}
