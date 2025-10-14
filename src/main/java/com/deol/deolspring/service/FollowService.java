package com.deol.deolspring.service;

import com.deol.deolspring.dto.FollowDto;
import com.deol.deolspring.entity.Follow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final MemberService memberService;

    @PersistenceContext
    private EntityManager em;

    // 아티스트 팔로우
    @Transactional
    public void followArtist(Integer artistId) {
        Integer followerId = memberService.getLoggedMemberSeq();

        String jpql = "SELECT COUNT(f) FROM Follow f WHERE f.followerId = :followerId AND f.followingArtistId = :artistId";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("followerId", followerId)
                .setParameter("artistId", artistId)
                .getSingleResult();

        if (count > 0) {
            throw new RuntimeException("이미 팔로우한 아티스트입니다.");
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingArtistId(artistId)
                .build();
        em.persist(follow);
    }

    // 아티스트 언팔로우
    @Transactional
    public void unfollowArtist(Integer artistId) {
        Integer followerId = memberService.getLoggedMemberSeq();

        String jpql = "DELETE FROM Follow f WHERE f.followerId = :followerId AND f.followingArtistId = :artistId";
        em.createQuery(jpql)
                .setParameter("followerId", followerId)
                .setParameter("artistId", artistId)
                .executeUpdate();
    }

    // 팔로우 여부 확인
    @Transactional
    public boolean isFollowing(Integer artistId) {
        Integer followerId = memberService.getLoggedMemberSeq();

        String jpql = "SELECT COUNT(f) FROM Follow f WHERE f.followerId = :followerId AND f.followingArtistId = :artistId";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("followerId", followerId)
                .setParameter("artistId", artistId)
                .getSingleResult();

        return count > 0;
    }

    // 나의 팔로잉 아티스트 목록 불러오기
    @Transactional
    public List<Follow> getFollowingArtists() {
        Integer followerId = memberService.getLoggedMemberSeq();

        String jpql = "SELECT f FROM Follow f WHERE f.followerId = :followerId";
        TypedQuery<Follow> query = em.createQuery(jpql, Follow.class);
        query.setParameter("followerId", followerId);

        return query.getResultList();
    }

    @Transactional
    public List<FollowDto> getFollowDetails() {
        Integer followerId = memberService.getLoggedMemberSeq();

        String jpql = """
        SELECT new com.deol.deolspring.dto.FollowDto(
            m.memberArtistSeq,
            m.memberArtistName,
            m.profileImageUrl
        )
        FROM Follow f
        JOIN Member m ON f.followingArtistId = m.memberArtistSeq
        WHERE f.followerId = :followerId
    """;

        return em.createQuery(jpql, FollowDto.class)
                .setParameter("followerId", followerId)
                .getResultList();
    }


    @Transactional
    public Long getFollowerCount(Integer artistId) {
        String jpql = "SELECT COUNT(f) FROM Follow f WHERE f.followingArtistId = :artistId";
        return em.createQuery(jpql, Long.class)
                .setParameter("artistId", artistId)
                .getSingleResult();
    }

}
