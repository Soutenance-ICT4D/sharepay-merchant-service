package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import com.sharepay.merchant.dto.request.app.CreateAppRequest;
import com.sharepay.merchant.dto.response.AppResponse;
import com.sharepay.merchant.service.AppService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants/apps")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppResponse>> create(@Valid @RequestBody CreateAppRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "app.create.success",
                "Application créée",
                appService.create(request)
        ));
    }
}
