package com.deol.deolspring.controller;

import com.deol.deolspring.dto.PlaylistAddDto;
import com.deol.deolspring.dto.PlaylistTrackBulkAddDto;
import com.deol.deolspring.dto.TrackChartDto;
import com.deol.deolspring.service.ChartService;
import com.deol.deolspring.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chart")
@RequiredArgsConstructor
@Tag(name = "Chart API", description = "차트 관련 API")
@PermitAll
public class ChartController {

    private final PlaylistService playlistService;
    private final ChartService chartService;

    @GetMapping("/top/streaming")
    @Operation(summary = "실시간 Top100 (24시간)", description = "최근 24시간 재생 수 기준 상위 100곡")
    public List<TrackChartDto> getRealtimeTop100(@RequestParam(defaultValue = "100") int limit) {
        int capped = Math.min(Math.max(limit, 1), 100); // 1~100만 허용
        return chartService.getRealtimeTopN(capped);
    }

    // ★ 신곡 TOP
    @GetMapping("/new-releases")
    @Operation(
            summary = "신곡 TOP (발매 30일 이내 + 최근 7일 재생)",
            description = "발매일이 최근 releaseDays(기본 30일) 이내이면서, 최근 lookbackDays(기본 7일) 재생수 상위"
    )
    public List<TrackChartDto> getNewReleasesTop(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "30")  int releaseDays,
            @RequestParam(defaultValue = "7")   int lookbackDays
    ) {
        // limit 캡핑 (1~100)
        int capped = Math.min(Math.max(limit, 1), 100);
        return chartService.getNewReleasesTop(capped, releaseDays, lookbackDays);
    }

    // 차트의 모든 트랙을 플레이리스트에 추가
    @Operation(summary = "차트 트랙 전체 추가", description = "차트의 전체 트랙을 추가합니다.")
    @ApiResponse(responseCode = "201", description = "전체 트랙 추가 성공")
    @ApiResponse(responseCode = "401", description = "전체 트랙 추가 실패")
    @PostMapping("/add/playlist/chart")
    public PlaylistAddDto addChartToPlaylist(@RequestBody PlaylistTrackBulkAddDto dto) {
        return playlistService.addTracksToPlaylist(dto.getPlaylistId(), dto.getTrackIds());
    }
}
