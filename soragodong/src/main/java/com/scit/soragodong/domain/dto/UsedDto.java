package com.scit.soragodong.domain.dto;


import java.time.LocalDateTime;


public record UsedDto(
        Integer usedIdx,
        String usedTitle,
        String usedContent,
        Integer usedPrice,
        String usedState,
        String tradingLoc,
        Integer viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isUse,
        Integer userIdx // Users 엔티티 전체 대신 PK만 담도록 설계
) {}

