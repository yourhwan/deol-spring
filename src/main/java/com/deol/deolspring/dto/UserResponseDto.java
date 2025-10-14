package com.deol.deolspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserResponseDto {

    private String memberId;
    private String memberPassword;
    private String memberName;
    private String memberNickname;
    private String memberArtistName;
    private Integer memberSeq;
    private Integer memberArtistSeq;
    private String memberEmail;
    private String memberGender;
    @JsonProperty("isArtist") // 명시적으로 isArtist로 반환
    private boolean isArtist; // 아티스트 여부

    // 맞춤 생성자 추가
    public UserResponseDto(String memberName, String memberNickname,
                           String memberArtistName, Integer memberSeq, Integer memberArtistSeq,
                           boolean isArtist) {
        this.memberName = memberName;
        this.memberNickname = memberNickname;
        this.memberArtistName = memberArtistName;
        this.memberSeq = memberSeq;
        this.memberArtistSeq = memberArtistSeq;
        this.isArtist = isArtist;
    }
}

