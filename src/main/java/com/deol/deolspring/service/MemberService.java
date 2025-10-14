    package com.deol.deolspring.service;

    import com.deol.deolspring.dto.*;
    import com.deol.deolspring.entity.Authority;
    import com.deol.deolspring.entity.Member;
    import com.deol.deolspring.jwt.TokenProvider;
    import com.deol.deolspring.repository.*;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.Collections;
    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class MemberService {

        private final MemberRepository memberRepository;
        private final PasswordEncoder passwordEncoder;
        private final TokenProvider tokenProvider;
        private final AuthorityRepository authorityRepository;
        private final AlbumRepository albumRepository;
        private final TrackRepository trackRepository;



        // 일반 회원가입
        public Member signUpRegularMember(SignUpRegularDto signUpRegularDto) {

            // ROLE_USERS 권한이 AuthorityEntity에 존재하지 않으면 생성
            Authority authority = authorityRepository.findByAuthorityName("ROLE_USERS")
                    .orElseGet(() -> authorityRepository.save(
                            Authority.builder()
                                    .authorityName("ROLE_USERS")
                                    .build()
                    ));

            Member member = Member.builder()
                    .memberId(signUpRegularDto.getMemberId())
                    .memberPassword(passwordEncoder.encode(signUpRegularDto.getMemberPassword()))
                    .memberName(signUpRegularDto.getMemberName())
                    .memberNickname(signUpRegularDto.getMemberNickname())
                    .memberEmail(signUpRegularDto.getMemberEmail())
                    .memberGender(signUpRegularDto.getMemberGender())
                    .memberBirthdate(signUpRegularDto.getMemberBirthdate())
                    .memberSeq(signUpRegularDto.getMemberSeq()) // 회원 고유번호 부여
                    .role(authority.getAuthorityName())  // 기본 역할 설정
                    .createDate(LocalDateTime.now())
                    .build();

            return memberRepository.save(member);
        }


        // 아티스트 회원가입
        public Member signUpArtist(SignUpArtistDto signUpArtistDto) {

            Integer memberArtistSeq = generateMemberArtistSeq(); // 아티스트 시퀀스 생성

            // ROLE_ARTIST 권한이 AuthorityEntity에 존재하지 않으면 생성
            Authority authority = authorityRepository.findByAuthorityName("ROLE_ARTIST")
                    .orElseGet(() -> authorityRepository.save(
                            Authority.builder()
                                    .authorityName("ROLE_ARTIST")
                                    .build()
                    ));

            Member member = Member.builder()
                    .memberId(signUpArtistDto.getMemberId())
                    .memberPassword(passwordEncoder.encode(signUpArtistDto.getMemberPassword()))
                    .memberName(signUpArtistDto.getMemberName())
                    .memberArtistName(signUpArtistDto.getArtistName())
                    .memberEmail(signUpArtistDto.getMemberEmail())
                    .memberGender(signUpArtistDto.getMemberGender())
                    .memberBirthdate(signUpArtistDto.getMemberBirthdate())
                    .memberArtistSeq(memberArtistSeq) // 아티스트 고유번호 부여
                    .memberSeq(signUpArtistDto.getMemberSeq()) // 회원 고유번호 부여
                    .role(authority.getAuthorityName())  // 아티스트 역할 설정
                    .createDate(LocalDateTime.now())
                    .build();

            return memberRepository.save(member);
        }


        // 로그인 시 아이디 및 비밀번호 여부 확인
        public TokenDto authenticate(String memberId, String memberPassword) {
            // 사용자 ID로 멤버 엔티티 조회
            System.out.println(memberId);
            Optional<Member> memberEntityOpt = memberRepository.findByMemberId(memberId);

            if (!memberEntityOpt.isPresent()) {
                // 아이디가 존재하지 않을 경우
                throw new RuntimeException("존재하지 않는 아이디입니다.");
            }

            Member member = memberEntityOpt.get();

            // 비밀번호 검증
            if (!passwordEncoder.matches(memberPassword, member.getMemberPassword())) {
                // 비밀번호가 잘못된 경우
                throw new RuntimeException("비밀번호가 잘못되었습니다.");
            }

            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(member.getRole())
            );

            // Authentication 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    member.getMemberId(),
                    null,
                    authorities
            );

            // Access Token과 Refresh Token 생성
            String accessToken = tokenProvider.createToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);

            return new TokenDto(accessToken, refreshToken);
        }

        // 아티스트 넘버 생성
        private Integer generateMemberArtistSeq() {
            // 최대값을 읽고 +1 하여 아티스트 시퀀스를 생성
            Integer maxArtistSeq = memberRepository.findMaxArtistNo(); // 최대 아티스트 시퀀스 조회
            System.out.println("Max Artist Sequence: " + maxArtistSeq); // 로그 추가
            return (maxArtistSeq != null ? maxArtistSeq + 1 : 1); // 없으면 1로 시작
        }

        // 아이디 중복 확인 메서드
        public boolean isIdAvailable(String memberId) {
            boolean exists = memberRepository.existsByMemberId(memberId);
            System.out.println("Member ID = " + memberId + ", exist = " + exists);
            return !exists;
        }

        // 유저 아이디 찾기 메서드
        public String findId(String memberName, String memberEmail) {
            Optional<Member> member = memberRepository.findByMemberNameAndMemberEmail(memberName, memberEmail);
            if (member.isPresent()) {
                return member.get().getMemberId(); // 아이디 반환
            } else {
                throw new RuntimeException("입력한 정보에 해당하는 아이디를 찾을 수 없습니다.");
            }
        }

        // 유저 비밀번호 찾기 및 변경
        public Optional<Member> findPassword(String memberName, String memberId, String memberEmail) {
            return memberRepository.findByMemberNameAndMemberIdAndMemberEmail(memberName, memberId, memberEmail);
        }

        // 비밀번호 변경 메서드
        public void changePassword(String memberId, String newPassword) {
            Optional<Member> member = memberRepository.findByMemberId(memberId);

            if (member.isPresent()) {
                Member memberEntity = member.get();
                memberEntity.setMemberPassword(passwordEncoder.encode(newPassword)); // 새 비밀번호를 암호화하여 저장
                memberRepository.save(memberEntity); // 변경된 엔티티를 저장
            } else {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }
        }

        // 로그인한 아티스트 회원의 아티스트 시퀀스 가져오는 메서드
        public Integer getLoggedArtistSeq() {

            // 여기에서 현재 로그인한 사용자 정보를 가져오는 로직 구현
            // 예: SecurityContextHolder에서 사용자 정보를 가져오거나, JWT 토큰에서 아티스트 시퀀스 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String memberId = authentication.getName(); // 현재 로그인한 사용자 ID
                System.out.println("memberId: "+memberId+"authentication: "+authentication);
                Member member = memberRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                return member.getMemberArtistSeq(); // member_artist_seq 반환
            }
            throw new RuntimeException("로그인된 사용자가 없습니다.");

        }

        // 로그인한 회원의 멤버 시퀀스 가져오는 메서드
        public Integer getLoggedMemberSeq() {

            // 여기에서 현재 로그인한 사용자 정보를 가져오는 로직 구현
            // 예: SecurityContextHolder에서 사용자 정보를 가져오거나, JWT 토큰에서 member_seq 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String memberId = authentication.getName(); // 현재 로그인한 사용자 ID
                System.out.println("memberId: "+memberId+"authentication: "+authentication);
                Member member = memberRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                return member.getMemberSeq(); // member_seq 반환
            }
            throw new RuntimeException("로그인된 사용자가 없습니다.");

        }

        // 앨범 업로드 페이지 접근을 위한 로그인한 유저의 정보 확인 (아티스트인지 아닌지)
        public UserResponseDto getLoggedInUserInfo(String token) {
            // 토큰에서 인증 정보 추출
            Authentication authentication = tokenProvider.getAuthentication(token);
            String username = authentication.getName();

            // 사용자 정보 조회
            Member member = memberRepository.findByMemberId(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // role 필드를 활용하여 아티스트 여부를 확인
            boolean isArtist = "ROLE_ARTIST".equals(member.getRole());

            // Member 정보를 UserResponseDto로 변환하여 반환
            return new UserResponseDto(
                    member.getMemberName(),             // memberName
                    member.getMemberNickname(),         // memberNickname
                    member.getMemberArtistName(),       // memberArtistName
                    member.getMemberSeq(),              // memberSeq
                    member.getMemberArtistSeq(),        // memberArtistSeq
                    isArtist                            // isArtist
            );
        }

        // 인기 아티스트 리스트 반환
        // 기본: 상위 10명
        public List<ArtistDto> getPopularArtists() {
            return getPopularArtists(10);
        }

        // 필요 시 개수 지정
        public List<ArtistDto> getPopularArtists(int limit) {
            // “최근 한 달”을 30일로 해석 (달 단위 원하면 minusMonths(1)로 교체)
            LocalDate end   = LocalDate.now();
            LocalDate start = end.minusDays(30);

            List<PopularArtistProjection> rows =
                    memberRepository.findPopularArtistsInWindow(start, end, PageRequest.of(0, Math.max(1, limit)));

            return rows.stream()
                    .map(p -> new ArtistDto(
                            p.getMemberArtistName(),
                            p.getMemberArtistSeq(),
                            p.getProfileImageUrl()
                    ))
                    .collect(Collectors.toList());
        }

        // 아티스트 상세 정보를 가져오는 메서드
        public Member getArtistDetails(Integer artistSeq) {
            return memberRepository.findByArtistSeq(artistSeq)
                    .orElseThrow(() -> new RuntimeException("해당 아티스트를 찾을 수 없습니다."));
        }

        // 아티스트와 연관된 데이터들을 가져오기
        public ArtistDetailsDto getArtistDetailsWithData(Integer artistSeq) {
            Member artist = getArtistDetails(artistSeq);

            // 기본 프로필 이미지 처리
            if (artist.getProfileImageUrl() == null || artist.getProfileImageUrl().isEmpty()) {
                artist.setProfileImageUrl("https://search.pstatic.net/common?type=b&size=3000&quality=100&direct=true&src=http%3A%2F%2Fsstatic.naver.net%2Fpeople%2FprofileImg%2Fd0306c5a-187c-424f-bf51-9eeb2085c838.jpg");
            }

            // 최신 앨범 정보 가져오기
            List<AlbumSummaryDto> latestAlbumList = albumRepository.findByAlbumArtistIdOrderByReleaseDateDesc(artistSeq);
            AlbumSummaryDto latestAlbum = latestAlbumList.isEmpty() ? null : latestAlbumList.get(0);

            // 전체 앨범 정보 가져오기
            List<AlbumSummaryDto> allAlbums = albumRepository.findByAlbumArtistId(artistSeq);

            // 인기 곡 정보 가져오기
            List<TrackPopularDto> topTracks = trackRepository.findTop3ByTrackArtistIdOrderByStreamCountDesc(artistSeq)
                    .stream()
                    .map(TrackPopularDto::fromEntity)
                    .toList();

            // DTO 생성 및 반환
            return ArtistDetailsDto.builder()
                    .artist(ArtistBasicInfoDto.fromEntity(artist)) // 아티스트 기본 정보
                    .latestAlbum(latestAlbum) // 최신 앨범
                    .allAlbums(allAlbums) // 모든 앨범
                    .topTracks(topTracks) // 인기 곡
                    .build();
        }


        ////////////// 팔로잉 관련 메서드 ////////////////




    }