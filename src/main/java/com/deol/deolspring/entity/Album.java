package com.deol.deolspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.util.List;
@DynamicInsert
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_album")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id", nullable = false)
    private Integer albumId;

    @Column(name = "album_artist_id", nullable = false) // 아티스트 ID (member_artist_seq)
    private Integer albumArtistId;

    @Column(name = "album_title", nullable = false)
    private String albumTitle;

    @Column(name = "cover_image", nullable = false)
    private String coverImage; // 앨범 커버 이미지 S3 URL

    @Column(name = "release_date", columnDefinition = "DATE", nullable = false) // 날짜 형식으로 변경
    private LocalDate releaseDate;

    @Column(name = "album_genre", nullable = false) // 앨범 장르 추가
    private String albumGenre;

    @Column(name = "album_description") // 앨범 설명 추가
    private String albumDescription;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL) // 음원과의 관계 설정
    private List<Track> tracks; // 앨범에 포함된 음원
}
