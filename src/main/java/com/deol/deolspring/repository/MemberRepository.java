package com.deol.deolspring.repository;

import com.deol.deolspring.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Member JPA Repository
 * - PK 타입: Integer (Member.memberSeq)
 * - 일괄가입 로직에서 쓰는 메서드(이메일/아이디 중복, 조회) 포함
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    @Query("SELECT m FROM Member m WHERE m.memberEmail = :email")
    Optional<Member> findOneWithAuthoritiesByEmail(String email);

    // 아티스트 멤버 시퀀스 증가 (ROLE_ARTIST 기준)
    @Query("SELECT COALESCE(MAX(m.memberArtistSeq), 0) FROM Member m WHERE m.role = 'ROLE_ARTIST'")
    Integer findMaxArtistNo();

    // 로그인 아이디로 조회/중복
    Optional<Member> findByMemberId(String memberId);
    boolean existsByMemberId(String memberId);

    // 이메일로 조회/중복
    Optional<Member> findByMemberEmail(String memberEmail);
    boolean existsByMemberEmail(String memberEmail);

    // 계정 찾기
    Optional<Member> findByMemberNameAndMemberEmail(String memberName, String memberEmail);
    Optional<Member> findByMemberNameAndMemberIdAndMemberEmail(String memberName, String memberId, String memberEmail);

    // 역할별 조회
    List<Member> findAllByRole(String role);

    // 아티스트 시퀀스로 조회
    @Query("SELECT m FROM Member m WHERE m.memberArtistSeq = :artistSeq")
    Optional<Member> findByArtistSeq(@Param("artistSeq") Integer artistSeq);

    // role='ROLE_ARTIST' + 이름 검색
    List<Member> findByRoleAndMemberArtistNameContainingIgnoreCase(String role, String keyword);


    /**
     * 최근 한 달(서비스에서 넘겨주는 날짜 구간) 내 release_date를 가진 앨범의 트랙 stream_count 합계를
     * 아티스트별로 집계해 내림차순 정렬.
     * - 동률이면 최근 가입일(create_date) 우선(원하면 제거/변경 가능)
     * - Pageable로 상위 N명만 반환
     */
    @Query(value = """
        SELECT 
            m.member_artist_name AS memberArtistName,
            m.member_artist_seq  AS memberArtistSeq,
            m.profile_image_url  AS profileImageUrl,
            COALESCE(SUM(t.stream_count), 0) AS streamSum
        FROM tbl_member m
        JOIN tbl_track  t ON t.track_artist_id = m.member_artist_seq
        JOIN tbl_album  a ON a.album_id = t.album_id
        WHERE m.role = 'ROLE_ARTIST'
          AND a.release_date BETWEEN :startDate AND :endDate
        GROUP BY m.member_artist_seq, m.member_artist_name, m.profile_image_url, m.create_date
        ORDER BY streamSum DESC, m.create_date DESC
        """,
            nativeQuery = true)
    List<PopularArtistProjection> findPopularArtistsInWindow(LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable);


    List<Member> findByMemberArtistSeqIn(Collection<Integer> artistSeqs);
}

