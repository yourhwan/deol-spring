package com.deol.deolspring.repository;

import com.deol.deolspring.entity.TrackPlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TrackPlayRepository extends JpaRepository<TrackPlay, Long> {

    // 최근 24시간 트랙별 집계
    @Query(value = """
        SELECT tp.track_id AS trackId, COUNT(*) AS cnt
        FROM tbl_track_play tp
        WHERE tp.played_at >= :since
        GROUP BY tp.track_id
        ORDER BY cnt DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> topTracksSince(@Param("since") LocalDateTime since,
                                  @Param("limit") int limit);

    // ★ 신곡 TOP: (발매일 >= minReleaseDate) + (played_at >= since)
    @Query(value = """
        SELECT tp.track_id AS trackId, COUNT(*) AS cnt
        FROM tbl_track_play tp
        JOIN tbl_track  t ON t.track_id  = tp.track_id
        JOIN tbl_album  a ON a.album_id  = t.album_id
        WHERE a.release_date >= :minReleaseDate
          AND tp.played_at   >= :since
        GROUP BY tp.track_id
        ORDER BY cnt DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> topNewReleases(@Param("minReleaseDate") LocalDate minReleaseDate,
                                  @Param("since") LocalDateTime since,
                                  @Param("limit") int limit);


}
