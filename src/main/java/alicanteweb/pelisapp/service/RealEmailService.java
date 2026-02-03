package alicanteweb.pelisapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Servicio real de email que envía correos reales.
 * Se activa cuando app.email.enabled=true Y JavaMailSender está disponible
 */
@Service("realEmailService")
@Primary // Se convierte en el servicio principal cuando está habilitado
@Slf4j
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true")
public class RealEmailService implements IEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:#{null}}")
    private String fromEmail;

    @Value("${app.name:PelisApp}")
    private String appName;

    @PostConstruct
    public void init() {
        log.info("🔧 Inicializando RealEmailService...");
        log.info("📧 Email origen: {}", fromEmail != null ? fromEmail : "❌ NO CONFIGURADO");
        log.info("🌍 Base URL: {}", baseUrl);
        log.info("📱 Nombre app: {}", appName);
        log.info("📮 JavaMailSender: {}", mailSender != null ? "✅ CONFIGURADO" : "❌ NO CONFIGURADO");

        if (mailSender == null) {
            log.error("❌ CRÍTICO: JavaMailSender no está disponible. Verifica la configuración de email.");
        }

        if (fromEmail == null || fromEmail.isEmpty()) {
            log.error("❌ CRÍTICO: spring.mail.username no está configurado.");
        } else {
            log.info("✅ RealEmailService inicializado correctamente");
        }
    }

    @Override
    public void sendConfirmationEmail(String toEmail, String username, String confirmationToken) {
        // Verificar que el servicio está configurado correctamente
        if (mailSender == null) {
            log.error("❌ JavaMailSender no está configurado. Verifica las propiedades de email.");
            throw new RuntimeException("JavaMailSender no configurado");
        }

        if (fromEmail == null || fromEmail.isEmpty()) {
            log.error("❌ spring.mail.username no está configurado. Configura EMAIL_USERNAME.");
            throw new RuntimeException("Email username no configurado");
        }

        try {
            log.info("📧 Enviando email de confirmación a: {} desde: {}", toEmail, fromEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Configurar email
            helper.setTo(toEmail);
            helper.setFrom(fromEmail);
            helper.setSubject("🎬 Confirma tu cuenta en " + appName);

            // Crear URL de confirmación
            String confirmationUrl = baseUrl + "/confirm-account?token=" + confirmationToken;

            // Crear contenido HTML del email
            String htmlContent = createConfirmationEmailHTML(username, confirmationUrl);
            helper.setText(htmlContent, true);

            // Enviar email
            mailSender.send(mimeMessage);

            log.info("✅ Email de confirmación enviado exitosamente a: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Error enviando email de confirmación a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error enviando email de confirmación: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error inesperado enviando email: {}", e.getMessage());
            throw new RuntimeException("Error inesperado enviando email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendSimpleConfirmationEmail(String toEmail, String username, String confirmationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setSubject("🎬 Confirma tu cuenta en " + appName);

            String confirmationUrl = baseUrl + "/confirm-account?token=" + confirmationToken;

            String textContent = String.format("""
                ¡Hola %s!
                
                Gracias por registrarte en %s, tu red social de películas favorita.
                
                Para activar tu cuenta, haz clic en el siguiente enlace:
                %s
                
                Este enlace es válido por 24 horas.
                
                Si no creaste esta cuenta, puedes ignorar este email.
                
                ¡Disfruta valorando y descubriendo nuevas películas!
                
                Saludos,
                El equipo de %s
                """, username, appName, confirmationUrl, appName);

            message.setText(textContent);

            mailSender.send(message);
            log.info("✅ Email simple de confirmación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Error enviando email simple a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }

    /**
     * Crea contenido HTML bonito para el email de confirmación
     */
    private String createConfirmationEmailHTML(String username, String confirmationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Confirma tu cuenta - %s</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #1e1e1e, #2c2c2c);
                        margin: 0;
                        padding: 20px;
                        color: #ffffff;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: rgba(44, 44, 44, 0.95);
                        border-radius: 15px;
                        overflow: hidden;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
                        border: 1px solid rgba(255, 107, 53, 0.3);
                    }
                    .header {
                        background: linear-gradient(135deg, #ff6b35, #e55a30);
                        text-align: center;
                        padding: 40px 20px;
                    }
                    .logo {
                        font-size: 2.5rem;
                        font-weight: bold;
                        margin-bottom: 10px;
                    }
                    .subtitle {
                        font-size: 1.1rem;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .welcome {
                        font-size: 1.5rem;
                        font-weight: bold;
                        margin-bottom: 20px;
                        color: #ff6b35;
                    }
                    .message {
                        font-size: 1.1rem;
                        line-height: 1.6;
                        margin-bottom: 30px;
                        color: #b3b3b3;
                    }
                    .confirm-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #ff6b35, #e55a30);
                        color: white !important;
                        text-decoration: none;
                        padding: 15px 40px;
                        border-radius: 10px;
                        font-weight: bold;
                        font-size: 1.1rem;
                        transition: all 0.3s ease;
                        box-shadow: 0 8px 25px rgba(255, 107, 53, 0.3);
                    }
                    .confirm-button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 12px 30px rgba(255, 107, 53, 0.4);
                    }
                    .footer {
                        padding: 30px;
                        text-align: center;
                        border-top: 1px solid rgba(255, 255, 255, 0.1);
                    }
                    .footer-text {
                        color: #888;
                        font-size: 0.9rem;
                        line-height: 1.5;
                    }
                    .url-fallback {
                        background: rgba(0, 0, 0, 0.3);
                        padding: 15px;
                        border-radius: 8px;
                        margin-top: 20px;
                        font-family: monospace;
                        word-break: break-all;
                        font-size: 0.9rem;
                        color: #ccc;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🎬 %s</div>
                        <div class="subtitle">Tu Red Social de Películas</div>
                    </div>
                    
                    <div class="content">
                        <div class="welcome">¡Hola %s!</div>
                        
                        <div class="message">
                            Gracias por unirte a <strong>%s</strong>, la comunidad donde las películas cobran vida.<br><br>
                            
                            Para comenzar a valorar películas, escribir reseñas y conectar con otros cinéfilos,
                            necesitas confirmar tu dirección de email.
                        </div>
                        
                        <a href="%s" class="confirm-button">
                            ✅ Confirmar mi Cuenta
                        </a>
                        
                        <div class="url-fallback">
                            <strong>Si el botón no funciona, copia y pega este enlace en tu navegador:</strong><br>
                            <span style="color: #ff6b35;">%s</span>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <div class="footer-text">
                            <strong>Este enlace es válido por 24 horas.</strong><br><br>
                            
                            Si no creaste esta cuenta, puedes ignorar este email de forma segura.<br><br>
                            
                            ¿Tienes problemas? Contacta con nuestro soporte.<br><br>
                            
                            Con amor cinéfilo,<br>
                            <strong>El equipo de %s</strong> 🍿
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, appName, appName, username, appName, confirmationUrl, confirmationUrl, appName);
    }
}
