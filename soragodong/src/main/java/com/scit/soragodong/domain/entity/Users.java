package com.scit.soragodong.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_IDX")
    private Integer userIdx;

    @Column(name = "USER_EMAIL", length = 200, nullable = false)
    private String userEmail;

    @Column(name = "PASSWORD", length = 300, nullable = false)
    private String password;

    @Column(name = "USER_NAME", length = 50, nullable = false)
    private String userName;

    @Column(name = "USER_NICKNAME", length = 50, nullable = false)
    private String userNickname;

    @Column(name = "USER_ADDRESS", length = 500, nullable = false)
    private String userAddress;

    @Column(name = "CREATED_AT")
    private LocalDateTime createAt;



    @Column(name = "USER_BADGE", length = 50)
    private String userBadge;

    @Column(name = "PROFILE_IDX")
    private Integer profileIdx;

    @Column(name = "MANNER_SCORE")
    private Integer mannerScore;

    @Column(name = "MONTHLY_BUDGET")
    private Integer monthlyBudget;

    @Column(name = "USER_LAT", nullable = false)
    private Double userLat;

    @Column(name = "USER_LNG", nullable = false)
    private Double userLng;

    @Column(name = "IS_USE", nullable = false)
    private Boolean isUse;

    @Column(name = "WARNING_COUNT")
    private Integer warningCount;

}
