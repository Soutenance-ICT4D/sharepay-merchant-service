package com.sharepay.merchant.service;

import com.sharepay.merchant.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PaymentLinkTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;

    public PaymentLinkTokenService(@Value("${application.payment-links.crypto-key-base64}") String cryptoKeyBase64) {
        if (cryptoKeyBase64 == null || cryptoKeyBase64.isBlank()) {
            throw new IllegalStateException("Missing application.payment-links.crypto-key-base64");
        }

        byte[] keyBytes = Base64.getDecoder().decode(cryptoKeyBase64);
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalStateException("Invalid crypto key length. Use 16/24/32 bytes (AES-128/192/256) base64.");
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encryptCode(String code) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] ciphertext = cipher.doFinal(code.getBytes(StandardCharsets.UTF_8));

            byte[] tokenBytes = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, tokenBytes, 0, iv.length);
            System.arraycopy(ciphertext, 0, tokenBytes, iv.length, ciphertext.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "payment_link.token.encrypt_failed", "Erreur lors du chiffrement du lien");
        }
    }

    public String decryptToCode(String token) {
        try {
            byte[] tokenBytes = Base64.getUrlDecoder().decode(token);
            if (tokenBytes.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid token");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[tokenBytes.length - IV_LENGTH_BYTES];
            System.arraycopy(tokenBytes, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(tokenBytes, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "payment_link.token.invalid", "Lien invalide");
        }
    }
}
