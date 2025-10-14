package com.deol.deolspring.dto;

import com.deol.deolspring.entity.Album;
import com.deol.deolspring.entity.Track;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumDetailDto {
    private Integer albumId;
    private String albumTitle;
    private String coverImage;

    // 상세 정보용 필드
    private String albumGenre;
    private String releaseDate;
    private String albumDescription;
    private String artistName;
    private Integer albumArtistId; // 앨범의 아티스트 ID 추가
    private List<TrackDetailDto> tracks;

    // 요약 정보를 위한 생성자
    public AlbumDetailDto(Integer albumId, String albumTitle, String coverImage) {
        this.albumId = albumId;
        this.albumTitle = albumTitle;
        this.coverImage = coverImage;
    }

    // 요약 정보를 위한 정적 메서드
    public static AlbumDetailDto fromEntitySummary(Album album) {
        return new AlbumDetailDto(
                album.getAlbumId(),
                album.getAlbumTitle(),
                album.getCoverImage()
        );
    }

    // 상세 정보를 위한 정적 메서드
    public static AlbumDetailDto fromEntity(
            Album album,
            List<Track> tracks,
            String artistName) {
        // 앨범 커버 이미지도 포함된 트랙 리스트를 처리
        String albumCover = album.getCoverImage();  // 앨범 커버 이미지 가져오기

        return AlbumDetailDto.builder()
                .albumId(album.getAlbumId())
                .albumTitle(album.getAlbumTitle())
                .albumGenre(album.getAlbumGenre())
                .releaseDate(album.getReleaseDate().toString())
                .albumDescription(album.getAlbumDescription())
                .coverImage(albumCover)  // 앨범 커버 이미지
                .artistName(artistName)
                .albumArtistId(album.getAlbumArtistId())
                .tracks(tracks.stream()
                        .map(track -> TrackDetailDto.fromEntity(track, albumCover, artistName, null))  // playlistTrackId는 앨범 상세에서는 null
                        .collect(Collectors.toList()))
                .build();
    }

}
