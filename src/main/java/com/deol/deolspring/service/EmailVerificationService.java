package com.deol.deolspring.service;

import com.deol.deolspring.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MemberRepository memberRepository;
    private final JavaMailSender mailSender;
    private final RedisService redisService;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // 이메일 중복 확인
    public boolean isEmailDuplicated(String memberEmail) {
        return memberRepository.findByMemberEmail(memberEmail).isPresent();
    }

    // 인증 코드 생성
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (random.nextBoolean()) {
                code.append((char) (random.nextInt(26) + 'A')); // 대문자
            } else {
                code.append(random.nextInt(10)); // 숫자
            }
        }
        return code.toString();
    }

    // HTML 이메일 전송
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // HTML 형식으로 전송
        helper.setFrom(senderEmail);
        mailSender.send(message);
    }

    // 인증 코드 발송
    @Transactional
    public void sendVerificationCode(String memberEmail) {
        if (isEmailDuplicated(memberEmail)) {
            throw new IllegalArgumentException("이미 회원가입이 완료된 이메일입니다.");
        }

        String code = generateVerificationCode();
        redisService.setDataExpire("VERIFY_CODE_" + memberEmail, code, 3 * 60); // 3분 동안 유효

        // 로그 추가
        System.out.println("Saving verification code to Redis: VERIFY_CODE_" + memberEmail + " = " + code);

        // HTML 콘텐츠 작성
        String htmlContent = "<html><body>" +
                "<h2>이메일 인증 코드</h2>" +
                "<p>안녕하세요,</p>" +
                "<p>아래의 인증 코드를 입력해 주세요:</p>" +
                "<h3 style='color: #007bff;'>" + code + "</h3>" +
                "<p>이 코드는 3분 동안 유효합니다.</p>" +
                "<p>감사합니다.</p>" +
                "<p>Deol팀 드림</p>" +
                "</body></html>";

        try {
            sendHtmlEmail(memberEmail, "이메일 인증 코드", htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    // 인증 코드 검증
    public boolean verifyCode(String memberEmail, String code) {
        String savedCode = redisService.getData("VERIFY_CODE_" + memberEmail);

        if (savedCode == null) {
            // 만약 코드가 Redis에 없다면 만료된 코드
            throw new IllegalStateException("인증 코드가 만료되었습니다. 다시 시도해 주세요.");
        }

        if (!savedCode.equals(code)) {
            // 인증 코드가 일치하지 않음
            throw new IllegalArgumentException("올바르지 않은 인증 코드입니다.");
        }

        // 인증 코드가 일치할 경우
        redisService.deleteData("VERIFY_CODE_" + memberEmail);
        return true;
    }
//    public boolean verifyCode(String memberEmail, String code) {
//        return redisService.checkData("VERIFY_CODE_" + memberEmail, code);
//    }
}
