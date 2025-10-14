package com.deol.deolspring.controller;

import com.deol.deolspring.dto.ArtistDetailsDto;
import com.deol.deolspring.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "Artist API", description = "아티스트 관련 API")
@PermitAll
public class ArtistController {

    private final MemberService memberService;

    @GetMapping("/{artistSeq}/details")
    @Operation(summary = "아티스트 상세 페이지", description = "아티스트의 자세한 정보를 조회 가능합니다.")
    public ResponseEntity<ArtistDetailsDto> getArtistDetails(@PathVariable Integer artistSeq) {
        ArtistDetailsDto artistDetails = memberService.getArtistDetailsWithData(artistSeq);
        System.out.println("ArtistDetailsDto = "+artistDetails);
        return ResponseEntity.ok(artistDetails);
    }
}