package com.deol.deolspring.controller;

import com.deol.deolspring.service.ChartService;
import com.deol.deolspring.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
@Tag(name = "Track API", description = "트랙 관련 API")
@PermitAll
public class TrackController {

    private final TrackService trackService;
    private final ChartService chartService;

    /**
     * 재생 '종료' 시 1회 호출 (메인 차트 집계 기준)
     *  - stream_count +1 (누적)
     *  - tbl_track_play 로그 적재 (24h 차트용)
     *
     *  호환을 위해 /{id}/played 와 /{id}/play 둘 다 받습니다.
     *  (프론트가 예전 경로를 호출해도 동작)
     */
    @PostMapping({"/{trackId}/played", "/{trackId}/play"})
    @Operation(
            summary = "재생 종료 이벤트 기록",
            description = "재생이 끝난 트랙을 누적/로그에 반영합니다. (Top100용)"
    )
    public ResponseEntity<Void> onPlayed(
            @PathVariable Integer trackId,
            @AuthenticationPrincipal Object principal // 커스텀 principal 있으면 타입 바꿔서 memberSeq 꺼내세요.
    ) {
        Integer userId = null;
        // if (principal instanceof CustomUser cu) userId = cu.getMemberSeq();

        // 1) 누적 카운트 +1 (원한다면 유지, 아니면 주석 처리)
        trackService.incrementStreamCount(trackId);

        // 2) 실시간 차트용 로그 적재
        chartService.logPlay(trackId, userId, "web");

        return ResponseEntity.noContent().build();
    }
}