package com.deol.deolspring.config;

import com.deol.deolspring.jwt.JwtAccessDeniedHandler;
import com.deol.deolspring.jwt.JwtAuthenticationEntryPoint;
import com.deol.deolspring.jwt.JwtFilter;
import com.deol.deolspring.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenProvider tokenProvider;

    // 주입받은 JwtFilter 사용
    private final JwtFilter jwtFilter;

    /**
     * ✅ 공개 엔드포인트는 보안 필터 체인(= JwtFilter 포함) "밖"으로 빼서 401을 방지한다.
     *   - 여기 나열된 경로는 JwtFilter 자체를 타지 않음.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                // 공개 조회 API
                "/api/albums/**",
                "/api/tracks/**",
                "/api/chart/**",
                "/api/mainhome/**",
                "/api/search/**",

                // 가입/인증 등 공개 엔드포인트
                "/api/signup/**",
                "/api/check-id",
                "/api/email/**",
                "/api/find-id/**",
                "/api/find-password",
                "/api/change-password",
                "/api/login",
                "/upload/album",

                // 정적/문서/오류
                "/",
                "/index.html",
                "/static/**",
                "/assets/**",
                "/error",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/api-docs/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())

                // 예외 처리
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 세션 미사용 (JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인가 규칙 (여기엔 "필터 체인 안"으로 두는 경로만 남음)
                .authorizeHttpRequests(auth -> auth
                        // 프리플라이트는 항상 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // (필요시 추가 permitAll/권한 규칙—아래 예시는 네 기존 규칙 유지)
                        .requestMatchers("/api/playlists/**").permitAll()
                        .requestMatchers("/api/mypage/**").permitAll()
                        .requestMatchers("/api/follow/**").permitAll()
                        .requestMatchers("/api/user", "/api/user/*").hasAnyRole("ADMIN")

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 삽입
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS: 개발/배포 동시 허용(배포는 동일 오리진이라 실제로는 CORS가 거의 안 걸림)
     * 프로덕션에선 @Profile로 분리하는 걸 권장.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://deolstreaming.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
