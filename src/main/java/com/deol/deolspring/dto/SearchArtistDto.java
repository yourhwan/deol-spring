// src/main/java/com/deol/deolspring/dto/SearchArtistDto.java
package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchArtistDto {

    @Schema(description = "아티스트 시퀀스 번호 (Member.memberArtistSeq)", example = "123")
    private Integer artistSeq;

    @Schema(description = "아티스트 이름 (Member.memberArtistName)", example = "BTS")
    private String artistName;

    @Schema(description = "아티스트 프로필 이미지 URL", example = "https://example-bucket.s3.amazonaws.com/profile.png")
    private String profileImageUrl;

    public static SearchArtistDto fromMember(Member member) {
        return new SearchArtistDto(
                member.getMemberArtistSeq(),       // ← 여기서 memberArtistSeq
                member.getMemberArtistName(),
                member.getProfileImageUrl()
        );
    }
}
