package com.sharepay.merchant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class PaymentLinkPublicResponse {

    private final String id;
    private final String title;
    private final String description;
    private final String amountType;
    private final BigDecimal amountValue;
    private final String currency;
    private final String logoUrl;
    private final String themeColor;
    private final String redirectUrl;
    private final Instant expiresAt;
    private final String status;
}
