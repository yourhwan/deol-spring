package com.deol.deolspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AlbumSearchDto {

    @Schema(description = "앨범 시퀀스 번호", example = "456")
    private Integer albumId;

    @Schema(description = "앨범을 만든 아티스트 시퀀스 번호", example = "123")
    private Integer albumArtistId;

    @Schema(description = "앨범 제목", example = "Love Yourself")
    private String albumTitle;

    @Schema(description = "앨범 커버 이미지 URL", example = "https://example-bucket.s3.amazonaws.com/cover.png")
    private String coverImage;

    @Schema(description = "발매일", example = "2021-08-15")
    private LocalDate releaseDate;

    @Schema(description = "앨범 장르", example = "K-Pop")
    private String albumGenre;
}
