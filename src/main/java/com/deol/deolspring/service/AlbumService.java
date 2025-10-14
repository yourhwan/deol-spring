package com.deol.deolspring.service;

import com.deol.deolspring.dto.AlbumDetailDto;
import com.deol.deolspring.dto.AlbumDto;
import com.deol.deolspring.dto.TrackDto;
import com.deol.deolspring.entity.Album;
import com.deol.deolspring.entity.Member;
import com.deol.deolspring.entity.Track;
import com.deol.deolspring.repository.AlbumRepository;
import com.deol.deolspring.repository.MemberRepository;
import com.deol.deolspring.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository; // 앨범 저장소
    private final S3Service s3Service; // S3 서비스
    private final MemberService memberService; // 멤버 서비스 (로그인한 아티스트 정보 조회용)
    private final TrackRepository trackRepository; // 트랙 저장소
    private final MemberRepository memberRepository;

    // 앨범 업로드 메서드
    public Album uploadAlbum(AlbumDto albumDto, MultipartFile coverImage, List<TrackDto> trackDtos) {

        System.out.println("[DEBUG] albumDto: " + albumDto);
        System.out.println("[DEBUG] trackDtos: " + trackDtos);

        try {
            Integer artistId = memberService.getLoggedArtistSeq(); // 로그인한 아티스트 ID 가져오기
            System.out.println("[DEBUG] artistId: " + artistId);

            // 커버 이미지를 S3에 업로드
            String coverImageUrl = s3Service.upload(coverImage);
            System.out.println("[DEBUG] 커버 이미지 업로드 성공: " + coverImageUrl);

            // Album 엔티티 생성 및 저장
            Album album = Album.builder()
                    .albumArtistId(artistId)
                    .albumTitle(albumDto.getAlbumTitle())
                    .coverImage(coverImageUrl)
                    .releaseDate(LocalDate.now())
                    .albumGenre(albumDto.getAlbumGenre())
                    .albumDescription(albumDto.getAlbumDescription())
                    .build();

            Album savedAlbum = albumRepository.save(album);
            System.out.println("[DEBUG] 앨범 저장 성공: " + savedAlbum);

            Integer albumId = savedAlbum.getAlbumId();

            // Track 엔티티 생성 및 저장
            List<Track> tracks = new ArrayList<>();
            for (TrackDto trackDto : trackDtos) {
                System.out.println("[DEBUG] 처리 중인 trackDto: " + trackDto);
                if (trackDto.getTrackFile() == null) {
                    System.err.println("[ERROR] trackFile이 null입니다. trackDto: " + trackDto);
                    continue;
                }
                // 각 트랙에 대한 정보 설정
                Track track = extractTrackMetadata(trackDto.getTrackFile());
                track.setTrackTitle(trackDto.getTrackTitle()); // 사용자가 입력한 제목 사용
                track.setAlbumId(albumId); // Album의 albumId 설정
                track.setTrackArtistId(artistId); // track_artist_id 설정
                tracks.add(track);
                System.out.println("[DEBUG] 트랙 생성 완료: " + track);
            }

            trackRepository.saveAll(tracks); // 트랙 리스트 저장
            System.out.println("[DEBUG] 모든 트랙 저장 완료: " + tracks);

            return savedAlbum;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 트랙 메타데이터 추출 메서드
    private Track extractTrackMetadata(MultipartFile trackFile) throws IOException {
        if (trackFile == null) {
            System.err.println("[ERROR] trackFile이 null입니다.");
            throw new IllegalArgumentException("trackFile이 null입니다.");
        }

        File tempFile = File.createTempFile("track-", ".mp3");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write(trackFile.getBytes());
        }

        try {
            AudioFile audioFile = AudioFileIO.read(tempFile);
            Tag tag = audioFile.getTag();

            long durationInMillis = audioFile.getAudioHeader().getTrackLength();
            int totalSeconds = (int) durationInMillis;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String durationFormatted = String.format("%d:%02d", minutes, seconds);

            String lyrics = tag.getFirst(FieldKey.LYRICS) != null ? tag.getFirst(FieldKey.LYRICS) : "No Lyrics Available";
            String genre = tag.getFirst(FieldKey.GENRE) != null ? tag.getFirst(FieldKey.GENRE) : "Unknown Genre";

            String trackFileUrl = s3Service.upload(trackFile);
            Track track = Track.builder()
                    .trackLyrics(lyrics)
                    .trackGenre(genre)
                    .trackDuration(durationFormatted)
                    .trackFile(trackFileUrl)
                    .build();
            System.out.println("[DEBUG] 트랙 메타데이터 추출 성공: " + track);
            return track;
        } catch (Exception e) {
            throw new IOException("메타데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    // ✅ 기본: 상위 10개
    public List<AlbumDto> getPopularAlbums() {
        return getPopularAlbums(10);
    }

    // ✅ 필요 시 개수 지정
    public List<AlbumDto> getPopularAlbums(int limit) {
        int topN = Math.max(1, limit);
        List<Album> albums = albumRepository.findPopularAlbumsOrderByStreams(PageRequest.of(0, topN));
        return albums.stream()
                .map(AlbumDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 앨범 상세 정보 불러오기
    public AlbumDetailDto getAlbumDetails(Integer albumId) {
        // 앨범 정보 조회
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("앨범을 찾을 수 없습니다."));

        // 수록곡 리스트 조회
        List<Track> tracks = trackRepository.findByAlbumId(albumId);

        // 아티스트 이름 조회
        String artistName = memberRepository.findByArtistSeq(album.getAlbumArtistId())
                .map(Member::getMemberArtistName)
                .orElse("Unknown Artist");

        // DTO로 변환
        return AlbumDetailDto.fromEntity(album, tracks, artistName);
    }

    // 아티스트의 다른 앨범 보여주기(현재 보는 앨범 제외)
    public List<AlbumDto> getOtherAlbumsByArtist(Integer artistId, Integer excludeAlbumId) {
        List<Album> albums = albumRepository.findByAlbumArtistIdAndAlbumIdNot(artistId, excludeAlbumId);
        return albums.stream()
                .map(AlbumDto::fromEntity)
                .toList();
    }
}
