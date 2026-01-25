package com.sharepay.merchant.service;

import com.sharepay.merchant.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    private static final String PRIMARY_COLOR = "#098865";
    private static final String BACKGROUND_COLOR = "#F9FAFB";

    public MailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendVerifyEmailOtp(String to, String otpCode) {
        sendHtml(to, "Vérification de votre compte SharePay", buildVerifyEmailHtml(otpCode));
    }

    public void sendResetPasswordOtp(String to, String otpCode) {
        sendHtml(to, "Réinitialisation de votre mot de passe SharePay", buildResetPasswordHtml(otpCode));
    }

    public void sendPasswordResetSuccess(String to) {
        sendHtml(to, "Mot de passe réinitialisé - SharePay", buildPasswordResetSuccessHtml());
    }

    private void sendHtml(String to, String subject, String html) {
        log.info("Mail send attempt: to='{}', subject='{}', from='{}'", to, subject, from);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom("SharePay <" + from + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mimeMessage);
            log.info("Mail sent successfully: to='{}', subject='{}'", to, subject);
        } catch (MailAuthenticationException ex) {
            log.error("Mail authentication failed (check SMTP username/password or Gmail App Password): to='{}', subject='{}'", to, subject, ex);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "mail.auth_failed",
                    "Erreur d'authentification SMTP lors de l'envoi de l'email"
            );
        } catch (MailSendException ex) {
            log.error("Mail send failed (SMTP rejected message): to='{}', subject='{}'", to, subject, ex);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "mail.send_failed",
                    "Erreur lors de l'envoi de l'email"
            );
        } catch (MailException ex) {
            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }

            String rootType = root.getClass().getName();
            String rootMessage = root.getMessage();
            log.error("Mail exception occurred: to='{}', subject='{}', rootType='{}', rootMessage='{}'", to, subject, rootType, rootMessage, ex);

            String messageKey = "mail.send_failed";
            String message = "Erreur lors de l'envoi de l'email";
            if (rootType.contains("SSL") || rootType.contains("Handshake")) {
                messageKey = "mail.tls_failed";
                message = "Erreur TLS lors de l'envoi de l'email";
            } else if (rootType.contains("UnknownHost") || rootType.contains("Connect") || rootType.contains("Timeout")) {
                messageKey = "mail.transport_failed";
                message = "Erreur réseau lors de l'envoi de l'email";
            }

            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    messageKey,
                    message
            );
        } catch (Exception ex) {
            log.error("Unexpected error while sending mail: to='{}', subject='{}'", to, subject, ex);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "mail.unexpected_error",
                    "Erreur inattendue lors de l'envoi de l'email"
            );
        }
    }

    private String buildVerifyEmailHtml(String otpCode) {
        return buildBaseHtml(
                "Vérifiez votre adresse email",
                "Merci d'avoir rejoint SharePay ! Pour finaliser la création de votre compte, veuillez utiliser le code de confirmation suivant :",
                otpCode,
                "Ce code est valable pendant 10 minutes. Si vous n'avez pas créé de compte, vous pouvez ignorer cet email en toute sécurité."
        );
    }

    private String buildResetPasswordHtml(String otpCode) {
        return buildBaseHtml(
                "Réinitialisation de mot de passe",
                "Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte SharePay. Utilisez le code ci-dessous pour continuer :",
                otpCode,
                "Ce code est valable pendant 10 minutes. Si vous n'avez pas demandé de réinitialisation, veuillez sécuriser votre compte."
        );
    }

    private String buildPasswordResetSuccessHtml() {
        return "<!DOCTYPE html>"
                + "<html lang=\"fr\">"
                + "<head>"
                + "    <meta charset=\"UTF-8\">"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "</head>"
                + "<body style=\"margin: 0; padding: 0; background-color: " + BACKGROUND_COLOR + "; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">"
                + "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"
                + "        <tr>"
                + "            <td align=\"center\" style=\"padding: 40px 10px;\">"
                + "                <table role=\"presentation\" style=\"max-width: 500px; width: 100%; background: #ffffff; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); overflow: hidden;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"
                + "                    <tr>"
                + "                        <td align=\"center\" style=\"padding: 30px 40px 10px 40px;\">"
                + "                            <h1 style=\"margin: 0; color: " + PRIMARY_COLOR + "; font-size: 28px; font-weight: 800; letter-spacing: -0.5px;\">SharePay</h1>"
                + "                        </td>"
                + "                    </tr>"
                + "                    <tr>"
                + "                        <td style=\"padding: 20px 40px 40px 40px; text-align: center;\">"
                + "                            <h2 style=\"margin: 0 0 16px 0; color: #111827; font-size: 20px; font-weight: 700;\">" + escape("Mot de passe réinitialisé") + "</h2>"
                + "                            <p style=\"margin: 0 0 14px 0; color: #4b5563; font-size: 15px; line-height: 1.6;\">" + escape("Votre mot de passe a été réinitialisé avec succès.") + "</p>"
                + "                            <p style=\"margin: 0; color: #4b5563; font-size: 15px; line-height: 1.6;\">" + escape("Si vous n'êtes pas à l'origine de cette action, contactez immédiatement le support et sécurisez votre compte.") + "</p>"
                + "                        </td>"
                + "                    </tr>"
                + "                    <tr><td style=\"padding: 0 40px;\"><div style=\"border-top: 1px solid #f3f4f6;\"></div></td></tr>"
                + "                    <tr>"
                + "                        <td style=\"padding: 20px 40px 30px 40px; text-align: center;\">"
                + "                            <p style=\"margin: 0; color: #9ca3af; font-size: 12px;\">© 2026 SharePay Inc. Tous droits réservés.</p>"
                + "                            <p style=\"margin: 4px 0 0 0; color: #9ca3af; font-size: 12px;\">Yaoundé, Cameroun</p>"
                + "                        </td>"
                + "                    </tr>"
                + "                </table>"
                + "            </td>"
                + "        </tr>"
                + "    </table>"
                + "</body>"
                + "</html>";
    }

    private String buildBaseHtml(String title, String intro, String otpCode, String footer) {
        return "<!DOCTYPE html>"
                + "<html lang=\"fr\">"
                + "<head>"
                + "    <meta charset=\"UTF-8\">"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "    <style>"
                + "        @media market { .container { width: 100% !important; } }"
                + "    </style>"
                + "</head>"
                + "<body style=\"margin: 0; padding: 0; background-color: " + BACKGROUND_COLOR + "; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">"
                + "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"
                + "        <tr>"
                + "            <td align=\"center\" style=\"padding: 40px 10px;\">"
                + "                <table role=\"presentation\" style=\"max-width: 500px; width: 100%; background: #ffffff; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); overflow: hidden;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"
                + "                    "
                + "                    <tr>"
                + "                        <td align=\"center\" style=\"padding: 30px 40px 10px 40px;\">"
                + "                            <h1 style=\"margin: 0; color: " + PRIMARY_COLOR + "; font-size: 28px; font-weight: 800; letter-spacing: -0.5px;\">SharePay</h1>"
                + "                        </td>"
                + "                    </tr>"
                + "                    "
                + "                    <tr>"
                + "                        <td style=\"padding: 20px 40px 40px 40px; text-align: center;\">"
                + "                            <h2 style=\"margin: 0 0 16px 0; color: #111827; font-size: 20px; font-weight: 700;\">" + escape(title) + "</h2>"
                + "                            <p style=\"margin: 0 0 30px 0; color: #4b5563; font-size: 15px; line-height: 1.6;\">" + escape(intro) + "</p>"
                + "                            "
                + "                            <div style=\"padding: 20px; background-color: #f0fdf4; border: 2px dashed " + PRIMARY_COLOR + "; border-radius: 12px; display: inline-block;\">"
                + "                                <span style=\"font-family: 'Courier New', Courier, monospace; font-size: 36px; font-weight: 800; color: " + PRIMARY_COLOR + "; letter-spacing: 8px;\">" + escape(otpCode) + "</span>"
                + "                            </div>"
                + "                            "
                + "                            <p style=\"margin: 30px 0 0 0; color: #9ca3af; font-size: 13px; font-style: italic;\">" + escape(footer) + "</p>"
                + "                        </td>"
                + "                    </tr>"
                + "                    "
                + "                    <tr><td style=\"padding: 0 40px;\"><div style=\"border-top: 1px solid #f3f4f6;\"></div></td></tr>"
                + "                    "
                + "                    <tr>"
                + "                        <td style=\"padding: 20px 40px 30px 40px; text-align: center;\">"
                + "                            <p style=\"margin: 0; color: #9ca3af; font-size: 12px;\">© 2026 SharePay Inc. Tous droits réservés.</p>"
                + "                            <p style=\"margin: 4px 0 0 0; color: #9ca3af; font-size: 12px;\">Yaoundé, Cameroun</p>"
                + "                        </td>"
                + "                    </tr>"
                + "                </table>"
                + "            </td>"
                + "        </tr>"
                + "    </table>"
                + "</body>"
                + "</html>";
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
