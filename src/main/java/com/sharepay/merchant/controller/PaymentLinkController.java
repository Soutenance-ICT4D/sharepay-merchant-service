package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import com.sharepay.merchant.dto.request.paymentlink.CreatePaymentLinkRequest;
import com.sharepay.merchant.dto.response.PaymentLinkResponse;
import com.sharepay.merchant.service.PaymentLinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/payment-links")
public class PaymentLinkController {

    private final PaymentLinkService paymentLinkService;

    public PaymentLinkController(PaymentLinkService paymentLinkService) {
        this.paymentLinkService = paymentLinkService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentLinkResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                "payment_link.list.success",
                "Liste des liens de paiement",
                paymentLinkService.listMyPaymentLinks()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentLinkResponse>> create(@Valid @RequestBody CreatePaymentLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "payment_link.create.success",
                "Lien de paiement créé",
                paymentLinkService.create(request)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentLinkResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                "payment_link.get.success",
                "Lien de paiement chargé",
                paymentLinkService.getMyPaymentLink(id)
        ));
    }
}
