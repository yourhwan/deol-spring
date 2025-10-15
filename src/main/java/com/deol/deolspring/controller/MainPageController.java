package com.deol.deolspring.controller;

import com.deol.deolspring.dto.AlbumDto;
import com.deol.deolspring.dto.ArtistDto;
import com.deol.deolspring.service.AlbumService;
import com.deol.deolspring.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mainhome")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "MainPage API", description = "메인페이지 관련 API")
@PermitAll
public class MainPageController {

    private final MemberService memberService;
    private final AlbumService albumService;

    @GetMapping("/artists")
    @Operation(
            summary = "인기 아티스트 리스트",
            description = "최근 한달 이내 업로드된 앨범에 속한 트랙의 스트리밍 합계 기준 상위 아티스트를 반환합니다."
    )
    public List<ArtistDto> getPopularArtists(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return memberService.getPopularArtists(limit);
    }

    // 인기 앨범
    @GetMapping("/albums")
    @Operation(summary = "인기 앨범 리스트", description = "트랙의 스트리밍 합계 기준 상위 앨범을 반환합니다.")
    public List<AlbumDto> getPopularAlbums(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return albumService.getPopularAlbums(limit);
    }

    // 추천 차트




}
