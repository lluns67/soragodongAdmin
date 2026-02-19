package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.FileRes;
import com.scit.soragodong.domain.dto.NoticeDto;
import com.scit.soragodong.domain.entity.Notice;
import com.scit.soragodong.domain.enums.FileRefType;
import com.scit.soragodong.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {
	
	private final NoticeRepository noticeRepository;
	private final FileService fileService; // 기존 파일 서비스 활용
	
	/**
	 * 공지사항 등록
	 */
	@Transactional
	public void writeNotice(NoticeDto dto, List<MultipartFile> files) {
		// 1. 엔티티 생성
		Notice notice = Notice.builder()
				.title(dto.getTitle())
				.content(dto.getContent())
				.build();
		
		// save를 먼저 해야 DB에서 noticeIdx(Auto Increment)가 생성됩니다.
		Notice savedNotice = noticeRepository.save(notice);
		
		// 2. 파일이 있으면 업로드 진행
		if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
			// FileRefType에 NOTICE가 없다면 추가 필요
			List<FileRes> uploaded = fileService.upload(
					FileRefType.NOTICE,
					notice.getNoticeIdx(),
					files
			);
			
		}
		
		
	}
	
	
	
	/**
	 * 공지사항 기본 정보 저장 (ID 확보용)
	 */
	public Notice saveNotice(NoticeDto noticeDto) {
		// DTO를 엔티티로 변환
		Notice notice = Notice.builder()
				.title(noticeDto.getTitle())
				.content(noticeDto.getContent())
				.build();
		
		// DB에 저장 후, Auto Increment로 생성된 ID가 포함된 엔티티 반환
		return noticeRepository.save(notice);
	}
	
	public List<Notice> getActiveNotices() {
		// 1. IS_USE 값이 1인 공지사항만 최신순으로 조회합니다.
		// 상수는 (byte) 1 로 캐스팅하여 전달합니다.
		return noticeRepository.findByIsUseTrueOrderByCreateAtDesc();
	}
	
	@Transactional
	public void deleteNotice(Integer noticeIdx) {
		Notice notice = noticeRepository.findById(noticeIdx)
				.orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));
		
		// 논리 삭제 처리
		notice.setIsUse(false);
	}
}