package com.scit.soragodong.controller;

import com.scit.soragodong.domain.dto.NoticeDto;
import com.scit.soragodong.domain.entity.File;
import com.scit.soragodong.domain.entity.Notice;
import com.scit.soragodong.repository.FileRepository;
import com.scit.soragodong.repository.NoticeRepository;
import com.scit.soragodong.service.FileService;
import com.scit.soragodong.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
@Slf4j
public class AdminNoticeController {
	
	private final NoticeService noticeService;
	private final FileService fileService;
	private final NoticeRepository noticeRepository;
	private final FileRepository fileRepository;
	
	@PostMapping("/write")
	public String writeNotice(NoticeDto noticeDto,
							  @RequestParam(value = "noticeFiles", required = false) List<MultipartFile> files,
							  RedirectAttributes rttr) {
		try {
			
			// 서비스에서 저장과 파일 업로드를 한 번에 처리하도록 호출
			noticeService.writeNotice(noticeDto, files);
			rttr.addFlashAttribute("message", "공지사항이 등록되었습니다.");
		} catch (Exception e){
			rttr.addFlashAttribute("error", "공지사항 등록 실패: " + e.getMessage());
			
		}
		// 알림 내역 페이지로 리다이렉트
		return "redirect:/";
	}
	@GetMapping("/api/{noticeIdx}")
	@ResponseBody
	public ResponseEntity<?> getNoticeDetail(@PathVariable Integer noticeIdx) {
		try {
			// 1. 공지사항 본문 조회
			Notice notice = noticeRepository.findById(noticeIdx)
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
			
			// 2. 해당 공지사항에 연결된 파일 목록 조회 (FileRefType.NOTICE와 noticeIdx 기준)
			// fileRepository를 통해 직접 조회하거나 FileService에 메서드 추가 필요
			List<File> files = fileRepository.findByFileGroup_RefTypeAndFileGroup_RefId(com.scit.soragodong.domain.enums.FileRefType.NOTICE, noticeIdx);
			
			// 3. 응답용 데이터 구성 (Map 활용)
			Map<String, Object> response = new HashMap<>();
			response.put("title", notice.getTitle());
			response.put("content", notice.getContent());
			
			// 파일 목록에서 필요한 정보만 추출하여 전달 (순환 참조 방지) 쉽지 않더라
			List<Map<String, Object>> fileInfoList = files.stream().map(f -> {
				Map<String, Object> fMap = new HashMap<>();
				fMap.put("fileIdx", f.getFileIdx()); // HTML에서 /img/${fileIdx}로 사용
				return fMap;
			}).collect(Collectors.toList());
			
			response.put("files", fileInfoList);
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	@DeleteMapping("/api/{noticeIdx}")
	@ResponseBody
	public ResponseEntity<String> deleteNotice(@PathVariable Integer noticeIdx) {
		try {
			noticeService.deleteNotice(noticeIdx);
			return ResponseEntity.ok("success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}