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
@Table(name = "tbl_current_playlist")
public class CurrentPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "current_playlist_id", nullable = false)
    private Integer currentPlaylistId; // 현재 재생목록 ID

    @Column (name = "member_seq")
    private Integer memberSeq; // 현재 재생목록을 사용하는 유저

}
