package com.scit.soragodong.domain.dto;


public record ReportDto(
        int reportIdx,
        int reporterIdx,
        String targetType,
        Long targetId,
        String reason,
        String description,
        String status,
        String processNote

) {}

