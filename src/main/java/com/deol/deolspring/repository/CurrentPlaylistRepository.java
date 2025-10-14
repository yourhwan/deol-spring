package com.deol.deolspring.repository;

import com.deol.deolspring.entity.CurrentPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrentPlaylistRepository extends JpaRepository<CurrentPlaylist, Integer> {

    // 특정 회원의 currentPlaylist를 조회하는 메서드
    Optional<CurrentPlaylist> findByMemberSeq(Integer memberSeq);
}

