package com.scit.soragodong.domain.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BoardDto(
        Integer boardIdx,
        Integer userIdx,
        Integer profileIdx,
        String boardCategory,
        String boardTitle,
        String boardContent,
        String userNickname,
        Boolean isUse,

        Integer likeCount,
        Integer viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Integer replyCount) {
}