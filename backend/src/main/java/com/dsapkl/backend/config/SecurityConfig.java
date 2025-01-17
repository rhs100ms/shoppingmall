//package com.dsapkl.backend.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/admin/**").hasRole("ADMIN") // ADMIN만 접근 가능
//                        .requestMatchers("/user/**").hasRole("USER")   // USER만 접근 가능
//                        .anyRequest().authenticated()                // 그 외는 인증 필요
//                )
//                .formLogin(login -> login
//                        .loginPage("/login") // 커스텀 로그인 페이지 경로
//                        .defaultSuccessUrl("/default") // 로그인 성공 후 이동할 경로
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login")
//                        .permitAll()
//                );
//        return http.build();
//    }
//
////    @Bean
////    public UserDetailsService userDetailsService() {
////        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
////        manager.createUser(User.withUsername("admin")
////                .password("{noop}admin123") // {noop}은 비밀번호 암호화를 비활성화
////                .roles("ADMIN")
////                .build());
////        manager.createUser(User.withUsername("user")
////                .password("{noop}user123")
////                .roles("USER")
////                .build());
////        return manager;
////    }
//}
