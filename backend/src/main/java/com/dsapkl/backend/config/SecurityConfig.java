package com.dsapkl.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스 접근 허용
                .requestMatchers(
                    "/css/**",
                    "/js/**", 
                    "/images/**",
                    "/assets/**"
                ).permitAll()
                // 공개 페이지 접근 허용
                .requestMatchers(
                    "/",
                    "/items",
                    "/items/{itemId}",
                    "/error",
                    "/members/new",  // 회원가입 페이지
                    "/members/add"   // 회원가입 처리
                ).permitAll()
                // 공개 API 엔드포인트 접근 허용
                .requestMatchers(
                    "/api/items/{itemId}/reviews",  // 리뷰 조회
                    "/api/items/{itemId}/rating",   // 상품 평점 조회
                    "/api/items/*/images"           // 상품 이미지 조회
                ).permitAll()
                // 그 외 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/members")
                .loginProcessingUrl("/members/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
} 