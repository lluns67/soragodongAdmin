package com.scit.soragodong.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "USED")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Used {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USED_IDX")
    private Integer usedIdx;

    @Column(name = "USED_TITLE", length = 200, nullable = false)
    private String usedTitle;

    @Column(name = "USED_CONTENT", columnDefinition = "text", nullable = false)
    private String usedContent;

    @Column(name = "USED_PRICE", nullable = false)
    private Integer usedPrice;

    @Column(name = "USED_STATE", length = 20, nullable = false)
    private String usedState; // 예: 판매중, 예약중, 거래완료 등

    @Column(name = "TRADING_LOC", length = 200)
    private String tradingLoc;

    @Builder.Default
    @Column(name = "VIEW_COUNT", nullable = false)
    private Integer viewCount = 0;



    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "IS_USE", nullable = false)
    private Boolean isUse = true;

    // FK 매핑: USERS 테이블과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_USER", referencedColumnName = "USER_IDX")
    private Users user;

    // 편의 메서드
    public void delete() {
        this.isUse = false;
    }
}