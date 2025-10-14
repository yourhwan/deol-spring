package com.deol.deolspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Schema(description = "사용자 아이디", example = "test01", required = true)
    private String memberId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Schema(description = "사용자 비밀번호", example = "qwer1234$", required = true)
    private String memberPassword;
}
