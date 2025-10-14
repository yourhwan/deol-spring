package com.deol.deolspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackSearchDto {

    @Schema(description = "트랙 시퀀스 번호", example = "789")
    private Integer trackId;

    @Schema(description = "트랙이 속한 앨범 시퀀스 번호", example = "456")
    private Integer albumId;

    @Schema(description = "트랙을 만든 아티스트 시퀀스 번호", example = "123")
    private Integer trackArtistId;

    @Schema(description = "트랙 제목", example = "DNA")
    private String trackTitle;

    @Schema(description = "트랙 파일(음원) URL", example = "https://example-bucket.s3.amazonaws.com/track.mp3")
    private String trackFile;
}
