package com.deol.deolspring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistDetailsDto {
    private ArtistBasicInfoDto artist; // 아티스트 기본 정보
    private AlbumSummaryDto latestAlbum; // 가장 최근 발매 앨범
    private List<AlbumSummaryDto> allAlbums; // 업로드된 전체 앨범
    private List<TrackPopularDto> topTracks; // 인기 곡 리스트
//    private LiveStream liveStream; // 라이브 방송 정보
}

