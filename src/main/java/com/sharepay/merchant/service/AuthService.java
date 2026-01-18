package com.sharepay.merchant.service;

import com.sharepay.merchant.dto.request.auth.LoginRequest;
import com.sharepay.merchant.dto.request.auth.RefreshRequest;
import com.sharepay.merchant.dto.request.auth.RegisterRequest;
import com.sharepay.merchant.dto.request.auth.RequestPasswordResetRequest;
import com.sharepay.merchant.dto.request.auth.ResetPasswordRequest;
import com.sharepay.merchant.dto.request.auth.VerifyEmailRequest;
import com.sharepay.merchant.dto.request.auth.VerifyResetOtpRequest;
import com.sharepay.merchant.dto.response.AuthResponse;
import com.sharepay.merchant.entity.RefreshToken;
import com.sharepay.merchant.entity.User;
import com.sharepay.merchant.entity.enums.AuthProvider;
import com.sharepay.merchant.exception.BusinessException;
import com.sharepay.merchant.repository.UserRepository;
import com.sharepay.merchant.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private enum OtpPurpose {
        VERIFY_EMAIL,
        RESET_PASSWORD
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(RegisterRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            if (user.isVerified()) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.email.already_used",
                        "Email déjà utilisé"
                );
            }
            if (user.getProvider() != AuthProvider.LOCAL) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.use_social_login",
                        "Veuillez utiliser la connexion via Google"
                );
            }

            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setProvider(AuthProvider.LOCAL);
            user.setProviderId(null);
            user.setVerified(false);
        }

        issueOtp(user, OtpPurpose.VERIFY_EMAIL);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.user.not_found",
                        "Utilisateur introuvable"
                ));

        if (user.isVerified()) {
            return;
        }

        validateOtp(user, request.getOtpCode());

        user.setVerified(true);
        clearOtp(user);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.invalid_credentials",
                        "Identifiants invalides"
                ));

        if (user.getPassword() == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.use_social_login",
                    "Veuillez utiliser la connexion via Google"
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.invalid_credentials",
                    "Identifiants invalides"
            );
        }

        if (!user.isVerified()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.account_not_verified",
                    "Compte non vérifié"
            );
        }

        String accessToken = jwtService.generateToken(user.getEmail(), Map.of("uid", user.getId().toString()));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(user.getEmail(), Map.of("uid", user.getId().toString()));
        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    public void requestPasswordReset(RequestPasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.user.not_found",
                        "Utilisateur introuvable"
                ));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.reset.not_allowed_for_social",
                    "Réinitialisation du mot de passe indisponible pour les comptes Google"
            );
        }

        issueOtp(user, OtpPurpose.RESET_PASSWORD);
    }

    public String verifyResetOtp(VerifyResetOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.user.not_found",
                        "Utilisateur introuvable"
                ));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.reset.not_allowed_for_social",
                    "Réinitialisation du mot de passe indisponible pour les comptes Google"
            );
        }

        validateOtp(user, request.getOtpCode());

        String resetToken = UUID.randomUUID().toString();
        clearOtp(user);
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        return resetToken;
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "auth.user.not_found",
                        "Utilisateur introuvable"
                ));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.reset.not_allowed_for_social",
                    "Réinitialisation du mot de passe indisponible pour les comptes Google"
            );
        }

        validateResetToken(user, request.getResetToken());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clearResetToken(user);
        userRepository.save(user);

        try {
            mailService.sendPasswordResetSuccess(user.getEmail());
        } catch (Exception ex) {
            log.warn("Password reset success email failed to send", ex);
        }
    }

    private void issueOtp(User user, OtpPurpose purpose) {
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        if (purpose == OtpPurpose.VERIFY_EMAIL) {
            mailService.sendVerifyEmailOtp(user.getEmail(), otp);
            return;
        }

        mailService.sendResetPasswordOtp(user.getEmail(), otp);
    }

    private void validateOtp(User user, String otpCode) {
        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.otp.not_requested",
                    "Aucun code OTP n'a été demandé"
            );
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.otp.expired",
                    "Code OTP expiré"
            );
        }

        if (!user.getOtpCode().equals(otpCode)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.otp.invalid",
                    "Code OTP invalide"
            );
        }
    }

    private void validateResetToken(User user, String resetToken) {
        if (user.getResetToken() == null || user.getResetTokenExpiry() == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.reset.not_requested",
                    "Réinitialisation non demandée"
            );
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.reset.token_expired",
                    "Token de réinitialisation expiré"
            );
        }

        if (!user.getResetToken().equals(resetToken)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "auth.reset.token_invalid",
                    "Token de réinitialisation invalide"
            );
        }
    }

    private void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpExpiry(null);
    }

    private void clearResetToken(User user) {
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
    }

    private String generateOtp() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}
