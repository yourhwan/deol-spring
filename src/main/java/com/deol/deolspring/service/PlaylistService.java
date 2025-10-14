package com.deol.deolspring.service;

import com.deol.deolspring.dto.*;
import com.deol.deolspring.entity.CurrentPlaylist;
import com.deol.deolspring.entity.Playlist;
import com.deol.deolspring.entity.Track;
import com.deol.deolspring.repository.CurrentPlaylistRepository;
import com.deol.deolspring.repository.PlaylistRepository;
import com.deol.deolspring.repository.TrackRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final CurrentPlaylistRepository currentPlaylistRepository;
    private final EntityManager entityManager; // JPA를 통한 커스텀 쿼리 실행을 위해
    private final MemberService memberService; // JWT에서 로그인 한 사용자의 멤버 시퀀스 자동 추출
    private final S3Service s3Service;

    // 플레이리스트 생성
    @Transactional
    public PlaylistDto createPlaylist(PlaylistDto playlistDto, MultipartFile playlistCover) throws IOException {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        // 플레이리스트 커버 이미지를 S3에 업로드
        String playlistCoverUrl = s3Service.upload(playlistCover);  // 커버 이미지 S3에 업로드

        // 플레이리스트 생성
        Playlist playlist = new Playlist();
        playlist.setPlaylistName(playlistDto.getPlaylistName());
        playlist.setPlaylistDescription(playlistDto.getPlaylistDescription());
        playlist.setPlaylistCover(playlistCoverUrl);  // S3에서 받은 URL을 저장
        playlist.setMemberSeq(memberSeq);  // 유저의 memberSeq 자동 설정

        // 플레이리스트 저장
        Playlist savedPlaylist = playlistRepository.save(playlist);

        // PlaylistLoadDto로 변환하여 반환
        return new PlaylistDto(
                savedPlaylist.getPlaylistId(),
                savedPlaylist.getPlaylistName(),
                savedPlaylist.getPlaylistDescription()
        );
    }







    // 플레이리스트 삭제
    @Transactional
    public void deletePlaylist(Integer playlistId) {

        Integer memberSeq = memberService.getLoggedMemberSeq();

        // 플레이리스트가 존재하는지 확인
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);

        if (playlistOptional.isPresent()) {
            Playlist playlist = playlistOptional.get();

            // 플레이리스트의 owner (memberSeq)와 로그인된 사용자의 memberSeq가 일치하는지 확인
            if (!playlist.getMemberSeq().equals(memberSeq)) {
                throw new RuntimeException("User is not authorized to delete this playlist");
            }

            // 플레이리스트에서 트랙들을 삭제 (중간 테이블에서 삭제)
            String deleteTracksQuery = "DELETE FROM tbl_playlist_tracks WHERE playlist_id = :playlistId";
            entityManager.createNativeQuery(deleteTracksQuery)
                    .setParameter("playlistId", playlistId)
                    .executeUpdate();

            // 플레이리스트 삭제
            playlistRepository.delete(playlist);
        } else {
            throw new RuntimeException("Playlist not found");
        }
    }





    // 플레이리스트에 트랙 추가
    @Transactional
    public PlaylistAddDto addTrackToPlaylist(Integer playlistId, Integer trackId) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);
        Optional<Track> trackOptional = trackRepository.findById(trackId);

        if (playlistOptional.isPresent() && trackOptional.isPresent()) {
            Playlist playlist = playlistOptional.get();
            Track track = trackOptional.get();

            if (!playlist.getMemberSeq().equals(memberSeq)) {
                throw new RuntimeException("User is not authorized to add tracks to this playlist");
            }

            // 트랙을 중간 테이블에 추가
            String insertQuery = "INSERT INTO tbl_playlist_tracks (playlist_id, track_id) VALUES (:playlistId, :trackId)";
            entityManager.createNativeQuery(insertQuery)
                    .setParameter("playlistId", playlistId)
                    .setParameter("trackId", trackId)
                    .executeUpdate();

            // 트랙 정보 다시 가져오기 (playlist_track_id 포함)
            String sql = "SELECT " +
                    "t.track_id, t.track_title, t.track_file, t.track_duration, t.track_genre, t.track_lyrics, " +
                    "a.album_title, a.cover_image, m.member_artist_name, pt.playlist_track_id " +
                    "FROM tbl_track t " +
                    "JOIN tbl_playlist_tracks pt ON t.track_id = pt.track_id " +
                    "JOIN tbl_album a ON t.album_id = a.album_id " +
                    "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                    "WHERE pt.playlist_id = :playlistId";

            List<Object[]> result = entityManager.createNativeQuery(sql)
                    .setParameter("playlistId", playlistId)
                    .getResultList();

            List<TrackDetailDto> trackDtos = new ArrayList<>();
            for (Object[] row : result) {
                Integer tId = ((Number) row[0]).intValue();
                String tTitle = (String) row[1];
                String tFile = (String) row[2];
                String tDuration = (String) row[3];
                String tGenre = (String) row[4];
                String tLyrics = (String) row[5];
                String albumTitle = (String) row[6];
                String albumCover = (String) row[7];
                String artistName = (String) row[8];
                Long playlistTrackId = ((Number) row[9]).longValue();

                trackDtos.add(new TrackDetailDto(
                        tId, tTitle, tFile, tDuration, tGenre, tLyrics,
                        albumTitle, albumCover, artistName, playlistTrackId
                ));
            }

            return new PlaylistAddDto(
                    playlist.getPlaylistId(),
                    playlist.getPlaylistName(),
                    playlist.getPlaylistDescription(),
                    playlist.getPlaylistCover(),
                    trackDtos,
                    playlist.getMemberSeq()
            );
        }

        throw new RuntimeException("Playlist or Track not found");
    }







    // 앨범의 모든 트랙 추가
    @Transactional
    public PlaylistAddDto addAllAlbumTracksToPlaylist(Integer playlistId, Integer albumId) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다"));

        if (!playlist.getMemberSeq().equals(memberSeq)) {
            throw new RuntimeException("해당 플레이리스트에 추가할 권한이 없습니다.");
        }

        // 앨범의 트랙 목록 조회
        List<Track> albumTracks = trackRepository.findAllByAlbum_AlbumId(albumId);
        for (Track track : albumTracks) {
            // 이미 추가되어 있는 트랙인지 확인 (중복 방지)
            String checkQuery = "SELECT COUNT(*) FROM tbl_playlist_tracks WHERE playlist_id = :playlistId AND track_id = :trackId";
            Long count = ((Number) entityManager.createNativeQuery(checkQuery)
                    .setParameter("playlistId", playlistId)
                    .setParameter("trackId", track.getTrackId())
                    .getSingleResult()).longValue();

            if (count == 0) {
                // 중복이 아니라면 추가
                String insertQuery = "INSERT INTO tbl_playlist_tracks (playlist_id, track_id) VALUES (:playlistId, :trackId)";
                entityManager.createNativeQuery(insertQuery)
                        .setParameter("playlistId", playlistId)
                        .setParameter("trackId", track.getTrackId())
                        .executeUpdate();
            }
        }

        // 기존 `addTrackToPlaylist`와 동일하게 전체 트랙을 다시 조회해 반환
        return getPlaylistByUserAndId(playlistId);
    }








    // 특정 플레이리스트에서 특정 트랙 삭제
    @Transactional
    public PlaylistAddDto removeTrackFromPlaylist(Long playlistTrackId) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        // playlist_id 조회
        String playlistIdQuery = "SELECT playlist_id FROM tbl_playlist_tracks WHERE playlist_track_id = :playlistTrackId";
        Integer playlistId = ((Number) entityManager.createNativeQuery(playlistIdQuery)
                .setParameter("playlistTrackId", playlistTrackId)
                .getSingleResult()).intValue();

        // playlist 정보 조회
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);
        if (playlistOptional.isEmpty()) {
            throw new RuntimeException("해당 플레이리스트를 찾을 수 없습니다.");
        }

        Playlist playlist = playlistOptional.get();

        if (!playlist.getMemberSeq().equals(memberSeq)) {
            throw new RuntimeException("사용자는 이 플레이리스트의 소유자가 아닙니다");
        }

        // 삭제
        String deleteQuery = "DELETE FROM tbl_playlist_tracks WHERE playlist_track_id = :playlistTrackId";
        entityManager.createNativeQuery(deleteQuery)
                .setParameter("playlistTrackId", playlistTrackId)
                .executeUpdate();

        // 트랙 목록 조회 (앨범 커버, 아티스트 이름 포함)
        String trackQuery =
                "SELECT t.track_id, t.track_title, t.track_file, t.track_duration, " +
                        "t.track_genre, t.track_lyrics, a.album_title, a.cover_image, m.member_artist_name, pt.playlist_track_id " +
                        "FROM tbl_track t " +
                        "JOIN tbl_playlist_tracks pt ON t.track_id = pt.track_id " +
                        "JOIN tbl_album a ON t.album_id = a.album_id " +
                        "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                        "WHERE pt.playlist_id = :playlistId";

        List<Object[]> resultList = entityManager.createNativeQuery(trackQuery)
                .setParameter("playlistId", playlist.getPlaylistId())
                .getResultList();

        List<TrackDetailDto> trackDtos = new ArrayList<>();
        for (Object[] row : resultList) {
            Integer trackId = ((Number) row[0]).intValue();
            String trackTitle = (String) row[1];
            String trackFile = (String) row[2];
            String trackDuration = (String) row[3];
            String trackGenre = (String) row[4];
            String trackLyrics = (String) row[5];
            String albumTitle = (String) row[6];
            String coverImage = (String) row[7];
            String artistName = (String) row[8];
            Long playlistTrackIdResult = ((Number) row[9]).longValue();

            trackDtos.add(new TrackDetailDto(
                    trackId, trackTitle, trackFile, trackDuration, trackGenre,
                    trackLyrics, albumTitle, coverImage, artistName, playlistTrackIdResult
            ));
        }

        return new PlaylistAddDto(
                playlist.getPlaylistId(),
                playlist.getPlaylistName(),
                playlist.getPlaylistDescription(),
                playlist.getPlaylistCover(),
                trackDtos,
                playlist.getMemberSeq()
        );
    }








    // 특정 유저의 모든 플레이리스트 조회
    @Transactional
    public List<PlaylistLoadDto> getAllPlaylistsByUser() {
        Integer memberSeq = memberService.getLoggedMemberSeq();  // 로그인한 사용자의 memberSeq 가져오기

        // 로그인한 사용자의 플레이리스트 조회
        List<Playlist> playlists = playlistRepository.findAllByUserMemberSeq(memberSeq);

        return playlists.stream()
                .map(playlist -> new PlaylistLoadDto(
                        playlist.getPlaylistId(),
                        playlist.getPlaylistName(),
                        playlist.getPlaylistDescription(),
                        playlist.getPlaylistCover()  // S3에서 받은 커버 이미지 URL을 포함
                ))
                .collect(Collectors.toList());
    }






    @Transactional
    public PlaylistAddDto getPlaylistByUserAndId(Integer playlistId) {

        Integer memberSeq = memberService.getLoggedMemberSeq();

        // 플레이리스트 조회
        String playlistQuery = "SELECT p.* FROM tbl_playlist p WHERE p.member_seq = :memberSeq AND p.playlist_id = :playlistId";
        List<Playlist> playlists = entityManager.createNativeQuery(playlistQuery, Playlist.class)
                .setParameter("memberSeq", memberSeq)
                .setParameter("playlistId", playlistId)
                .getResultList();

        if (!playlists.isEmpty()) {
            Playlist playlist = playlists.get(0);

            // ✅ 트랙 목록 + 앨범 커버 + 앨범 제목 + 아티스트 이름 + playlist_track_id 조회
            String trackQuery =
                    "SELECT t.track_id, t.track_title, t.track_file, t.track_duration, t.track_genre, t.track_lyrics, " +
                            "a.album_title, a.cover_image, m.member_artist_name, pt.playlist_track_id " +
                            "FROM tbl_track t " +
                            "JOIN tbl_playlist_tracks pt ON t.track_id = pt.track_id " +
                            "JOIN tbl_album a ON t.album_id = a.album_id " +
                            "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                            "WHERE pt.playlist_id = :playlistId " +
                            "ORDER BY pt.playlist_track_id ASC";

            List<Object[]> result = entityManager.createNativeQuery(trackQuery)
                    .setParameter("playlistId", playlistId)
                    .getResultList();

            // 결과 매핑
            List<TrackDetailDto> trackDtos = new ArrayList<>();
            for (Object[] row : result) {
                Integer trackId = ((Number) row[0]).intValue();
                String trackTitle = (String) row[1];
                String trackFile = (String) row[2];
                String trackDuration = (String) row[3];
                String trackGenre = (String) row[4];
                String trackLyrics = (String) row[5];
                String albumTitle = (String) row[6];
                String albumCover = (String) row[7];
                String artistName = (String) row[8];
                Long playlistTrackId = ((Number) row[9]).longValue();

                trackDtos.add(new TrackDetailDto(
                        trackId, trackTitle, trackFile, trackDuration, trackGenre, trackLyrics,
                        albumTitle, albumCover, artistName, playlistTrackId
                ));
            }

            return new PlaylistAddDto(
                    playlist.getPlaylistId(),
                    playlist.getPlaylistName(),
                    playlist.getPlaylistDescription(),
                    playlist.getPlaylistCover(),
                    trackDtos,
                    playlist.getMemberSeq()
            );
        }

        return null;
    }










    // 현재 재생목록 생성
    @Transactional
    public CurrentPlaylistDto createCurrentPlaylist() {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        Optional<CurrentPlaylist> existingPlaylist = currentPlaylistRepository.findByMemberSeq(memberSeq);

        if (existingPlaylist.isPresent()) {
            return new CurrentPlaylistDto(
                    existingPlaylist.get().getCurrentPlaylistId(),
                    new ArrayList<>(),
                    existingPlaylist.get().getMemberSeq()
            );
        }

        CurrentPlaylist newPlaylist = new CurrentPlaylist();
        newPlaylist.setMemberSeq(memberSeq);
        CurrentPlaylist saved = currentPlaylistRepository.save(newPlaylist);

        return new CurrentPlaylistDto(saved.getCurrentPlaylistId(), new ArrayList<>(), saved.getMemberSeq());
    }







    // 현재 재생목록에 트랙 추가
    @Transactional
    public CurrentPlaylistDto addTrackToCurrentPlaylist(Integer currentPlaylistId, Integer trackId) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        // 현재 재생목록과 트랙이 존재하는지 확인
        Optional<CurrentPlaylist> currentPlaylistOptional = currentPlaylistRepository.findById(currentPlaylistId);
        Optional<Track> trackOptional = trackRepository.findById(trackId);

        if (currentPlaylistOptional.isPresent() && trackOptional.isPresent()) {
            CurrentPlaylist currentPlaylist = currentPlaylistOptional.get();
            Track track = trackOptional.get();

            if (!currentPlaylist.getMemberSeq().equals(memberSeq)) {
                throw new RuntimeException("User is not authorized to add tracks to this current playlist");
            }

            // 중간 테이블에 트랙을 추가
            String insertQuery = "INSERT INTO tbl_current_playlist_tracks (current_playlist_id, track_id) VALUES (:currentPlaylistId, :trackId)";
            entityManager.createNativeQuery(insertQuery)
                    .setParameter("currentPlaylistId", currentPlaylistId)
                    .setParameter("trackId", trackId)
                    .executeUpdate();

            // 트랙 목록을 다시 가져오기 (중간 테이블 고유 ID 포함)
            String trackQuery =
                    "SELECT t.track_id, t.track_title, t.track_file, t.track_duration, " +
                            "t.track_genre, t.track_lyrics, a.cover_image, a.album_title, " +
                            "m.member_artist_name, cpt.current_playlist_track_id " +
                            "FROM tbl_track t " +
                            "JOIN tbl_current_playlist_tracks cpt ON t.track_id = cpt.track_id " +
                            "JOIN tbl_album a ON t.album_id = a.album_id " +
                            "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                            "WHERE cpt.current_playlist_id = :currentPlaylistId";

            List<Object[]> result = entityManager.createNativeQuery(trackQuery)
                    .setParameter("currentPlaylistId", currentPlaylistId)
                    .getResultList();

            List<TrackDetailDto> trackDtos = new ArrayList<>();
            for (Object[] row : result) {
                Integer trackIdResult = ((Number) row[0]).intValue();
                String trackTitle = (String) row[1];
                String trackFile = (String) row[2];
                String trackDuration = (String) row[3];
                String trackGenre = (String) row[4];
                String trackLyrics = (String) row[5];
                String albumCover = (String) row[6];
                String albumTitle = (String) row[7];
                String artistName = (String) row[8];
                Long currentPlaylistTrackId = ((Number) row[9]).longValue();  // ✅ 추가된 중간 테이블 ID

                trackDtos.add(new TrackDetailDto(
                        trackIdResult, trackTitle, trackFile, trackDuration, trackGenre,
                        trackLyrics, albumTitle, albumCover, artistName, currentPlaylistTrackId
                ));
            }

            return new CurrentPlaylistDto(currentPlaylistId, trackDtos, memberSeq);
        }

        throw new RuntimeException("Current Playlist or Track not found");
    }













    // 현재 재생목록에서 트랙 삭제
    @Transactional
    public CurrentPlaylistDto removeTrackFromCurrentPlaylist(Integer currentPlaylistId, Long currentPlaylistTrackId) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        // current_playlist_id 조회
        String playlistIdQuery = "SELECT current_playlist_id FROM tbl_current_playlist_tracks WHERE current_playlist_track_id = :currentPlaylistTrackId";
        Integer foundPlaylistId = ((Number) entityManager.createNativeQuery(playlistIdQuery)
                .setParameter("currentPlaylistTrackId", currentPlaylistTrackId)
                .getSingleResult()).intValue();

        if (!foundPlaylistId.equals(currentPlaylistId)) {
            throw new RuntimeException("재생목록 ID가 일치하지 않습니다");
        }

        // 삭제
        String deleteQuery = "DELETE FROM tbl_current_playlist_tracks WHERE current_playlist_track_id = :currentPlaylistTrackId";
        entityManager.createNativeQuery(deleteQuery)
                .setParameter("currentPlaylistTrackId", currentPlaylistTrackId)
                .executeUpdate();

        // 삭제 후 남은 트랙들 조회
        String trackQuery = "SELECT t.track_id, t.track_title, t.track_file, t.track_duration, t.track_genre, t.track_lyrics, " +
                "a.cover_image AS album_cover, a.album_title, m.member_artist_name, cpt.current_playlist_track_id " +
                "FROM tbl_track t " +
                "JOIN tbl_current_playlist_tracks cpt ON t.track_id = cpt.track_id " +
                "JOIN tbl_album a ON t.album_id = a.album_id " +
                "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                "WHERE cpt.current_playlist_id = :currentPlaylistId";

        List<Object[]> result = entityManager.createNativeQuery(trackQuery)
                .setParameter("currentPlaylistId", currentPlaylistId)
                .getResultList();

        List<TrackDetailDto> trackDtos = new ArrayList<>();
        for (Object[] row : result) {
            Integer trackId = ((Number) row[0]).intValue();
            String trackTitle = (String) row[1];
            String trackFile = (String) row[2];
            String trackDuration = (String) row[3];
            String trackGenre = (String) row[4];
            String trackLyrics = (String) row[5];
            String albumCover = (String) row[6];
            String albumTitle = (String) row[7];
            String artistName = (String) row[8];
            Long playlistTrackId = ((Number) row[9]).longValue();

            trackDtos.add(new TrackDetailDto(
                    trackId, trackTitle, trackFile, trackDuration, trackGenre, trackLyrics,
                    albumTitle, albumCover, artistName, playlistTrackId
            ));
        }

        return new CurrentPlaylistDto(currentPlaylistId, trackDtos, memberSeq);
    }






    // 현재 재생목록 조회
    // PlaylistService.java

    @Transactional
    public CurrentPlaylistDto getCurrentPlaylistTracks(Integer currentPlaylistId) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        // ✅ 순서를 보장하기 위해 current_playlist_track_id 기준으로 정렬
        String trackQuery = "SELECT t.track_id, t.track_title, t.track_file, t.track_duration, t.track_genre, t.track_lyrics, " +
                "a.album_title, a.cover_image AS album_cover, m.member_artist_name, cpt.current_playlist_track_id " +
                "FROM tbl_track t " +
                "JOIN tbl_current_playlist_tracks cpt ON t.track_id = cpt.track_id " +
                "JOIN tbl_current_playlist cp ON cp.current_playlist_id = cpt.current_playlist_id " +
                "JOIN tbl_album a ON t.album_id = a.album_id " +
                "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                "WHERE cpt.current_playlist_id = :currentPlaylistId AND cp.member_seq = :memberSeq " +
                "ORDER BY cpt.current_playlist_track_id ASC"; // ✅ 추가

        List<Object[]> result = entityManager.createNativeQuery(trackQuery)
                .setParameter("currentPlaylistId", currentPlaylistId)
                .setParameter("memberSeq", memberSeq)
                .getResultList();

        List<TrackDetailDto> trackDtos = new ArrayList<>();
        for (Object[] row : result) {
            Integer trackId = ((Number) row[0]).intValue();
            String trackTitle = (String) row[1];
            String trackFile = (String) row[2];
            String trackDuration = (String) row[3];
            String trackGenre = (String) row[4];
            String trackLyrics = (String) row[5];
            String albumTitle = (String) row[6];
            String coverImage = (String) row[7];
            String artistName = (String) row[8];
            Long currentPlaylistTrackId = ((Number) row[9]).longValue();

            trackDtos.add(new TrackDetailDto(
                    trackId, trackTitle, trackFile, trackDuration, trackGenre,
                    trackLyrics, albumTitle, coverImage, artistName, currentPlaylistTrackId
            ));
        }

        return new CurrentPlaylistDto(currentPlaylistId, trackDtos, memberSeq);
    }



    // 차트의 전체 트랙을 플레이리스트에 추가
    @Transactional
    public PlaylistAddDto addTracksToPlaylist(Integer playlistId, List<Integer> trackIds) {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        if (!playlist.getMemberSeq().equals(memberSeq)) {
            throw new RuntimeException("해당 플레이리스트에 추가할 권한이 없습니다.");
        }

        for (Integer trackId : trackIds) {
            String insertQuery = "INSERT INTO tbl_playlist_tracks (playlist_id, track_id) VALUES (:playlistId, :trackId)";
            entityManager.createNativeQuery(insertQuery)
                    .setParameter("playlistId", playlistId)
                    .setParameter("trackId", trackId)
                    .executeUpdate();
        }

        // 트랙 정보 다시 조회
        String sql = "SELECT " +
                "t.track_id, t.track_title, t.track_file, t.track_duration, t.track_genre, t.track_lyrics, " +
                "a.album_title, a.cover_image, m.member_artist_name, pt.playlist_track_id " +
                "FROM tbl_track t " +
                "JOIN tbl_playlist_tracks pt ON t.track_id = pt.track_id " +
                "JOIN tbl_album a ON t.album_id = a.album_id " +
                "JOIN tbl_member m ON t.track_artist_id = m.member_artist_seq " +
                "WHERE pt.playlist_id = :playlistId";

        List<Object[]> result = entityManager.createNativeQuery(sql)
                .setParameter("playlistId", playlistId)
                .getResultList();

        List<TrackDetailDto> trackDtos = new ArrayList<>();
        for (Object[] row : result) {
            Integer tId = ((Number) row[0]).intValue();
            String tTitle = (String) row[1];
            String tFile = (String) row[2];
            String tDuration = (String) row[3];
            String tGenre = (String) row[4];
            String tLyrics = (String) row[5];
            String albumTitle = (String) row[6];
            String albumCover = (String) row[7];
            String artistName = (String) row[8];
            Long playlistTrackId = ((Number) row[9]).longValue();

            trackDtos.add(new TrackDetailDto(
                    tId, tTitle, tFile, tDuration, tGenre, tLyrics,
                    albumTitle, albumCover, artistName, playlistTrackId
            ));
        }

        return new PlaylistAddDto(
                playlist.getPlaylistId(),
                playlist.getPlaylistName(),
                playlist.getPlaylistDescription(),
                playlist.getPlaylistCover(),
                trackDtos,
                playlist.getMemberSeq()
        );
    }


}


