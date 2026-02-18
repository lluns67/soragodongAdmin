package com.scit.soragodong.service;

import com.scit.soragodong.domain.entity.Admin;
import com.scit.soragodong.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailService implements UserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + adminId));

        return User.builder()
                .username(admin.getAdminId())
                .password(admin.getAdminPassword()) // 주의: DB에 암호화되어 저장되어 있어야 함
                .roles(admin.getRole()) // "ADMIN" 또는 "OWNER"
                .build();
    }
}
