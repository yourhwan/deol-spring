// src/main/java/com/deol/deolspring/service/SearchService.java
package com.deol.deolspring.service;

import com.deol.deolspring.dto.AlbumSearchDto;
import com.deol.deolspring.dto.SearchArtistDto;
import com.deol.deolspring.dto.SearchResultDto;
import com.deol.deolspring.dto.TrackSearchDto;
import com.deol.deolspring.entity.Member;
import com.deol.deolspring.repository.AlbumRepository;
import com.deol.deolspring.repository.MemberRepository;
import com.deol.deolspring.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    /**
     * keyword 를 기준으로
     *  1) Member(role='ROLE_ARTIST') 중 memberArtistName 에 keyword 포함 → SearchArtistDto
     *  2) Album.albumTitle 에 keyword 포함 → AlbumSearchDto
     *  3) Track.trackTitle 에 keyword 포함 → TrackSearchDto
     *  4) (추가) 1) 에서 매칭된 아티스트들의 전체 앨범도 모두 조회
     * 이 네 가지 결과를 합쳐서 SearchResultDto 로 반환
     */
    public SearchResultDto searchByKeyword(String keyword) {
        // ── 1) 아티스트 검색 ─────────────────────────────────────────────────────────
        List<Member> matchedArtists = memberRepository
                .findByRoleAndMemberArtistNameContainingIgnoreCase("ROLE_ARTIST", keyword);

        List<SearchArtistDto> artistDtos = matchedArtists.stream()
                .map(SearchArtistDto::fromMember)
                .collect(Collectors.toList());

        // ── 2) 앨범 검색 (제목 기반) ─────────────────────────────────────────────────
        List<AlbumSearchDto> albumDtosByTitle = albumRepository
                .findByAlbumTitleContainingIgnoreCase(keyword)
                .stream()
                .map(a -> new AlbumSearchDto(
                        a.getAlbumId(),
                        a.getAlbumArtistId(),
                        a.getAlbumTitle(),
                        a.getCoverImage(),
                        a.getReleaseDate(),
                        a.getAlbumGenre()
                ))
                .collect(Collectors.toList());

        // ── 3) “아티스트 → 앨범 전체” 추가 (제목 제외) ─────────────────────────────────
        List<AlbumSearchDto> albumDtosByArtist = matchedArtists.stream()
                .flatMap(member ->
                        albumRepository.findAllByAlbumArtistId(member.getMemberArtistSeq()).stream()
                                // 중복 제거: 이미 title 기반에서 포함된 앨범은 제외
                                .filter(albumEntity ->
                                        albumDtosByTitle.stream()
                                                .noneMatch(x -> x.getAlbumId().equals(albumEntity.getAlbumId()))
                                )
                                .map(albumEntity -> new AlbumSearchDto(
                                        albumEntity.getAlbumId(),
                                        albumEntity.getAlbumArtistId(),
                                        albumEntity.getAlbumTitle(),
                                        albumEntity.getCoverImage(),
                                        albumEntity.getReleaseDate(),
                                        albumEntity.getAlbumGenre()
                                ))
                )
                .collect(Collectors.toList());

        // 두 리스트를 합칩니다.
        List<AlbumSearchDto> combinedAlbumDtos = Stream
                .concat(albumDtosByTitle.stream(), albumDtosByArtist.stream())
                .collect(Collectors.toList());

        // ── 4) 트랙 검색 ────────────────────────────────────────────────────────────
        List<TrackSearchDto> trackDtos = trackRepository
                .findByTrackTitleContainingIgnoreCase(keyword)
                .stream()
                .map(t -> new TrackSearchDto(
                        t.getTrackId(),
                        t.getAlbumId(),
                        t.getTrackArtistId(),
                        t.getTrackTitle(),
                        t.getTrackFile()
                ))
                .collect(Collectors.toList());

        return new SearchResultDto(artistDtos, combinedAlbumDtos, trackDtos);
    }
}
