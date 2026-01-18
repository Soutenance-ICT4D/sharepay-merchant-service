package com.sharepay.merchant.security;

import java.util.UUID;

public record JwtAuthDetails(UUID userId, Object webDetails) {
}
