package com.deol.deolspring.service;

import com.deol.deolspring.dto.*;
import com.deol.deolspring.entity.Member;
import com.deol.deolspring.repository.AlbumRepository;
import com.deol.deolspring.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final  AlbumRepository albumRepository;
    private final MemberService memberService;
    private final PlaylistService playlistService;
    private final FollowService followService;
    private final S3Service s3Service;

    @Transactional
    public MyPageDto uploadOrUpdateProfileImage(MultipartFile file) {
        Integer memberSeq = memberService.getLoggedMemberSeq();
        Member member = memberRepository.findById(Integer.valueOf(memberSeq))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            String oldUrl = member.getProfileImageUrl();
            if (oldUrl != null && !oldUrl.isEmpty()) {
                String oldKey = s3Service.extractKeyFromUrl(oldUrl);
                s3Service.delete(oldKey); // 기존 이미지 삭제
            }

            String newImageUrl = s3Service.upload(file);
            member.setProfileImageUrl(newImageUrl); // DB 갱신
            memberRepository.save(member);
            return new MyPageDto(newImageUrl); // 새 URL 반환
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 수정 실패", e);
        }
    }

    // 업로드한 앨범 목록
    @Transactional
    public List<AlbumSummaryDto> getUploadedAlbums() {
        Integer artistId = memberService.getLoggedArtistSeq();

        return albumRepository.findByAlbumArtistId(artistId).stream()
                .map(album -> new AlbumSummaryDto(
                        album.getAlbumId(),
                        album.getAlbumTitle(),
                        album.getCoverImage(),
                        album.getTrackCount(),
                        album.getReleaseDate()
                ))
                .toList();
    }


    // 업로드한 플레이리스트 목록
    @Transactional
    public List<PlaylistLoadDto> getMyPlaylists() {
        return playlistService.getAllPlaylistsByUser(); // 재사용
    }

    // 팔로우한 아티스트 목록
    @Transactional
    public List<FollowDto> getFollowing() {
        return followService.getFollowDetails(); // 재사용
    }

    // 프로필 사진 불러오기
    @Transactional
    public MyPageDto getProfileImage() {
        Integer memberSeq = memberService.getLoggedMemberSeq();

        String imageUrl = memberRepository.findById(Integer.valueOf(memberSeq))
                .map(Member::getProfileImageUrl)
                .orElse(null);

        return new MyPageDto(imageUrl);
    }

    // 로그인한 사용자의 닉네임 조회
    @Transactional
    public NicknameDto getMyNickname() {
        Integer memberSeq = memberService.getLoggedMemberSeq();
        Member member = memberRepository.findById(Integer.valueOf(memberSeq))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 일반 유저일 경우 memberNickname, 아티스트일 경우 memberArtistName 반환
        String role = member.getRole();
        String nickname;

        if ("ROLE_ARTIST".equalsIgnoreCase(role)) {
            nickname = member.getMemberArtistName();
        } else {
            nickname = member.getMemberNickname();
        }

        return new NicknameDto(nickname);

    }



}

