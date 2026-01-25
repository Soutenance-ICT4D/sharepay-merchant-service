package com.sharepay.merchant.dto.request.paymentlink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreatePaymentLinkRequest {

    @NotNull
    private UUID appId;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String amountType;

    private BigDecimal amountValue;

    private String currency;

    private String logoUrl;

    private String themeColor;

    private String redirectUrl;

    private LocalDateTime expiresAt;

    private Integer maxUses;

    private Boolean collectCustomerInfo;

    private String status;

    private String applicationTarget;
}
