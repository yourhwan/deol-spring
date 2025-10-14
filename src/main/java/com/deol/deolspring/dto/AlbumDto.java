package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Album;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlbumDto {
    private Integer albumId;
    private String albumTitle;
    private String albumGenre;
    private String releaseDate;
    private String albumDescription;
    private String coverImage; // 추가: 앨범 커버 이미지 URL

    // Album 엔티티를 AlbumDto로 변환하는 정적 메서드
    public static AlbumDto fromEntity(Album album) {
        return new AlbumDto(
                album.getAlbumId(),
                album.getAlbumTitle(),
                album.getAlbumGenre(),
                album.getReleaseDate().toString(), // LocalDate를 문자열로 변환
                album.getAlbumDescription(),
                album.getCoverImage() // 앨범 커버 이미지 추가
        );
    }
}
