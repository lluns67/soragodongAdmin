package com.scit.soragodong.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 로그인한 사용자의 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (roles.contains("ROLE_ADMIN")) {
            // 전체 관리자일 경우
            response.sendRedirect("/admin/index2");
        } else if (roles.contains("ROLE_OWNER")) {
            // 점주일 경우 (상점 관리 페이지 등)
            response.sendRedirect("/owner/dashboard");
        } else {
            // 그 외 기본 페이지
            response.sendRedirect("/index");
        }
    }
}
