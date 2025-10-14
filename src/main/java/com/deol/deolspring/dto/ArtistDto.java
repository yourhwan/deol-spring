package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArtistDto {

    @Schema(description = "아티스트 이름", example = "BTS")
    private String memberArtistName;

    @Schema(description = "아티스트 시퀀스 번호", example = "123")
    private Integer memberArtistSeq;

    @Schema(description = "아티스트 프로필 이미지 URL", example = "https://...")
    private String profileImageUrl; // ✅ 추가

    public static ArtistDto fromEntity(Member member) {
        return new ArtistDto(
                member.getMemberArtistName(),
                member.getMemberArtistSeq(),
                member.getProfileImageUrl()
        );
    }
}
