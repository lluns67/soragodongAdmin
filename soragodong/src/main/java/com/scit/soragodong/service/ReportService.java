package com.scit.soragodong.service;

import com.scit.soragodong.domain.entity.Report;
import com.scit.soragodong.domain.enums.ReportStatus;
import com.scit.soragodong.repository.BoardRepository;
import com.scit.soragodong.repository.ReportRepository;
import com.scit.soragodong.repository.UsedRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final UsedRepository usedRepository;
    private final RestTemplate restTemplate; // 본 서버 통신용
	
	@Value("${external.main-server.url}")
	private String mainServerUrl;
	
    /**
     * 전체 신고 목록 조회 (최신순)
     */
    public List<Report> getAllReports() {

        List<Report> reports = reportRepository.findAll();
        return reports.stream()
                .sorted((r1, r2) -> {
                    // 1. PROCESSING 상태를 최상단으로 (0순위)
                    int r1Priority = "PROCESSING".equals(r1.getStatus()) ? 0 : 1;
                    int r2Priority = "PROCESSING".equals(r2.getStatus()) ? 0 : 1;

                    if (r1Priority != r2Priority) {
                        return Integer.compare(r1Priority, r2Priority);
                    }

                    // 2. 같은 상태 내에서는 최신순 정렬 (BaseEntity의 createdAt 활용)
                    if (r1.getCreatedAt() == null || r2.getCreatedAt() == null) return 0;
                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .toList();
    }

    /**
     * 신고 처리 및 실시간 알림 발송
     */
    public void processReport(Integer reportIdx, ReportStatus status, String processNote) {
        // 1. 신고 데이터 상태 업데이트
        Report report = reportRepository.findById(Long.valueOf(reportIdx))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));

        report.setStatus(status.name()); // Enum 값을 문자열로 저장
        report.setProcessNote(processNote);

        // 2. 승인 또는 반려 시 본 서버 실시간 알림 (SSE) 전송
        String resultText = (status == ReportStatus.APPROVED) ? "승인" : "반려";

        // 신고자에게 알림 전송
        String reporterMsg = String.format("[신고 결과] 제보하신 신고가 %s되었습니다. 사유: %s", resultText, processNote);
        sendSseNotification(report.getReporterIdx(), reporterMsg);

        // 피신고자에게 알림 전송
        Integer targetUserIdx = getTargetUserIdx(report);
        if (targetUserIdx != null) {
            String targetMsg = (status == ReportStatus.APPROVED)
                    ? String.format("[신고 안내] 귀하의 게시물이 신고되어 %s 처리되었습니다. 사유: %s", resultText, processNote)
                    : String.format("[신고 안내] 귀하의 게시물에 대한 신고가 검토 결과 %s되었습니다.", resultText);
            sendSseNotification(targetUserIdx, targetMsg);
        }
    }

    /**
     * 신고 대상 게시물의 작성자 ID 조회
     */
    private Integer getTargetUserIdx(Report report) {
        String type = report.getTargetType();
        Long id = report.getTargetId();
        if (id == null) return null;

        try {
            if ("BOARD".equalsIgnoreCase(type)) {
                return boardRepository.findById(id.intValue())
                        .map(board -> board.getUser().getUserIdx())
                        .orElse(null);
            } else if ("USED_ITEM".equalsIgnoreCase(type) || "USED".equalsIgnoreCase(type)) {
                return usedRepository.findById(id.intValue())
                        .map(used -> used.getUser().getUserIdx())
                        .orElse(null);
            }
        } catch (Exception e) {
            log.error("피신고자 ID 조회 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * SSE 알림 전송 공통 메서드
     */
    private void sendSseNotification(Integer userIdx, String message) {
		
		String fullUrl = mainServerUrl + "/api/internal/broadcast";
		
		if (userIdx == null) return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("userIdx", userIdx);
        payload.put("message", message);

        try {
            
            restTemplate.postForEntity(fullUrl, payload, String.class);
        } catch (Exception e) {
            log.error("SSE 알림 전송 실패 (userIdx: {}): {}", userIdx, e.getMessage());
        }
    }
	
	public long countProcessingReports() {
		return reportRepository.countByStatus("PROCESSING");
	}
}
