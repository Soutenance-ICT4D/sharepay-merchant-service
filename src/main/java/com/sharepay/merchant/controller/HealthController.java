package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchants")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                "auth.health.success",
                "Le service Merchant Service est en ligne",
                Map.of()
        ));
    }
}