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

import java.util.UUID;

@Service
public class AppService {

    private final AppRepository appRepository;
    private final EntityManager entityManager;

    public AppService(AppRepository appRepository, EntityManager entityManager) {
        this.appRepository = appRepository;
        this.entityManager = entityManager;
    }

    public AppResponse create(CreateAppRequest request) {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        App app = new App();
        app.setName(request.getName());
        app.setWebhookUrl(request.getWebhookUrl());

        User ownerRef = entityManager.getReference(User.class, userId);
        app.setOwnerUser(ownerRef);

        App saved = appRepository.save(app);
        return new AppResponse(
                saved.getId(),
                saved.getName(),
                saved.getEnvironment(),
                saved.getStatus(),
                saved.getWebhookUrl(),
                saved.getCreatedAt()
        );
    }
}
