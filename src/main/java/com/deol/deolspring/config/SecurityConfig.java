package com.deol.deolspring.config;

import com.deol.deolspring.jwt.JwtAccessDeniedHandler;
import com.deol.deolspring.jwt.JwtAuthenticationEntryPoint;
import com.deol.deolspring.jwt.JwtFilter;
import com.deol.deolspring.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults()) // ✅ CORS 설정 활성화

                // Exception handling 설정
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // 세션 사용하지 않음(JWT 사용하므로 STATELESS 설정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 세션 사용하지 않음(JWT 사용하므로 STATELESS 설정)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/signup/home").permitAll()
                        .requestMatchers("/api/signup/regular").permitAll()
                        .requestMatchers("/api/signup/artist").permitAll()
                        .requestMatchers("/api/signup/success").permitAll()
                        .requestMatchers("/api/check-id").permitAll()
                        .requestMatchers("/api/email/check-email").permitAll()
                        .requestMatchers("/api/email/verify").permitAll()
                        .requestMatchers("/api/find-id").permitAll()
                        .requestMatchers("/api/find-id/result").permitAll()
                        .requestMatchers("/api/find-password").permitAll()
                        .requestMatchers("/api/change-password").permitAll()
                        .requestMatchers("/api/email/verify").permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/albums").permitAll()
                        .requestMatchers("/api/albums/**").permitAll()
                        .requestMatchers("/api/albums/upload").permitAll()
                        .requestMatchers("/upload/album").permitAll()
                        .requestMatchers("/api/mainhome/**").permitAll()
                        .requestMatchers("/api/mainhome").permitAll()
                        .requestMatchers("/api/authenticate").permitAll()
                        .requestMatchers("/api/artists").permitAll()
                        .requestMatchers("/api/artists/**").permitAll()
                        .requestMatchers("/api/tracks/**").permitAll()
                        .requestMatchers("/api/playlists/**").permitAll()
                        .requestMatchers("/api/chart").permitAll()
                        .requestMatchers("/api/mypage/**").permitAll()
                        .requestMatchers("/api/search").permitAll()
                        .requestMatchers("/api/search/**").permitAll()
                        .requestMatchers("/api/follow").permitAll()
                        .requestMatchers("/api/follow/**").permitAll()
                        .requestMatchers("/api/follow/add").permitAll()
                        .requestMatchers("/api/follow/remove").permitAll()
                        .requestMatchers("/api/follow/is_following").permitAll()
                        .requestMatchers("/api/follow/list").permitAll()
                        .requestMatchers("/api/follow/list/details").permitAll()
                        .requestMatchers("/api/follow/count").permitAll()
                        .requestMatchers(
                                "/api/chart/**",           // 차트 API 공개
                                "/api/tracks/*/play"       // 재생 로그 공개
                        ).permitAll()
                        // 정적/프론트 접근 허용(필요시)
                        .requestMatchers("/", "/index.html", "/static/**", "/assets/**").permitAll()

                        .requestMatchers("/api/user/*").hasAnyRole("ADMIN")
                        .requestMatchers("/api/user").hasAnyRole("ADMIN")
                        .requestMatchers(
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs/swagger-config/**",
                                "/swagger-ui.html",
                                "/swagger-resources",
                                "/swagger-resources/*",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/webjars/**",
                                "/swagger-ui/**").permitAll() // 스웨거 관련

                        .anyRequest().authenticated()) // 나머지 요청은 인증 필요

                // 폼 로그인 비활성화 (JWT 인증 방식 사용)
                .formLogin(AbstractHttpConfigurer::disable)

                // JWT 필터 추가
                .addFilterBefore(new JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 프론트 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 메서드
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 인증 정보 허용 (Authorization 헤더 포함 허용)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
