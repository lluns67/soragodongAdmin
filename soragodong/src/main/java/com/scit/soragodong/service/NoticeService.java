package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.FileRes;
import com.scit.soragodong.domain.dto.NoticeDto;
import com.scit.soragodong.domain.entity.Notice;
import com.scit.soragodong.domain.entity.Notification;
import com.scit.soragodong.domain.enums.FileRefType;
import com.scit.soragodong.repository.NoticeRepository;
import com.scit.soragodong.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoticeService {
	
	private final NoticeRepository noticeRepository;
	private final FileService fileService; // 기존 파일 서비스 활용
    private final NotificationRepository notificationRepository;
	
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

        //Notification DB에 저장용 USER_IDX 는 0으로 설정함
        Notification notification = Notification.builder()
                .userIdx(0L)
                .notiType("ADMIN_NOTICE")
                .refId(Long.valueOf(savedNotice.getNoticeIdx()))
                .message(dto.getTitle())
				.isRead(false)
				.createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);


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
		log.info("[삭제 요청] 공지사항 ID(refId): {}", noticeIdx);
		
		// 1. 공지사항 원본 삭제
		Notice notice = noticeRepository.findById(noticeIdx)
				.orElseThrow(() -> new IllegalArgumentException("공지사항 없음: " + noticeIdx));
		notice.setIsUse(false);
		noticeRepository.save(notice);
		
		// 2. 연결된 알림들 찾기 (NOTICE 타입으로 검색)
		// 팁: ADMIN_NOTICE 대신 실제 DB에 저장된 타입(NOTICE 등)을 사용하세요.
		List<Notification> notifications = notificationRepository.findByNotiTypeAndRefId(
				"ADMIN_NOTICE", Long.valueOf(noticeIdx));
		
		log.info("[조회 결과] 찾은 알림 개수: {}", notifications.size());
		
		if (!notifications.isEmpty()) {
			for (Notification notification : notifications) {
				notification.setIsUse(false);
				notificationRepository.save(notification);
				log.info("[알림 삭제 성공] 알림 PK: {}", notification.getNotiIdx());
			}
		} else {
			log.warn("[주의] 삭제할 알림을 찾지 못했습니다. refId와 타입을 확인하세요.");
		}
	}
}