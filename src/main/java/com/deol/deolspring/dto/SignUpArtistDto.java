package com.deol.deolspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpArtistDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Schema(description = "사용자 아이디", example = "test01", required = true)
    private String memberId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Schema(description = "사용자 비밀번호", example = "qwer1234$", required = true)
    private String memberPassword;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Schema(description = "사용자 이름", example = "테스트", required = true)
    private String memberName;

    @NotBlank(message = "아티스트 이름은 필수 입력 항목입니다.")
    @Schema(description = "아티스트 이름", example = "테스트", required = true)
    private String artistName;

    @NotBlank(message = "생년월일은 필수 입력 항목입니다.")
    @Schema(description = "생년월일", example = "19990909", required = true)
    private String memberBirthdate;

    @NotBlank(message = "성별은 필수 입력 항목입니다.")
    @Schema(description = "성별", example = "남자", required = true)
    private String memberGender;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Schema(description = "이메일", example = "test01@test.com", required = true)
    @Email(message = "유효한 이메일 주소를 입력해 주세요.")
    private String memberEmail;

    private Integer memberSeq;

    private Integer memberArtistSeq;

    private String role; // 예: "ROLE_USER", "ROLE_ARTIST", "ROLE_ADMIN"
}
