package com.deol.deolspring.dto;

import lombok.Data;

@Data
public class PlaylistLoadDto {
    private Integer playlistId;
    private String playlistName;
    private String playlistDescription;
    private String playlistCover; // 커버 이미지 URL

    // 생성자
    public PlaylistLoadDto(Integer playlistId, String playlistName, String playlistDescription, String playlistCover) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistDescription = playlistDescription;
        this.playlistCover = playlistCover;
    }
}
