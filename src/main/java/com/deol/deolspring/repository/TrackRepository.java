package com.deol.deolspring.repository;

import com.deol.deolspring.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Track t SET t.streamCount = t.streamCount + 1 WHERE t.trackId = :trackId")
    void incrementStreamCount(Integer trackId);

    // 특정 앨범을 앨범아이디로 조회
    List<Track> findByAlbumId(Integer albumId);

    // 특정 아티스트의 인기 곡 상위 3개 가져오기
    List<Track> findTop3ByTrackArtistIdOrderByStreamCountDesc(@Param("artistId") Integer artistId);

    // 앨범 ID로 모든 트랙 가져오기
    List<Track> findAllByAlbum_AlbumId(Integer albumId);

    // trackTitle에 키워드 포함된 트랙 검색
    List<Track> findByTrackTitleContainingIgnoreCase(String keyword);


}
