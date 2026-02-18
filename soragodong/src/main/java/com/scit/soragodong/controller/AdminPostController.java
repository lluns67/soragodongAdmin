package com.scit.soragodong.controller;

import com.scit.soragodong.domain.dto.AdminPostDto;
import com.scit.soragodong.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/post")
@RequiredArgsConstructor
@Slf4j
public class AdminPostController {

    private final AdminService adminService;

    /**
     * 게시물 상태 변경 (삭제/복구)
     * @param idx 게시글 번호
     * @param type 게시글 유형 (커뮤니티 / 중고거래)
     */
    @PostMapping("/toggle-status")
    public ResponseEntity<String> togglePostStatus(
            @RequestParam("idx") Integer idx,
            @RequestParam("type") String type) {

        try {
            adminService.updatePostStatus(idx, type);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail: " + e.getMessage());
        }
    }
    @GetMapping("/detail")
    public ResponseEntity<AdminPostDto> getPostDetail(
            @RequestParam("idx") Integer idx,
            @RequestParam("type") String type) {
        log.debug("{}, {}",idx, type);
        // 이전에 만든 AdminPostDto 규격을 그대로 활용하여 반환합니다.
        AdminPostDto detail = adminService.getPostDetail(idx, type);
        return ResponseEntity.ok(detail);
    }
}
