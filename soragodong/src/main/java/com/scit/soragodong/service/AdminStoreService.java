package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.OwnerStoreDto;
import com.scit.soragodong.domain.entity.Admin;
import com.scit.soragodong.domain.entity.Store;
import com.scit.soragodong.repository.AdminRepository;
import com.scit.soragodong.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AdminStoreService {
	
	private final AdminRepository adminRepository;
	private final StoreRepository storeRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	@Transactional
	public void registerNewStoreWithAccount(OwnerStoreDto dto) {
		// 1. 점주(OWNER) 계정 생성
		Admin newOwner = Admin.builder()
				.adminId(dto.ownerId())
				.adminPassword(passwordEncoder.encode(dto.ownerPw())) // 암호화 필수!
				.role("OWNER")
				.build();
		
		Admin savedOwner = adminRepository.save(newOwner);
		
		// 2. 점포 생성 및 점주 연결
		Store newStore = Store.builder()
				.storeName(dto.storeName())
				.storeAddress(dto.storeAddress())
				.storeOpenTime(LocalTime.now())
				.storeCloseTime(LocalTime.now())
				.owner(savedOwner) // 생성된 점주 객체 연결
				.build();
		
		storeRepository.save(newStore);
	}
}
