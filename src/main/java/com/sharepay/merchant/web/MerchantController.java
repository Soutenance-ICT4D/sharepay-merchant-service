package com.sharepay.merchant.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "sharepay-merchant-service",
                "status", "OK"
        );
    }
}
