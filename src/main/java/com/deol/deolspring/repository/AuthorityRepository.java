package com.deol.deolspring.repository;

import com.deol.deolspring.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    // authorityName으로 AuthorityEntity를 조회하는 메서드
    Optional<Authority> findByAuthorityName(String authorityName);
}
