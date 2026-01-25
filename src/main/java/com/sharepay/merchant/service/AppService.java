package com.sharepay.merchant.service;

import com.sharepay.merchant.dto.request.app.CreateAppRequest;
import com.sharepay.merchant.dto.response.AppResponse;
import com.sharepay.merchant.entity.App;
import com.sharepay.merchant.entity.User;
import com.sharepay.merchant.exception.BusinessException;
import com.sharepay.merchant.repository.AppRepository;
import com.sharepay.merchant.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AppService {

    private final AppRepository appRepository;
    private final EntityManager entityManager;
    private final ApiKeyService apiKeyService;

    public AppService(AppRepository appRepository, EntityManager entityManager, ApiKeyService apiKeyService) {
        this.appRepository = appRepository;
        this.entityManager = entityManager;
        this.apiKeyService = apiKeyService;
    }

    public AppResponse create(CreateAppRequest request) {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        App app = new App();
        app.setName(request.getName());
        app.setDescription(request.getDescription());
        app.setEnvironment(request.getEnvironment());
        app.setWebhookUrl(request.getWebhookUrl());

        User ownerRef = entityManager.getReference(User.class, userId);
        app.setOwnerUser(ownerRef);

        App saved = appRepository.save(app);

        apiKeyService.ensureDefaultKeys(saved);
        return toResponse(saved);
    }

    public List<AppResponse> listMyApps() {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        return appRepository.findAllByOwnerUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AppResponse getMyApp(UUID appId) {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        App app = appRepository.findByIdAndOwnerUser_Id(appId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "app.not_found", "Application introuvable"));

        return toResponse(app);
    }

    private AppResponse toResponse(App app) {
        return new AppResponse(
                app.getId(),
                app.getName(),
                app.getDescription(),
                app.getEnvironment(),
                app.getStatus(),
                app.getWebhookUrl(),
                app.getCreatedAt()
        );
    }
}
