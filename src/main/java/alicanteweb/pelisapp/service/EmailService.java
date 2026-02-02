package alicanteweb.pelisapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Servicio para envío de emails de confirmación y notificaciones.
 * Solo se activa cuando app.dev-mode=false (producción)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.dev-mode", havingValue = "false", matchIfMissing = true)
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@pelisapp.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Envía un email de confirmación al usuario recién registrado.
     */
    public void sendConfirmationEmail(String toEmail, String username, String confirmationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎬 Confirma tu cuenta en PelisApp");

            String confirmationUrl = baseUrl + "/confirm-account?token=" + confirmationToken;

            String htmlContent = buildConfirmationEmailHtml(username, confirmationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de confirmación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email de confirmación a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("No se pudo enviar el email de confirmación", e);
        }
    }

    /**
     * Envía un email simple de texto plano (fallback).
     */
    public void sendSimpleConfirmationEmail(String toEmail, String username, String confirmationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Confirma tu cuenta en PelisApp");

            String confirmationUrl = baseUrl + "/confirm-account?token=" + confirmationToken;
            String content = String.format(
                "Hola %s,\n\n" +
                "Gracias por registrarte en PelisApp! Para confirmar tu cuenta, " +
                "haz clic en el siguiente enlace:\n\n" +
                "%s\n\n" +
                "Si no puedes hacer clic en el enlace, cópialo y pégalo en tu navegador.\n\n" +
                "¡Bienvenido a la comunidad cinéfila de PelisApp!\n\n" +
                "El equipo de PelisApp",
                username, confirmationUrl
            );

            message.setText(content);
            mailSender.send(message);
            log.info("Email simple de confirmación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email simple a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("No se pudo enviar el email de confirmación", e);
        }
    }

    /**
     * Construye el contenido HTML del email de confirmación.
     */
    private String buildConfirmationEmailHtml(String username, String confirmationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Confirma tu cuenta - PelisApp</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #1e1e1e;
                        color: #ffffff;
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: linear-gradient(135deg, #2c2c2c, #1e1e1e);
                        border-radius: 10px;
                        padding: 30px;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 2.5rem;
                        color: #ff6b35;
                        margin-bottom: 10px;
                    }
                    .title {
                        font-size: 24px;
                        color: #ff6b35;
                        font-weight: bold;
                    }
                    .content {
                        line-height: 1.6;
                        margin-bottom: 30px;
                    }
                    .button-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .confirm-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #ff6b35, #e55a30);
                        color: white !important;
                        text-decoration: none;
                        padding: 15px 30px;
                        border-radius: 8px;
                        font-weight: bold;
                        font-size: 16px;
                        transition: all 0.3s ease;
                    }
                    .confirm-button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        color: #b3b3b3;
                        font-size: 14px;
                        border-top: 1px solid #444;
                        padding-top: 20px;
                    }
                    .url-fallback {
                        word-break: break-all;
                        background: rgba(255, 255, 255, 0.1);
                        padding: 10px;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-family: monospace;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🎬</div>
                        <h1 class="title">PelisApp</h1>
                    </div>
                    
                    <div class="content">
                        <h2>¡Hola %s! 👋</h2>
                        <p>¡Gracias por unirte a <strong>PelisApp</strong>, la red social para amantes del cine!</p>
                        <p>Para completar tu registro y comenzar a descubrir películas increíbles, necesitamos confirmar tu dirección de correo electrónico.</p>
                        
                        <div class="button-container">
                            <a href="%s" class="confirm-button">
                                ✅ Confirmar mi Cuenta
                            </a>
                        </div>
                        
                        <p>Si el botón no funciona, también puedes copiar y pegar este enlace en tu navegador:</p>
                        <div class="url-fallback">%s</div>
                        
                        <p><strong>¿Por qué confirmar tu email?</strong></p>
                        <ul>
                            <li>🔐 Protege tu cuenta</li>
                            <li>📧 Recibe notificaciones importantes</li>
                            <li>🎭 Accede a todas las funciones</li>
                            <li>⭐ Comienza a valorar películas</li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>Este enlace expirará en 24 horas por seguridad.</p>
                        <p>Si no creaste esta cuenta, puedes ignorar este email.</p>
                        <hr style="border-color: #444; margin: 20px 0;">
                        <p>
                            Con ❤️, el equipo de <strong>PelisApp</strong><br>
                            <em>La red social definitiva para cinéfilos</em>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, confirmationUrl, confirmationUrl);
    }
}
