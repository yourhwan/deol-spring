package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistBasicInfoDto {
    private Integer memberArtistSeq;
    private String memberArtistName;
    private String profileImageUrl;

    // Member 엔티티로부터 변환
    public static ArtistBasicInfoDto fromEntity(Member member) {
        return ArtistBasicInfoDto.builder()
                .memberArtistSeq(member.getMemberArtistSeq())
                .memberArtistName(member.getMemberArtistName())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
