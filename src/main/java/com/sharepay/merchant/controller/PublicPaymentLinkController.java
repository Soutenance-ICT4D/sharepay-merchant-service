package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import com.sharepay.merchant.dto.response.PaymentLinkPublicResponse;
import com.sharepay.merchant.service.PublicPaymentLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-links/public")
public class PublicPaymentLinkController {

    private final PublicPaymentLinkService publicPaymentLinkService;

    public PublicPaymentLinkController(PublicPaymentLinkService publicPaymentLinkService) {
        this.publicPaymentLinkService = publicPaymentLinkService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<PaymentLinkPublicResponse>> get(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success(
                "payment_link.public.get.success",
                "Lien de paiement chargé",
                publicPaymentLinkService.getByToken(token)
        ));
    }
}
