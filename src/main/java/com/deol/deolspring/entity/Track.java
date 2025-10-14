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
@Table(name = "tbl_track")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Column(name = "album_id", nullable = false) // 앨범 ID
    private Integer albumId;

    @Column(name = "track_artist_id", nullable = false) // 아티스트 ID (member_artist_seq)
    private Integer trackArtistId;

    @Column(name = "track_title", nullable = false)
    private String trackTitle; // 음원 파일 제목

    @Column(name = "track_file", nullable = false)
    private String trackFile; // 음원 파일 S3 URL

    @Column(name = "track_lyrics") // 가사
    private String trackLyrics;

    @Column(name = "track_genre") // 장르
    private String trackGenre;

    @Column(name = "track_duration", nullable = false)
    private String trackDuration; // 재생 시간 (초 단위)

    // ★ 포인트: JPA가 null을 넣지 않도록 엔티티 차원에서 기본값 강제
    @Column(name = "stream_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer streamCount = 0; // 스트림 카운트 기본값 0

    @PrePersist
    void prePersist() {
        if (streamCount == null) streamCount = 0;
    }

    @ManyToOne // 앨범과의 관계 설정
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

}
