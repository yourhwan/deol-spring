package com.deol.deolspring.dto;

import lombok.Data;

@Data
public class MemberDto {

    // 기능별 Dto를 생성하기 전 테스트용 Dto 입니다. 추후 수정 필요함

    private String memberId;
    private String memberPassword;
    private String memberName;
    private String memberNickname;
    private String memberArtistName;
    private String memberEmail;
    private String memberGender;
    private String memberBirthdate;
    private Integer memberSeq;
    private Integer memberArtistSeq;
    private String role;
    private String newPassword;

}
