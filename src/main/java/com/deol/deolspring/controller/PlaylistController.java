package com.deol.deolspring.controller;

import com.deol.deolspring.dto.*;
import com.deol.deolspring.service.MemberService;
import com.deol.deolspring.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlist API", description = "플레이리스트 API")
@PermitAll
public class PlaylistController {

    private final PlaylistService playlistService;
    private final MemberService memberService;

    // 플레이리스트 생성 v
    @PostMapping(value = "/create/playlist", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "플레이리스트 생성", description = "플레이리스트를 생성합니다.")
    public PlaylistDto createPlaylist(
            @RequestPart("playlistDto") PlaylistDto playlistDto,  // 플레이리스트 정보 (JSON)
            @RequestPart("playlistCover") MultipartFile playlistCover  // 커버 이미지 파일
    ) throws IOException {
        // 플레이리스트 생성 메서드 호출
        return playlistService.createPlaylist(playlistDto, playlistCover);
    }





    // 플레이리스트 삭제 v
    @DeleteMapping("/delete/playlist/{playlistId}")
    @Operation(summary = "플레이리스트 삭제", description = "플레이리스트를 삭제합니다.")
    public void deletePlaylist(@PathVariable Integer playlistId) {
        // 서비스에서 자동으로 memberSeq를 처리하도록 하고, 요청 본문에서 필요한 값들을 가져옵니다.
        playlistService.deletePlaylist(playlistId);  // 서비스에서 memberSeq를 자동으로 처리함
    }




    // 플레이리스트에 트랙 추가 v
    @PostMapping("/add/playlist/track")
    @Operation(summary = "플레이리스트 트랙 추가", description = "플레이리스트에 특정 트랙을 추가합니다.")
    public PlaylistAddDto addTrackToPlaylist(@RequestBody PlaylistTrackDto playlistTrackDto) {
        // 서비스 호출하여 트랙을 추가
        return playlistService.addTrackToPlaylist(playlistTrackDto.getPlaylistId(), playlistTrackDto.getTrackId());
    }





    // 앨범의 모든 트랙을 플레이리스트에 추가
    @PostMapping("/add/playlist/album")
    @Operation(summary = "앨범의 모든 트랙을 플레이리스트에 추가", description = "앨범에 포함된 모든 트랙을 선택한 플레이리스트에 추가합니다.")
    public PlaylistAddDto addAlbumTracksToPlaylist(@RequestBody PlaylistAlbumTracksDto dto) {
        return playlistService.addAllAlbumTracksToPlaylist(dto.getPlaylistId(), dto.getAlbumId());
    }




    // 특정 플레이리스트에서 특정 트랙 삭제 v
    @DeleteMapping("/remove/playlist/track")
    @Operation(summary = "플레이리스트 트랙 삭제", description = "플레이리스트에서 특정 트랙을 삭제합니다.")
    public PlaylistAddDto removeTrackFromPlaylist(@RequestBody PlaylistTrackDeleteDto deleteDto) {
        return playlistService.removeTrackFromPlaylist(deleteDto.getPlaylistTrackId());
    }





    // 특정 유저의 모든 플레이리스트 조회 v
    @GetMapping("/user/all_playlists")
    @Operation(summary = "유저의 모든 플레이리스트 조회", description = "유저의 모든 플레이리스트를 조회합니다.")
    public List<PlaylistLoadDto> getAllPlaylistsByUser() {
        return playlistService.getAllPlaylistsByUser();  // 로그인된 유저의 플레이리스트를 조회
    }





    // 특정 플레이리스트의 트랙 목록 조회 v
    @GetMapping("/user/{memberSeq}/playlist/{playlistId}")
    @Operation(summary = "유저의 특정 플레이리스트 조회", description = "유저의 특정 플레이리스트를 조회합니다.")
    public PlaylistAddDto getPlaylistByUserAndId(
            @PathVariable Integer playlistId  // 플레이리스트 ID
    ) {
        // 서비스 호출하여 특정 플레이리스트 조회
        return playlistService.getPlaylistByUserAndId(playlistId);
    }





    // 현재 재생목록 생성 v
    @PostMapping("/create/current")
    @Operation(summary = "현재 재생목록 생성", description = "유저의 현재 재생목록을 생성합니다.")
    public CurrentPlaylistDto createCurrentPlaylist() {
        return playlistService.createCurrentPlaylist();  // ✅ DTO 인자 제거
    }





    // 현재 재생목록에 트랙 추가 v
    @PostMapping("/add/current/track")
    @Operation(summary = "현재 재생목록에 트랙 추가", description = "현재 재생목록에 트랙을 추가합니다.")
    public CurrentPlaylistDto addTrackToCurrentPlaylist(@RequestBody CurrentTrackDto currentTrackDto) {
        return playlistService.addTrackToCurrentPlaylist(currentTrackDto.getCurrentPlaylistId(), currentTrackDto.getTrackId());
    }





    // 현재 재생목록에서 트랙 삭제 v
    @DeleteMapping("/remove/current/tracks")
    @Operation(summary = "현재 재생목록에서 트랙 삭제", description = "현재 재생목록에서 특정 트랙을 삭제합니다.")
    public CurrentPlaylistDto removeTrackFromCurrentPlaylist(@RequestBody CurrentPlaylistTrackDeleteDto dto) {
        return playlistService.removeTrackFromCurrentPlaylist(dto.getCurrentPlaylistId(), dto.getCurrentPlaylistTrackId());
    }





    // 현재 재생목록 조회 v
    @GetMapping("/current/{currentPlaylistId}/tracks")
    @Operation(summary = "유저의 현재 재생목록 조회", description = "유저의 현재 재생목록을 조회합니다.")
    public CurrentPlaylistDto getCurrentPlaylistTracks(@PathVariable Integer currentPlaylistId) {
        return playlistService.getCurrentPlaylistTracks(currentPlaylistId);
    }





    // 현재 유저의 currentPlaylistId를 안전하게 반환하거나 새로 생성
    @GetMapping("/current")
    @Operation(summary = "현재 유저의 재생목록 ID 반환 또는 생성", description = "로그인된 유저의 현재 재생목록 ID를 반환하거나 없으면 생성합니다.")
    public CurrentPlaylistDto getOrCreateCurrentPlaylist() {
        return playlistService.createCurrentPlaylist();
    }



}


