package com.scit.soragodong.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
	
	private Integer noticeIdx;       // 공지사항 고유 번호
	private String title;            // 제목
	private String content;          // 내용
	private Integer fileGrpIdx;      // 첨부 파일 그룹 ID (참조용)
	private LocalDateTime createAt;  // 작성 시간
	private LocalDateTime updateAt;  // 수정 시간
	private Byte isUse;              // 논리 삭제 여부
	
}
