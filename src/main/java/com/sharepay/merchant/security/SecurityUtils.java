package com.sharepay.merchant.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details instanceof JwtAuthDetails jwtAuthDetails) {
            return jwtAuthDetails.userId();
        }

        return null;
    }
}
