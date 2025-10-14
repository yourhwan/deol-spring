package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Track;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackPopularDto {
    private Integer trackId;
    private String trackTitle;
    private String albumTitle;
    private String albumCover;

    public static TrackPopularDto fromEntity(Track track) {
        return TrackPopularDto.builder()
                .trackId(track.getTrackId())
                .trackTitle(track.getTrackTitle())
                .albumTitle(track.getAlbum().getAlbumTitle())
                .albumCover(track.getAlbum().getCoverImage())
                .build();
    }
}
