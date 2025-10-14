package com.deol.deolspring.controller;

import com.deol.deolspring.dto.*;
import com.deol.deolspring.entity.Follow;
import com.deol.deolspring.entity.Member;
import com.deol.deolspring.service.FollowService;
import com.deol.deolspring.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "회원 관련 API", description = "회원 가입, 로그인, 팔로잉 및 마이페이지 API")
public class MemberController {

    private final MemberService memberService;
    private final FollowService followService;

    @Operation(summary = "일반 유저 회원가입", description = "일반 유저를 회원가입합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "401", description = "회원가입 실패")
    @PostMapping("/signup/regular")
    public ResponseEntity<Member> signUpRegularMember(@RequestBody @Valid SignUpRegularDto signUpRegularDto) {
        Member newMember = memberService.signUpRegularMember(signUpRegularDto);
        return new ResponseEntity<>(newMember, HttpStatus.CREATED);
    }

    @Operation(summary = "아티스트 회원가입", description = "아티스트를 회원가입합니다.")
    @ApiResponse(responseCode = "201", description = "아티스트 회원가입 성공")
    @ApiResponse(responseCode = "401", description = "아티스트 회원가입 실패")
    @PostMapping("/signup/artist")
    public ResponseEntity<Member> signUpArtistMember(@RequestBody @Valid SignUpArtistDto signUpArtistDto) {
        Member newArtist = memberService.signUpArtist(signUpArtistDto);
        return new ResponseEntity<>(newArtist, HttpStatus.CREATED);
    }

    @Operation(summary = "로그인", description = "회원 로그인 및 JWT 토큰 생성")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "로그인 실패")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto) {

        try {
//            데이터를 전달하는 객체 (data transfer object)
//            사용자한테 받는 데이터가 null값이다. -->
            TokenDto tokenDto = memberService.authenticate(loginDto.getMemberId(), loginDto.getMemberPassword());
            return ResponseEntity.ok(tokenDto);
        } catch (RuntimeException e) {
            ErrorDto errorDto = new ErrorDto("로그인 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
        }
    }

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정보 조회 성공")
    @PostMapping("/user")
    public ResponseEntity<String> getMyUserInfo() {
        return ResponseEntity.ok("user");
    }

    @Operation(summary = "특정 유저 정보 조회", description = "특정 유저의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정보 조회 성공")
    @PostMapping("/user/{username}")
    public ResponseEntity<String> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok("admin");
    }

    @Operation(summary = "아이디 중복 확인", description = "아이디 중복 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "중복 확인 성공")
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkIdAvailability(@RequestParam String memberId) {
        boolean isAvailable = memberService.isIdAvailable(memberId);
        return ResponseEntity.ok(isAvailable);
    }

    @Operation(summary = "아이디 찾기", description = "이름과 이메일로 아이디를 찾습니다.")
    @ApiResponse(responseCode = "200", description = "아이디 찾기 성공")
    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody MemberDto memberDto) {
        try {
            String foundId = memberService.findId(memberDto.getMemberName(), memberDto.getMemberEmail());
            return ResponseEntity.ok().body(foundId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Operation(summary = "비밀번호 찾기", description = "이름, 아이디, 이메일로 비밀번호를 찾습니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 찾기 성공")
    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@RequestBody MemberDto memberDto) {
        try {
            Optional<Member> member = memberService.findPassword(memberDto.getMemberName(), memberDto.getMemberId(), memberDto.getMemberEmail());
            if (member.isPresent()) {
                return ResponseEntity.ok().body("User found");
            } else {
                return ResponseEntity.status(404).body("입력한 정보에 해당하는 유저를 찾을 수 없습니다.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공")
    @ApiResponse(responseCode = "400", description = "비밀번호 변경 실패")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid MemberDto memberDto) {
        try {
            memberService.changePassword(memberDto.getMemberId(), memberDto.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 로그인 된 유저의 회원유형 조회
    @GetMapping("/users/me/type")
    @Operation(summary = "로그인된 사용자 유형 조회", description = "현재 로그인된 사용자의 유형을 반환합니다. 반환 값에는 회원 유형(아티스트 여부)도 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "사용자 유형 반환 성공")
    @ApiResponse(responseCode = "401", description = "로그인되지 않음 또는 토큰이 유효하지 않음")
    public ResponseEntity<?> getLoggedInUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사를 제거하고 순수 토큰만 추출
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 서비스 호출
            UserResponseDto userResponse = memberService.getLoggedInUserInfo(token);
            // 반환 전 로그로 확인
            System.out.println("Returned UserResponseDto: " + userResponse);

            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    /////// 팔로우 기능 API ///////


    @PostMapping("/follow/add")
    @Operation(summary = "아티스트 팔로우", description = "로그인한 사용자가 해당 아티스트를 팔로우합니다.")
    public ResponseEntity<Void> followArtist(@RequestParam Integer artistId) {
        followService.followArtist(artistId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/follow/remove")
    @Operation(summary = "아티스트 언팔로우", description = "로그인한 사용자가 해당 아티스트 팔로우를 취소합니다.")
    public ResponseEntity<Void> unfollowArtist(@RequestParam Integer artistId) {
        followService.unfollowArtist(artistId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/follow/is_following")
    @Operation(summary = "팔로우 여부 확인", description = "로그인한 사용자가 해당 아티스트를 팔로우하고 있는지 확인합니다.")
    public ResponseEntity<Boolean> isFollowing(@RequestParam Integer artistId) {
        boolean result = followService.isFollowing(artistId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/follow/list")
    @Operation(summary = "팔로잉 목록 조회", description = "로그인한 사용자의 팔로잉 아티스트 목록을 조회합니다.")
    public ResponseEntity<List<Follow>> getFollowingArtists() {
        return ResponseEntity.ok(followService.getFollowingArtists());
    }

    @GetMapping("/follow/list/details")
    @Operation(summary = "팔로잉 아티스트 상세 목록", description = "팔로우한 아티스트의 이름과 프로필 이미지가 포함된 목록을 조회합니다.")
    public ResponseEntity<List<FollowDto>> getFollowingArtistDetails() {
        return ResponseEntity.ok(followService.getFollowDetails());
    }

    @GetMapping("/follow/count")
    @Operation(summary = "팔로워 수 조회", description = "특정 아티스트의 팔로워 수를 반환합니다.")
    public ResponseEntity<Long> getFollowerCount(@RequestParam Integer artistId) {
        return ResponseEntity.ok(followService.getFollowerCount(artistId));
    }

}
