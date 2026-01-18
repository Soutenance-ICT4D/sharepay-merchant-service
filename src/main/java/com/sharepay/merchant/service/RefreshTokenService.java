package com.sharepay.merchant.service;

import com.sharepay.merchant.entity.RefreshToken;
import com.sharepay.merchant.entity.User;
import com.sharepay.merchant.exception.BusinessException;
import com.sharepay.merchant.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${application.security.jwt.refresh-token.expiration}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    public RefreshToken validateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.refresh.invalid",
                        "Token de rafraîchissement invalide"
                ));

        if (token.isRevoked()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.refresh.revoked",
                    "Token de rafraîchissement révoqué"
            );
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.refresh.expired",
                    "Token de rafraîchissement expiré"
            );
        }

        return token;
    }

    public void revoke(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.refresh.invalid",
                        "Token de rafraîchissement invalide"
                ));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
}
