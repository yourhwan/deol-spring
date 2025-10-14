package com.deol.deolspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_follow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @Column(nullable = false)
    private Integer followerId; // 현재 로그인한 사용자 (member_seq)

    @Column(nullable = false)
    private Integer followingArtistId; // 팔로우할 아티스트 (member_artist_seq)
}

