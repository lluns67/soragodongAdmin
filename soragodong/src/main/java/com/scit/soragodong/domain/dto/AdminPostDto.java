package com.scit.soragodong.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class AdminPostDto {
    private Integer idx;
    private String type;
    private String category;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime date;
    private LocalDateTime updatedAt;
    private Integer views;
    private Boolean isUse;
    private Integer likeCount;

    // BoardDto를 받아 변환
    public static AdminPostDto fromBoard(BoardDto b) {
        AdminPostDto dto = new AdminPostDto();
        dto.setIdx(b.boardIdx());
        dto.setType("커뮤니티");
        dto.setCategory(b.boardCategory());
        dto.setTitle(b.boardTitle());
        dto.setContent(b.boardContent());
        dto.setWriter(b.userNickname());
        dto.setDate(b.createdAt());
        dto.setViews(b.viewCount());
        dto.setIsUse(b.isUse());
        dto.setLikeCount(b.likeCount());
        return dto;
    }

    // UsedDto를 받아 변환
    public static AdminPostDto fromUsed(UsedDto u, String nickname) {
        AdminPostDto dto = new AdminPostDto();
        dto.setIdx(u.usedIdx());
        dto.setType("중고거래");
        dto.setCategory(u.usedState());
        dto.setTitle(u.usedTitle());
        dto.setContent(u.usedContent());
        dto.setWriter(nickname);
        dto.setDate(u.createdAt());
        dto.setViews(u.viewCount());
        dto.setIsUse(u.isUse());
        return dto;
    }
}