package com.scit.soragodong.domain.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BoardReplyDto(
        Integer replyIdx,
        Integer boardIdx,
        Integer userIdx,
        String userNickname,
        String replyContent,
        Boolean isUse,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
