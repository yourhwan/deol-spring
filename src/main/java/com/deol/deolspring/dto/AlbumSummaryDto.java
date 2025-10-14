package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Album;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumSummaryDto {
    private Integer albumId;
    private String albumTitle;
    private String coverImage;
    private Integer trackCount;  // 트랙 수
    private LocalDate releaseDate;  // 발매일 (LocalDate로 처리)

    // 엔티티에서 DTO로 변환
    public static AlbumSummaryDto fromEntity(Album album) {
        return AlbumSummaryDto.builder()
                .albumId(album.getAlbumId())
                .albumTitle(album.getAlbumTitle())
                .coverImage(album.getCoverImage())
                .trackCount(album.getTracks().size())  // 앨범에 포함된 트랙 개수를 가져옴
                .releaseDate(album.getReleaseDate())  // 발매일 (LocalDate)
                .build();
    }
}
