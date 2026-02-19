package com.scit.soragodong.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "NOTICE_IDX")
	private Integer noticeIdx;
	
	@Column(name = "TITLE")
	private String title;
	
	@Column(name = "CONTENT")
	private String content;
	
	// 파일 그룹과의 연관 관계 (ManyToOne 또는 OneToOne 선택 가능)
	@Column(name = "FILE_GRP_IDX")
	private Integer fileGrpIdx;
	
	@Column(name = "CREATE_AT")
	private LocalDateTime createAt;
	
	@Column(name = "UPDATE_AT")
	private LocalDateTime updateAt;
	
	@Column(name = "IS_USE", nullable = false)
	@Builder.Default // 빌더 쓸 때도 이 값이 기본으로 들어가게 해줌
	private Boolean isUse = true;
	
	@PrePersist
	public void prePersist() {
		this.createAt = LocalDateTime.now();
	}
}