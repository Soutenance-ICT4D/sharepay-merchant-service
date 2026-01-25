package com.sharepay.merchant.service;

import com.sharepay.merchant.entity.ApiKey;
import com.sharepay.merchant.entity.App;
import com.sharepay.merchant.entity.enums.AppEnvironment;
import com.sharepay.merchant.entity.enums.ApiKeyType;
import com.sharepay.merchant.repository.ApiKeyRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final EntityManager entityManager;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyRepository apiKeyRepository, EntityManager entityManager) {
        this.apiKeyRepository = apiKeyRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void ensureDefaultKeys(App app) {
        UUID appId = app.getId();
        if (appId == null) {
            throw new IllegalArgumentException("App id is required");
        }

        if (!apiKeyRepository.existsByApp_IdAndKeyType(appId, ApiKeyType.PUBLIC)) {
            createKey(appId, ApiKeyType.PUBLIC);
        }
        if (!apiKeyRepository.existsByApp_IdAndKeyType(appId, ApiKeyType.SECRET)) {
            createKey(appId, ApiKeyType.SECRET);
        }
    }

    private void createKey(UUID appId, ApiKeyType type) {
        App appRef = entityManager.getReference(App.class, appId);

        String prefix = generateUniquePrefix(appRef, type);
        String token = prefix + generateTokenPart(32);
        String hash = sha256Base64(token);

        ApiKey apiKey = new ApiKey();
        apiKey.setApp(appRef);
        apiKey.setKeyType(type);
        apiKey.setName(type == ApiKeyType.PUBLIC ? "Default Public" : "Default Secret");
        apiKey.setKeyPrefix(prefix);
        apiKey.setKeyHash(hash);

        apiKeyRepository.save(apiKey);
    }

    private String generateUniquePrefix(App app, ApiKeyType type) {
        String envPrefix = app.getEnvironment() == null || app.getEnvironment() == AppEnvironment.SANDBOX
                ? "sp_test_"
                : "sp_live_";
        String typePrefix = type == ApiKeyType.PUBLIC ? "pub_" : "sec_";
        String base = envPrefix + typePrefix;

        for (int i = 0; i < 25; i++) {
            String suffix = generateTokenPart(20 - base.length());
            String candidate = base + suffix;
            if (candidate.length() > 20) {
                candidate = candidate.substring(0, 20);
            }
            if (apiKeyRepository.findByKeyPrefix(candidate).isEmpty()) {
                return candidate;
            }
        }

        return base.substring(0, Math.min(20, base.length()));
    }

    private String generateTokenPart(int length) {
        if (length <= 0) {
            return "";
        }
        byte[] bytes = new byte[(int) Math.ceil(length * 0.75) + 1];
        secureRandom.nextBytes(bytes);
        String s = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return s.length() <= length ? s : s.substring(0, length);
    }

    private String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash api key", ex);
        }
    }
}
