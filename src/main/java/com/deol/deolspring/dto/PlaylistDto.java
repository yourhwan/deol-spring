package com.deol.deolspring.dto;

import lombok.Data;

@Data
public class PlaylistDto {
    private Integer playlistId;
    private String playlistName;
    private String playlistDescription;
//    private String playlistCover;
//    private List<TrackDetailDto> tracks;  // 플레이리스트에 포함된 트랙들
//    private Integer memberSeq;  // 추가: 해당 플레이리스트의 소유자 (유저의 memberSeq)

    public PlaylistDto(Integer playlistId, String playlistName, String playlistDescription) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistDescription = playlistDescription;
//        this.playlistCover = playlistCover;
//        this.tracks = tracks;
//        this.memberSeq = memberSeq;
    }
}

