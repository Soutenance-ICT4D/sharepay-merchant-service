package com.sharepay.merchant.controller;

import com.sharepay.merchant.dto.ApiResponse;
import com.sharepay.merchant.dto.request.auth.LoginRequest;
import com.sharepay.merchant.dto.request.auth.LogoutRequest;
import com.sharepay.merchant.dto.request.auth.RefreshRequest;
import com.sharepay.merchant.dto.request.auth.RegisterRequest;
import com.sharepay.merchant.dto.request.auth.RequestPasswordResetRequest;
import com.sharepay.merchant.dto.request.auth.ResetPasswordRequest;
import com.sharepay.merchant.dto.request.auth.VerifyEmailRequest;
import com.sharepay.merchant.dto.request.auth.VerifyResetOtpRequest;
import com.sharepay.merchant.dto.response.AuthResponse;
import com.sharepay.merchant.service.AuthService;
import com.sharepay.merchant.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchants/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(
                "auth.register.otp_issued",
                "Code de vérification envoyé",
                Map.of()
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(
                "auth.email.verified",
                "Email vérifié",
                Map.of()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "auth.login.success",
                "Connexion réussie",
                authService.login(request)
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "auth.refresh.success",
                "Token rafraîchi",
                authService.refresh(request)
        ));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(
                "auth.reset.otp_issued",
                "Code de réinitialisation envoyé",
                Map.of()
        ));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        String resetToken = authService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "auth.reset.otp_verified",
                "Code OTP vérifié",
                Map.of("resetToken", resetToken)
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "auth.reset.password_updated",
                "Mot de passe mis à jour",
                Map.of()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(
                "auth.logout.success",
                "Déconnexion réussie",
                Map.of()
        ));
    }
}
