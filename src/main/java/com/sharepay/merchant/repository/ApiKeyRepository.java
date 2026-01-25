package com.sharepay.merchant.repository;

import com.sharepay.merchant.entity.ApiKey;
import com.sharepay.merchant.entity.enums.ApiKeyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    boolean existsByApp_IdAndKeyType(UUID appId, ApiKeyType keyType);

    Optional<ApiKey> findByKeyPrefix(String keyPrefix);
}
