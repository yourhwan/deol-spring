// src/main/java/com/deol/deolspring/repository/AlbumRepository.java
package com.deol.deolspring.repository;

import com.deol.deolspring.dto.AlbumSummaryDto;
import com.deol.deolspring.entity.Album;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {

    @Query("SELECT new com.deol.deolspring.dto.AlbumSummaryDto(a.albumId, a.albumTitle, a.coverImage, size(a.tracks), a.releaseDate) " +
            "FROM Album a WHERE a.albumArtistId = :artistId ORDER BY a.releaseDate DESC")
    List<AlbumSummaryDto> findByAlbumArtistIdOrderByReleaseDateDesc(@Param("artistId") Integer artistId);

    @Query("SELECT new com.deol.deolspring.dto.AlbumSummaryDto(a.albumId, a.albumTitle, a.coverImage, size(a.tracks), a.releaseDate) " +
            "FROM Album a WHERE a.albumArtistId = :artistId")
    List<AlbumSummaryDto> findByAlbumArtistId(@Param("artistId") Integer artistId);

    List<Album> findByAlbumArtistIdAndAlbumIdNot(Integer artistId, Integer excludeAlbumId);

    List<Album> findAllByAlbumArtistId(Integer artistId);

    List<Album> findByAlbumTitleContainingIgnoreCase(String keyword);

    // ✅ 인기 앨범: 트랙 stream_count 합계 기준 내림차순 (+ 발매일 동률시 최신 우선)
    @Query("""
           SELECT a
           FROM Album a
           LEFT JOIN a.tracks t
           GROUP BY a
           ORDER BY COALESCE(SUM(t.streamCount), 0) DESC,
                    a.releaseDate DESC
           """)
    List<Album> findPopularAlbumsOrderByStreams(Pageable pageable);
}
