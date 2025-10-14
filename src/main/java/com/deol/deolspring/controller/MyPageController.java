package com.deol.deolspring.controller;

import com.deol.deolspring.dto.MyPageDto;
import com.deol.deolspring.dto.NicknameDto;
import com.deol.deolspring.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage API", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageService myPageService;

    // 업로드한 앨범 조회
    @GetMapping("/uploaded/albums")
    @Operation(summary = "내가 업로드한 앨범 조회", description = "로그인한 아티스트가 업로드한 앨범 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드한 앨범 목록 반환 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getMyUploadedAlbums() {
        return ResponseEntity.ok(myPageService.getUploadedAlbums());
    }


    // 업로드한 플레이리스트 조회
    @GetMapping("/uploaded/playlists")
    @Operation(summary = "내가 업로드한 플레이리스트 조회", description = "로그인한 사용자가 업로드한 플레이리스트 목록을 반환합니다.")
    public ResponseEntity<?> getMyUploadedPlaylist() {
        return ResponseEntity.ok(myPageService.getMyPlaylists());
    }

    // 나의 팔로잉 목록 조회
    @GetMapping("/following")
    @Operation(summary = "내 팔로잉 아티스트 목록 조회", description = "로그인한 사용자가 팔로우한 아티스트 목록을 반환합니다.")
    public ResponseEntity<?> getMyFollowingArtists() {
        return ResponseEntity.ok(myPageService.getFollowing());
    }

    // 프로필 업로드 또는 수정
    @PostMapping(value = "/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 사진 업로드 또는 수정",
            description = """
        사용자의 프로필 사진을 업로드하거나 기존 사진이 있다면 새로운 사진으로 교체합니다.

        - 기존 프로필 사진이 **없는 경우**: 새로 업로드합니다.
        - 기존 프로필 사진이 **있는 경우**: 기존 파일을 S3에서 삭제한 뒤 새 파일로 교체합니다.
        - 응답으로는 업로드 또는 교체된 이미지의 S3 URL을 반환합니다.
    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 또는 수정 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 파일 누락"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<MyPageDto> uploadOrUpdateProfileImage(@RequestPart("file") MultipartFile file) {
        MyPageDto dto = myPageService.uploadOrUpdateProfileImage(file);
        return ResponseEntity.ok(dto);
    }

    // 프로필 사진 불러오기
    @GetMapping("/profile")
    @Operation(summary = "프로필 이미지 조회", description = "로그인한 사용자의 프로필 이미지 URL을 반환합니다.")
    public ResponseEntity<MyPageDto> getProfileImage() {
        return ResponseEntity.ok(myPageService.getProfileImage());
    }

    // 로그인한 사용자의 닉네임 조회
    @GetMapping("/nickname")
    @Operation(summary = "닉네임 조회", description = "로그인한 사용자의 닉네임(일반 또는 아티스트용)을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "닉네임 반환 성공")
    public ResponseEntity<NicknameDto> getMyNickname() {
        return ResponseEntity.ok(myPageService.getMyNickname());
    }

}
