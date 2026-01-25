package com.sharepay.merchant.service;

import com.sharepay.merchant.dto.request.paymentlink.CreatePaymentLinkRequest;
import com.sharepay.merchant.dto.response.PaymentLinkResponse;
import com.sharepay.merchant.entity.App;
import com.sharepay.merchant.entity.PaymentLink;
import com.sharepay.merchant.exception.BusinessException;
import com.sharepay.merchant.repository.AppRepository;
import com.sharepay.merchant.repository.PaymentLinkRepository;
import com.sharepay.merchant.repository.TransactionRepository;
import com.sharepay.merchant.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PaymentLinkService {

    private final PaymentLinkRepository paymentLinkRepository;
    private final TransactionRepository transactionRepository;
    private final AppRepository appRepository;
    private final PaymentLinkTokenService tokenService;
    private final String payBaseUrl;

    public PaymentLinkService(PaymentLinkRepository paymentLinkRepository,
                              TransactionRepository transactionRepository,
                              AppRepository appRepository,
                              PaymentLinkTokenService tokenService,
                              @Value("${application.payment-links.base-url}") String payBaseUrl) {
        this.paymentLinkRepository = paymentLinkRepository;
        this.transactionRepository = transactionRepository;
        this.appRepository = appRepository;
        this.tokenService = tokenService;
        this.payBaseUrl = payBaseUrl;
    }

    public List<PaymentLinkResponse> listMyPaymentLinks() {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        return paymentLinkRepository.findAllByApp_OwnerUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PaymentLinkResponse create(CreatePaymentLinkRequest request) {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        App app = appRepository.findByIdAndOwnerUser_Id(request.getAppId(), userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "app.not_found", "Application introuvable"));

        String amountType = normalizeAmountType(request.getAmountType());
        BigDecimal amount = null;
        if ("fixed".equals(amountType)) {
            if (request.getAmountValue() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "payment_link.amount.required", "amountValue est requis quand amountType=fixed");
            }
            amount = request.getAmountValue();
        }

        PaymentLink link = new PaymentLink();
        link.setApp(app);
        link.setCode(generateUniqueCode());
        link.setLink(buildPayLink(link.getCode()));
        link.setTitle(request.getTitle());
        link.setDescription(request.getDescription());
        link.setAmount(amount);

        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            link.setCurrency(request.getCurrency().trim().toUpperCase(Locale.ROOT));
        }
        if (request.getThemeColor() != null && !request.getThemeColor().isBlank()) {
            link.setThemeColor(request.getThemeColor().trim());
        }

        link.setLogoUrl(request.getLogoUrl());
        link.setRedirectUrl(request.getRedirectUrl());
        link.setExpiresAt(toInstantOrNull(request.getExpiresAt()));

        if (request.getStatus() != null) {
            String status = request.getStatus().trim().toUpperCase(Locale.ROOT);
            if ("EXPIRED".equals(status)) {
                link.setActive(false);
            } else if ("ACTIVE".equals(status)) {
                link.setActive(true);
            }
        }

        PaymentLink saved = paymentLinkRepository.save(link);
        return toResponse(saved);
    }

    public PaymentLinkResponse getMyPaymentLink(String code) {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        PaymentLink link = paymentLinkRepository.findByCodeAndApp_OwnerUser_Id(code, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "payment_link.not_found", "Lien de paiement introuvable"));

        return toResponse(link);
    }

    private PaymentLinkResponse toResponse(PaymentLink link) {
        BigDecimal amount = link.getAmount();
        String amountType = (amount == null) ? "free" : "fixed";

        String status = resolveStatus(link);
        long payments = transactionRepository.countByPaymentLink_Id(link.getId());

        return new PaymentLinkResponse(
                link.getCode(),
                link.getLink(),
                link.getTitle(),
                link.getDescription(),
                amountType,
                amount,
                link.getCurrency(),
                link.getLogoUrl(),
                link.getThemeColor(),
                link.getRedirectUrl(),
                link.getExpiresAt(),
                status,
                payments,
                link.getCreatedAt()
        );
    }

    private String buildPayLink(String code) {
        if (payBaseUrl == null || payBaseUrl.isBlank()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "payment_link.base_url.missing", "Configuration baseUrl manquante");
        }

        String base = payBaseUrl.endsWith("/") ? payBaseUrl.substring(0, payBaseUrl.length() - 1) : payBaseUrl;
        String token = tokenService.encryptCode(code);
        return base + "/pay/" + token;
    }

    private Instant toInstantOrNull(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toInstant();
    }

    private String resolveStatus(PaymentLink link) {
        if (!link.isActive()) {
            return "EXPIRED";
        }

        Instant expiresAt = link.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            return "EXPIRED";
        }

        return "ACTIVE";
    }

    private String normalizeAmountType(String amountType) {
        if (amountType == null) {
            return "fixed";
        }
        String v = amountType.trim().toLowerCase(Locale.ROOT);
        if (!"fixed".equals(v) && !"free".equals(v)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "payment_link.amount_type.invalid", "amountType invalide (fixed/free)");
        }
        return v;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "pl_" + randomBase62(12);
        } while (paymentLinkRepository.existsByCode(code));
        return code;
    }

    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private String randomBase62(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = BASE62[RANDOM.nextInt(BASE62.length)];
        }
        return new String(buf);
    }
}
