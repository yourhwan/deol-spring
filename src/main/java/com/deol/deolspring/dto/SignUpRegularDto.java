package com.deol.deolspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRegularDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Schema(description = "사용자 아이디", example = "test02", required = true)
    private String memberId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Schema(description = "사용자 비밀번호", example = "qwer1234$", required = true)
    private String memberPassword;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Schema(description = "사용자 이름", example = "테스트", required = true)
    private String memberName;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Schema(description = "유저 닉네임", example = "테스트 유저", required = true)
    private String memberNickname; // 일반유저

    @NotBlank(message = "생년월일은 필수 입력 항목입니다.")
    @Schema(description = "생년월일", example = "20001010", required = true)
    private String memberBirthdate;

    @NotBlank(message = "성별은 필수 선택 항목입니다.")
    @Schema(description = "성별", example = "여자", required = true)
    private String memberGender;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Schema(description = "이메일", example = "test02@test.com", required = true)
    @Email(message = "유효한 이메일 주소를 입력해 주세요.")
    private String memberEmail;

    private Integer memberSeq;

    private String role; // 예: "Role_USER", "Role_ARTIST", "Role_ADMIN"


}
