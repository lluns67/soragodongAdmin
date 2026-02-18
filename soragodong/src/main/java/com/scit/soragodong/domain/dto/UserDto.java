package com.scit.soragodong.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDto {
    private Integer userIdx;
    private String userEmail;
    private String password;
    private String userName;
    private String userNickname;
    private String userAddress;

    private String userBadge;
    private Integer profileIdx;
    private Integer mannerScore;
    private Integer monthlyBudget;
    private Double userLat;
    private Double userLng;
    private Boolean isUse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer warningCount;
    private Integer postCount;
}