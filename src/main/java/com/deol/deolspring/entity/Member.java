package com.deol.deolspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_member")
public class Member {

    @Id
    @Column(name = "member_seq", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberSeq;

    @Column(name = "member_artist_seq")
    private Integer memberArtistSeq;

    @Column(name = "role", length = 20, nullable = false)
    private String role; // 예: "ADMIN", "ARTIST", "USER"

    @Column(name = "member_id", length = 50, nullable = false, unique = true) // 사용자 아이디를 유니크 키로 설정
    private String memberId;

    @JsonIgnore
    @Column(name = "member_password", length = 100, nullable = false) // 비밀번호 필드 추가, 길이 제한 설정
    private String memberPassword;

    @Column(name = "member_name", length = 50, nullable = false)
    private String memberName;

    @Column(name = "member_nickname", length = 50)
    private String memberNickname;

    @Column(name = "member_artist_name", length = 50)
    private String memberArtistName;

    @Column(name = "member_email", length = 50, unique = true, nullable = false)
    private String memberEmail;

    @Column(name = "member_gender", length = 10, nullable = false)
    private String memberGender;

    @Column(name = "member_birthdate", length = 100, nullable = false)
    private String memberBirthdate;

    @Column(name = "create_date", columnDefinition = "TIMESTAMP", nullable = false) // 가입일 필드 추가, TIMESTAMP 사용
    private LocalDateTime createDate;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl; // 프로필 사진 URL 필드 추가

}
