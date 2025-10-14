package com.deol.deolspring.repository;

import com.deol.deolspring.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    // 특정 유저의 플레이리스트 목록을 조회 (memberSeq를 기준)
    @Query("SELECT p FROM Playlist p WHERE p.memberSeq = :memberSeq")
    List<Playlist> findAllByUserMemberSeq(@Param("memberSeq") Integer memberSeq);


}

