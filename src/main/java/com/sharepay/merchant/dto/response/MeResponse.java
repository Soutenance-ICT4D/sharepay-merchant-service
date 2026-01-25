package com.sharepay.merchant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MeResponse {
    private final UUID id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String role;
    private final boolean verified;
    private final Instant createdAt;
    private final Instant updatedAt;
}
