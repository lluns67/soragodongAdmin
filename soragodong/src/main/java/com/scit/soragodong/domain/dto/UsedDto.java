package com.scit.soragodong.domain.dto;

import lombok.*;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedDto {
    private Integer usedIdx;
    private String usedTitle;
    private String usedContent;
    private Integer usedPrice;
    private String usedState;
    private String tradingLoc;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isUse;
    public Integer userIdx; // Users 엔티티 전체 대신 PK만 담도록 설계
    private Integer fileGrp;

    public UsedDto(Integer usedIdx, String usedTitle, String usedContent, Integer usedPrice, String usedState, String tradingLoc, Integer viewCount, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isUse, Integer userIdx) {
        this.usedIdx = usedIdx;
        this.usedTitle = usedTitle;
        this.usedContent = usedContent;
        this.usedPrice = usedPrice;
        this.usedState = usedState;
        this.tradingLoc = tradingLoc;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isUse = isUse;
        this.userIdx = userIdx;
    }
}