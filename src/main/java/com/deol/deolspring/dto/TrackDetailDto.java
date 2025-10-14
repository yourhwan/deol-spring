package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Track;
import lombok.Data;

@Data
public class TrackDetailDto {
    private Integer trackId;
    private String trackTitle;
    private String trackFile;
    private String trackDuration;
    private String trackGenre;
    private String trackLyrics;
    private String albumTitle;
    private String coverImage;
    private String artistName;
    private Long playlistTrackId; // ✅ 플레이리스트 트랙 식별자

    public TrackDetailDto(Integer trackId, String trackTitle, String trackFile,
                          String trackDuration, String trackGenre, String trackLyrics,
                          String albumTitle, String coverImage, String artistName,
                          Long playlistTrackId) {
        this.trackId = trackId;
        this.trackTitle = trackTitle;
        this.trackFile = trackFile;
        this.trackDuration = trackDuration;
        this.trackGenre = trackGenre;
        this.trackLyrics = trackLyrics;
        this.albumTitle = albumTitle;
        this.coverImage = coverImage;
        this.artistName = artistName;
        this.playlistTrackId = playlistTrackId;
    }

    public static TrackDetailDto fromEntity(Track track, String coverImage, String artistName, Long playlistTrackId) {
        return new TrackDetailDto(
                track.getTrackId(),
                track.getTrackTitle(),
                track.getTrackFile(),
                track.getTrackDuration(),
                track.getTrackGenre(),
                track.getTrackLyrics(),
                track.getAlbum() != null ? track.getAlbum().getAlbumTitle() : null,
                coverImage,
                artistName,
                playlistTrackId
        );
    }
}
