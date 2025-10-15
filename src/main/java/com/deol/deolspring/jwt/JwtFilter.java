package com.deol.deolspring.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;

    // ✅ 공개(우회) 엔드포인트 패턴들
    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/api/email/**",         // 인증 메일 전송/검증
            "/api/check-id",         // 기존 예외
            "/actuator/health",      // 헬스체크(원하면)
            "/v3/api-docs/**", "/swagger-ui/**" // (선택) 스웨거
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private boolean isPublic(HttpServletRequest req) {
        String uri = req.getRequestURI();
        for (String pattern : PUBLIC_PATTERNS) {
            if (PATH_MATCHER.match(pattern, uri)) return true;
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();

        // ✅ OPTIONS(프리플라이트) 또는 공개 엔드포인트면 필터 우회
        if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod()) || isPublic(httpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String jwt = resolveToken(httpServletRequest);

        // ✅ 토큰이 없으면 "아무 것도 하지 않고" 다음 필터로 넘김 (permitAll과 공존)
        if (!StringUtils.hasText(jwt)) {
            logger.debug("JWT 없음, uri: {}", requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // ✅ 토큰이 있으면 검증. 실패해도 401을 여기서 만들지 않음(조용히 패스)
        try {
            if (tokenProvider.validateToken(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("SecurityContext에 인증 저장: {}, uri: {}", authentication.getName(), requestURI);
            } else {
                SecurityContextHolder.clearContext();
                logger.debug("JWT 유효하지 않음, uri: {}", requestURI);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            logger.debug("JWT 검증 중 예외, uri: {}, msg: {}", requestURI, e.getMessage());
            // 예외를 던지지 않고 체인 계속 진행 → permitAll 엔드포인트는 통과됨
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
