package com.scit.soragodong.controller;

import com.scit.soragodong.domain.enums.ReportStatus;
import com.scit.soragodong.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {

    private final ReportService reportService;

    // 신고 목록 페이지 이동
    @GetMapping("total-report")
    public String totalReportPage(Model model) {
        // 처리 대기 중인 신고와 전체 신고를 구분해서 가져올 수 있습니다.
        model.addAttribute("reports", reportService.getAllReports());
        return "admin/total-report";
    }

    // 신고 처리 (반려 또는 삭제 등)
    @PostMapping("report/process")
    @ResponseBody
    public ResponseEntity<String> processReport(@RequestParam Integer reportIdx,
                                                @RequestParam ReportStatus status,
                                                @RequestParam(required = false) String processNote){

        reportService.processReport(reportIdx, status, processNote);
        return ResponseEntity.ok("OK");
    }
}
