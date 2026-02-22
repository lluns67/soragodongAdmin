package com.scit.soragodong.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController // JSON 반환을 위해 RestController 권장 (혹은 @ResponseBody 사용)
@RequestMapping("/admin/api/monitoring")
@RequiredArgsConstructor
public class MonitoringApiController {

    private final RestTemplate restTemplate;

    // 본 서버의 내부 모니터링 주소 (예: http://192.168.0.100:8080/api/internal/stats)
    private final String MAIN_SERVER_URL = "http://localhost:8080/api/internal/stats";

    @GetMapping("stats")
    public ResponseEntity<?> getRemoteStats() {
        try {
            // 1. 본 서버로 데이터 요청 (GET 방식)
            // 본 서버에서 Map<String, Object> 형태의 JSON을 반환한다고 가정합니다.
            ResponseEntity<Map> response = restTemplate.getForEntity(MAIN_SERVER_URL, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();

                // 관리자 서버에서 추가로 가공하고 싶은 데이터가 있다면 여기서 처리 가능합니다.
                // 예: 응답 시간(responseTime) 계산 등
                return ResponseEntity.ok(body);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("본 서버 응답 오류");
            }
        } catch (Exception e) {
            // 이 줄을 추가해서 콘솔에 찍히는 에러를 확인하세요!
            System.out.println("본 서버 호출 실패 원인: " + e.getMessage());
            e.printStackTrace();


            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("status", "Offline");
            errorStats.put("cpuUsage", 0);
            errorStats.put("memoryUsage", 0);
            errorStats.put("activeUsers", 0);
            return ResponseEntity.ok(errorStats); // 화면에서 에러 처리를 위해 기본값 반환
        }
    }


}
