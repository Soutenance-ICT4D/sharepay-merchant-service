package com.sharepay.merchant.service;

import com.sharepay.merchant.dto.response.PaymentLinkPublicResponse;
import com.sharepay.merchant.entity.PaymentLink;
import com.sharepay.merchant.exception.BusinessException;
import com.sharepay.merchant.repository.PaymentLinkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PublicPaymentLinkService {

    private final PaymentLinkTokenService tokenService;
    private final PaymentLinkRepository paymentLinkRepository;

    public PublicPaymentLinkService(PaymentLinkTokenService tokenService, PaymentLinkRepository paymentLinkRepository) {
        this.tokenService = tokenService;
        this.paymentLinkRepository = paymentLinkRepository;
    }

    public PaymentLinkPublicResponse getByToken(String token) {
        String code = tokenService.decryptToCode(token);

        PaymentLink link = paymentLinkRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "payment_link.not_found", "Lien de paiement introuvable"));

        return toPublicResponse(link);
    }

    private PaymentLinkPublicResponse toPublicResponse(PaymentLink link) {
        BigDecimal amount = link.getAmount();
        String amountType = (amount == null) ? "free" : "fixed";

        String status;
        if (!link.isActive()) {
            status = "EXPIRED";
        } else {
            Instant expiresAt = link.getExpiresAt();
            status = (expiresAt != null && expiresAt.isBefore(Instant.now())) ? "EXPIRED" : "ACTIVE";
        }

        return new PaymentLinkPublicResponse(
                link.getCode(),
                link.getTitle(),
                link.getDescription(),
                amountType,
                amount,
                link.getCurrency(),
                link.getLogoUrl(),
                link.getThemeColor(),
                link.getRedirectUrl(),
                link.getExpiresAt(),
                status
        );
    }
}
