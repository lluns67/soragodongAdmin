package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.UserDto;
import com.scit.soragodong.domain.entity.Users;
import com.scit.soragodong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    // 전체 유저 조회 및 DTO 변환

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setUserIdx(user.getUserIdx());
                    dto.setUserEmail(user.getUserEmail());
                    dto.setUserName(user.getUserName());
                    dto.setUserNickname(user.getUserNickname());
                    dto.setUserAddress(user.getUserAddress());
                    dto.setCreatedAt(user.getCreateAt());
                    dto.setMannerScore(user.getMannerScore());
                    dto.setWarningCount(user.getWarningCount());
                    dto.setIsUse(user.getIsUse());
                    dto.setPostCount(userRepository.countBoardsByUserIdx(user.getUserIdx())
                                    + userRepository.countUsedByUserIdx(user.getUserIdx()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 유저 상태(isUse) 토글 처리
    @Transactional
    public void updateUserStatus(Integer userIdx) {
        Users user = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. IDX: " + userIdx));
        user.setIsUse(!user.getIsUse()); // true -> false, false -> true
    }
}