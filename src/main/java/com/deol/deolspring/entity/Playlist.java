package com.deol.deolspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_playlist")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id", nullable = false)
    private Integer playlistId; // 플레이리스트 ID

    @Column (name = "member_seq")
    private Integer memberSeq; // 플레이리스트를 만든 유저

    @Column(name = "playlist_name")
    private String playlistName; // 플레이리스트 이름

    @Column(name = "playlist_description")
    private String playlistDescription; // 플레이리스트 설명

    @Column(name = "playlist_cover")
    private String playlistCover; // 플레이리스트 대표 이미지

}
