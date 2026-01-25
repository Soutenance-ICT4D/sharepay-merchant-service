package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import com.sharepay.merchant.dto.request.user.UpdateMeRequest;
import com.sharepay.merchant.dto.response.MeResponse;
import com.sharepay.merchant.entity.User;
import com.sharepay.merchant.exception.BusinessException;
import com.sharepay.merchant.repository.UserRepository;
import com.sharepay.merchant.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/me")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MeResponse>> getMe() {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "user.not_found", "Utilisateur introuvable"));

        return ResponseEntity.ok(ApiResponse.success(
                "user.me.success",
                "Profil utilisateur",
                new MeResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole(),
                        user.isVerified(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                )
        ));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<MeResponse>> updateMe(@Valid @RequestBody UpdateMeRequest request) {
        UUID userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Utilisateur non authentifié");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "user.not_found", "Utilisateur introuvable"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        User saved = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                "user.me.updated",
                "Profil utilisateur mis à jour",
                new MeResponse(
                        saved.getId(),
                        saved.getFullName(),
                        saved.getEmail(),
                        saved.getPhone(),
                        saved.getRole(),
                        saved.isVerified(),
                        saved.getCreatedAt(),
                        saved.getUpdatedAt()
                )
        ));
    }
}
