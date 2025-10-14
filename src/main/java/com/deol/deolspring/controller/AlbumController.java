package com.deol.deolspring.controller;

import com.deol.deolspring.dto.AlbumDetailDto;
import com.deol.deolspring.dto.AlbumDto;
import com.deol.deolspring.dto.TrackDto;
import com.deol.deolspring.entity.Album;
import com.deol.deolspring.service.AlbumService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "Album API", description = "앨범 관련 API")
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping(value = "/upload/album", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "앨범 업로드", description = "아티스트가 앨범을 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "앨범 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청, 필수 데이터 누락 또는 형식 오류"),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인한 실패")
    })
    public Album uploadAlbum(
            @RequestPart("albumDto") AlbumDto albumDto,
            @RequestPart("coverImage") MultipartFile coverImage,
            @RequestPart(value = "trackInfo", required = false) String trackInfoJson,
            @RequestPart(value = "trackFiles", required = false) List<MultipartFile> trackFiles) {
        System.out.println("[DEBUG] AlbumController - 앨범 업로드 호출");
        System.out.println("[DEBUG] albumDto: " + albumDto);

        // trackInfoJson을 JSON 배열로 변환
        List<TrackDto> trackDtos = new ArrayList<>();
        if (trackInfoJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                trackDtos = List.of(mapper.readValue(trackInfoJson, TrackDto[].class));
                System.out.println("[DEBUG] trackDtos: " + trackDtos);
            } catch (Exception e) {
                System.err.println("[ERROR] trackInfo 처리 중 오류: " + e.getMessage());
                throw new RuntimeException("Invalid track information", e);
            }
        }

        // trackFiles와 trackDtos를 매핑
        if (trackFiles != null && trackDtos.size() == trackFiles.size()) {
            for (int i = 0; i < trackDtos.size(); i++) {
                trackDtos.get(i).setTrackFile(trackFiles.get(i));
                System.out.println("[DEBUG] 매핑된 TrackDto: " + trackDtos.get(i));
            }
        } else {
            System.err.println("[ERROR] trackFiles와 trackDtos의 크기가 다릅니다.");
        }

        Album album = albumService.uploadAlbum(albumDto, coverImage, trackDtos);
        System.out.println("[DEBUG] AlbumController - 앨범 업로드 완료: " + album);
        return album;
    }


    // 앨범 상세 페이지
    @GetMapping("/{albumId}/details")
    @Operation(summary = "앨범 상세 정보", description = "앨범 상세 정보와 수록곡을 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "앨범 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "앨범이 존재하지 않습니다."),
    })
    public AlbumDetailDto getAlbumDetails(@PathVariable Integer albumId) {
        System.out.println("앨범 api 정보 : "+ albumService.getAlbumDetails(albumId));

        return albumService.getAlbumDetails(albumId);
    }


    // 아티스트의 다른 앨범 가져오기
    @GetMapping("/artist/{artistId}/others")
    @Operation(summary = "디스코그래피", description = "아티스트의 다른 앨범들을 보여줍니다.")
    @PermitAll
    public List<AlbumDto> getOtherAlbums(
        @PathVariable Integer artistId,
        @RequestParam Integer excludeAlbumId // 제외할 앨범 ID
    ) {
            return albumService.getOtherAlbumsByArtist(artistId, excludeAlbumId);
    }

}
