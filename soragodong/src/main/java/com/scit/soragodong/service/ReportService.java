package com.scit.soragodong.service;

import com.scit.soragodong.domain.entity.Report;
import com.scit.soragodong.domain.enums.ReportStatus;
import com.scit.soragodong.repository.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RestTemplate restTemplate; // 본 서버 통신용

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
        Map<String, Object> payload = new HashMap<>();
        payload.put("userIdx", report.getReporterIdx());
        payload.put("message", String.format("[신고 결과] 제보하신 신고가 %s되었습니다. 사유: %s", resultText, processNote));

        try {
            String mainServerUrl = "http://localhost:8080/api/internal/broadcast";
            restTemplate.postForEntity(mainServerUrl, payload, String.class);
        } catch (Exception e) {
            log.error("SSE 알림 전송 실패: {}", e.getMessage());
        }
    }

}
