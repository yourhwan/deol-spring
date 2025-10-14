package com.deol.deolspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackChartDto {
    private Integer trackId;
    private String  trackTitle;
    private Integer artistId;
    private String  artistName;
    private Integer albumId;
    private String  albumTitle;
    private String  coverImage;   // 앨범 커버
    private Long    playCount24h; // 24시간 재생수
    private Integer rank;         // 1..100
}
