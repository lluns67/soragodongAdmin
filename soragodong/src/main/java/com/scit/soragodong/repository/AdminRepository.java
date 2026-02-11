package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
	// ID로 관리자/점주 찾기 (로그인 시 사용)
	Optional<Admin> findByAdminId(String adminId);
}