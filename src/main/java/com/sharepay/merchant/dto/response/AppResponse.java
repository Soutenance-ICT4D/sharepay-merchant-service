package com.sharepay.merchant.dto.response;

import com.sharepay.merchant.entity.enums.AppEnvironment;
import com.sharepay.merchant.entity.enums.AppStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AppResponse {
    private final UUID id;
    private final String name;
    private final AppEnvironment environment;
    private final AppStatus status;
    private final String webhookUrl;
    private final Instant createdAt;
}
