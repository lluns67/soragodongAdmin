package com.scit.soragodong.config;

import com.scit.soragodong.handler.AdminLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AdminLoginSuccessHandler adminLoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {



        http
                .csrf(csrf -> csrf.disable()) // 테스트를 위해 CSRF 비활성화 (필요시 설정)
                .authorizeHttpRequests(auth -> auth
                        // 로그인 페이지, CSS, JS 등 정적 리소스는 누구나 접근 가능
                        .requestMatchers("/index", "/login", "/assets/**", "/css/**", "/js/**").permitAll()
                        // /admin/으로 시작하는 모든 경로는 인증(로그인)된 사용자만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // ADMIN만 접근 가능
                        .requestMatchers("/owner/**").hasRole("OWNER")  // OWNER만 접근 가능

                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")            // 커스텀 로그인 페이지 경로
                        .loginProcessingUrl("/login")   // 로그인 폼 action 경로
                        .usernameParameter("adminId")         // Admin 엔티티의 필드명과 일치
                        .passwordParameter("adminPassword")   // Admin 엔티티의 필드명과 일치
                        .successHandler(adminLoginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)         // HTTP 세션 무효화
                        .deleteCookies("JSESSIONID")         // 쿠키 삭제
                        .permitAll()
                );

        return http.build();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}