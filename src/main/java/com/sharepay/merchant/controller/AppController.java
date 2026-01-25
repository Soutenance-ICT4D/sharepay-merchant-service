package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import com.sharepay.merchant.dto.request.app.CreateAppRequest;
import com.sharepay.merchant.dto.response.AppResponse;
import com.sharepay.merchant.service.AppService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppResponse>>> listMyApps() {
        return ResponseEntity.ok(ApiResponse.success(
                "app.list.success",
                "Liste des applications",
                appService.listMyApps()
        ));
    }

    @GetMapping("/{appId}")
    public ResponseEntity<ApiResponse<AppResponse>> getMyApp(@PathVariable UUID appId) {
        return ResponseEntity.ok(ApiResponse.success(
                "app.get.success",
                "Application chargée",
                appService.getMyApp(appId)
        ));
    }
}
