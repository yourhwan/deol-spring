package com.deol.deolspring.service;

import com.deol.deolspring.dto.TrackChartDto;
import com.deol.deolspring.entity.Album;
import com.deol.deolspring.entity.Member;
import com.deol.deolspring.entity.Track;
import com.deol.deolspring.entity.TrackPlay;
import com.deol.deolspring.repository.AlbumRepository;
import com.deol.deolspring.repository.MemberRepository;
import com.deol.deolspring.repository.TrackPlayRepository;
import com.deol.deolspring.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final TrackPlayRepository trackPlayRepository;
    private final TrackRepository     trackRepository;
    private final AlbumRepository     albumRepository;
    private final MemberRepository    memberRepository;

    /** 재생 로그 적재(플레이어에서 '종료' 시 호출) */
    @Transactional
    public void logPlay(Integer trackId, Integer userId, String source) {
        TrackPlay log = TrackPlay.builder()
                .trackId(trackId)
                .userId(userId)
                .source(source)
                .playedAt(LocalDateTime.now())
                .build();
        trackPlayRepository.save(log);
    }

    /** "초" 또는 "m:ss" 문자열을 화면 표시용 "m:ss"로 정규화 */
    private String normalizeDuration(String raw) {
        if (raw == null || raw.isBlank()) return null;
        // 이미 "m:ss" 같은 포맷이면 그대로 사용
        if (raw.contains(":")) return raw;

        // 숫자 문자열(초) → m:ss
        try {
            int sec = Integer.parseInt(raw.trim());
            if (sec < 0) sec = 0;
            int m = sec / 60;
            int s = sec % 60;
            return m + ":" + String.format("%02d", s);
        } catch (NumberFormatException e) {
            // 다른 포맷이면 원문 유지
            return raw;
        }
    }

    /** 실시간 Top N (최근 24시간) */
    public List<TrackChartDto> getRealtimeTopN(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Object[]> rows = trackPlayRepository.topTracksSince(since, limit);
        if (rows.isEmpty()) return Collections.emptyList();

        // 1) (trackId, cnt) 분리
        List<Integer> trackIds = new ArrayList<>();
        Map<Integer, Long> playCountMap = new HashMap<>();
        for (Object[] r : rows) {
            Integer trackId = ((Number) r[0]).intValue();
            Long cnt        = ((Number) r[1]).longValue();
            trackIds.add(trackId);
            playCountMap.put(trackId, cnt);
        }

        // 2) 메타 조회
        List<Track> tracks = trackRepository.findAllById(trackIds);
        Map<Integer, Track> trackMap = tracks.stream()
                .collect(Collectors.toMap(Track::getTrackId, t -> t));

        Set<Integer> albumIds = tracks.stream()
                .map(Track::getAlbumId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Album> albumMap = albumRepository.findAllById(albumIds).stream()
                .collect(Collectors.toMap(Album::getAlbumId, a -> a));

        // 3) 아티스트 이름 맵
        Set<Integer> artistIds = tracks.stream()
                .map(Track::getTrackArtistId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> artistNameMap;
        if (!artistIds.isEmpty()) {
            artistNameMap = memberRepository.findByMemberArtistSeqIn(artistIds).stream()
                    .collect(Collectors.toMap(
                            Member::getMemberArtistSeq,
                            Member::getMemberArtistName
                    ));
        } else {
            artistNameMap = Collections.emptyMap();
        }

        // 4) rows 순서대로 DTO 조립 (rank = i+1)
        return range(0, rows.size())
                .mapToObj(i -> {
                    Integer trackId = ((Number) rows.get(i)[0]).intValue();
                    Long    cnt     = ((Number) rows.get(i)[1]).longValue();

                    Track t = trackMap.get(trackId);
                    Album a = (t != null) ? albumMap.get(t.getAlbumId()) : null;

                    Integer artistId   = (t != null) ? t.getTrackArtistId() : null;
                    String  artistName = (artistId != null) ? artistNameMap.get(artistId) : null;

                    // ★ 추가: 재생시간
                    String duration = (t != null) ? normalizeDuration(t.getTrackDuration()) : null;

                    return new TrackChartDto(
                            trackId,
                            t != null ? t.getTrackTitle() : null,
                            artistId,
                            artistName,
                            a != null ? a.getAlbumId() : null,
                            a != null ? a.getAlbumTitle() : null,
                            a != null ? a.getCoverImage() : null,
                            cnt,
                            i + 1,
                            duration // ★ 추가
                    );
                })
                .collect(Collectors.toList());
    }

    /** 신곡 TOP: 발매 30일 이내 + 최근 7일 재생 수 */
    public List<TrackChartDto> getNewReleasesTop(int limit, int releaseDays, int lookbackDays) {
        int capped = Math.min(Math.max(limit, 1), 100);

        LocalDate     minReleaseDate = LocalDate.now().minusDays(releaseDays);
        LocalDateTime since          = LocalDateTime.now().minusDays(lookbackDays);

        // 1) 집계 결과 (trackId, cnt)
        List<Object[]> rows = trackPlayRepository.topNewReleases(minReleaseDate, since, capped);
        if (rows.isEmpty()) return Collections.emptyList();

        // 2) (trackId, cnt) 분리
        List<Integer> trackIds = new ArrayList<>();
        Map<Integer, Long> playCountMap = new HashMap<>();
        for (Object[] r : rows) {
            Integer trackId = ((Number) r[0]).intValue();
            Long cnt        = ((Number) r[1]).longValue();
            trackIds.add(trackId);
            playCountMap.put(trackId, cnt);
        }

        // 3) 메타 조회
        List<Track> tracks = trackRepository.findAllById(trackIds);
        Map<Integer, Track> trackMap = tracks.stream()
                .collect(Collectors.toMap(Track::getTrackId, t -> t));

        Set<Integer> albumIds = tracks.stream()
                .map(Track::getAlbumId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Album> albumMap = albumRepository.findAllById(albumIds).stream()
                .collect(Collectors.toMap(Album::getAlbumId, a -> a));

        // 4) 아티스트 이름 맵
        Set<Integer> artistIds = tracks.stream()
                .map(Track::getTrackArtistId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> artistNameMap;
        if (!artistIds.isEmpty()) {
            artistNameMap = memberRepository.findByMemberArtistSeqIn(artistIds).stream()
                    .collect(Collectors.toMap(
                            Member::getMemberArtistSeq,
                            Member::getMemberArtistName
                    ));
        } else {
            artistNameMap = Collections.emptyMap();
        }

        // 5) rows 순서대로 DTO 조립
        return range(0, rows.size())
                .mapToObj(i -> {
                    Integer trackId = ((Number) rows.get(i)[0]).intValue();
                    Long    cnt     = ((Number) rows.get(i)[1]).longValue();

                    Track t = trackMap.get(trackId);
                    Album a = (t != null) ? albumMap.get(t.getAlbumId()) : null;

                    Integer artistId   = (t != null) ? t.getTrackArtistId() : null;
                    String  artistName = (artistId != null) ? artistNameMap.get(artistId) : null;

                    // ★ 추가: 재생시간
                    String duration = (t != null) ? normalizeDuration(t.getTrackDuration()) : null;

                    return new TrackChartDto(
                            trackId,
                            t != null ? t.getTrackTitle() : null,
                            artistId,
                            artistName,
                            a != null ? a.getAlbumId() : null,
                            a != null ? a.getAlbumTitle() : null,
                            a != null ? a.getCoverImage() : null,
                            cnt,
                            i + 1,
                            duration // ★ 추가
                    );
                })
                .collect(Collectors.toList());
    }
}
