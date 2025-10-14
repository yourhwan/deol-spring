package com.deol.deolspring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_track_play")
public class TrackPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_id", nullable = false)
    private Long playId;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "source")
    private String source;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;
}

