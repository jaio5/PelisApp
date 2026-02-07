package alicanteweb.pelisapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@Slf4j
public class EmailDiagnosticService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @PostConstruct
    public void diagnosticEmailConfiguration() {
        log.info("🔍 === DIAGNÓSTICO DE CONFIGURACIÓN DE EMAIL ===");
        log.info("✉️ Email habilitado: {}", emailEnabled);
        log.info("🏠 Host SMTP: {}", mailHost);
        log.info("🚪 Puerto SMTP: {}", mailPort);
        log.info("👤 Usuario: {}", mailUsername);
        log.info("🔑 Contraseña configurada: {}", (mailPassword != null && !mailPassword.isEmpty()) ? "SÍ" : "NO");
        log.info("📬 JavaMailSender disponible: {}", mailSender != null ? "SÍ" : "NO");

        if (mailSender != null) {
            try {
                // Obtener propiedades de la sesión si es JavaMailSenderImpl
                if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl) {
                    org.springframework.mail.javamail.JavaMailSenderImpl senderImpl =
                        (org.springframework.mail.javamail.JavaMailSenderImpl) mailSender;
                    Properties props = senderImpl.getJavaMailProperties();
                    log.info("🔧 Propiedades SMTP:");
                    props.forEach((key, value) -> log.info("   {} = {}", key, value));
                } else {
                    log.info("🔧 JavaMailSender no es una implementación estándar, no se pueden obtener propiedades");
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudieron obtener las propiedades SMTP: {}", e.getMessage());
            }
        }
        log.info("🔍 === FIN DIAGNÓSTICO DE EMAIL ===");
    }

    public boolean testEmailConnection() {
        if (mailSender == null) {
            log.error("❌ JavaMailSender no está disponible");
            return false;
        }

        try {
            log.info("🧪 Probando conexión SMTP...");

            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(mailUsername);
            testMessage.setTo("javierbarcelo2106@gmail.com");
            testMessage.setSubject("✅ Test de Conexión SMTP - PelisApp");
            testMessage.setText("Este es un email de prueba para verificar que la conexión SMTP funciona correctamente.\n\n" +
                    "Si recibes este email, la configuración está funcionando.\n\n" +
                    "Enviado desde PelisApp - " + java.time.LocalDateTime.now());

            mailSender.send(testMessage);
            log.info("✅ Email de prueba enviado exitosamente");
            return true;

        } catch (Exception e) {
            log.error("❌ Error en test de conexión SMTP: {}", e.getMessage());
            log.error("❌ Detalles del error: ", e);
            return false;
        }
    }

    public boolean sendConfirmationTestEmail(String toEmail, String username) {
        if (mailSender == null) {
            log.error("❌ JavaMailSender no está disponible");
            return false;
        }

        try {
            log.info("📧 Enviando email de confirmación de prueba a: {}", toEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(mailUsername);
            helper.setTo(toEmail);
            helper.setSubject("🎬 [PRUEBA] Confirma tu cuenta en PelisApp");

            String testToken = "TEST-TOKEN-" + System.currentTimeMillis();
            String confirmationUrl = "http://localhost:8080/confirm-account?token=" + testToken;

            String htmlContent = createTestConfirmationHTML(username, confirmationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✅ Email de confirmación de prueba enviado a: {}", toEmail);
            return true;

        } catch (MessagingException e) {
            log.error("❌ Error enviando email de prueba a {}: {}", toEmail, e.getMessage());
            log.error("❌ Detalles del error: ", e);
            return false;
        }
    }

    private String createTestConfirmationHTML(String username, String confirmationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Email de Prueba - PelisApp</title>
            </head>
            <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #333; margin: 0;">🧪 EMAIL DE PRUEBA</h1>
                        <h2 style="color: #666; margin: 10px 0 0 0;">PelisApp</h2>
                    </div>
                    
                    <div style="background-color: #e3f2fd; padding: 20px; border-radius: 8px; margin-bottom: 30px;">
                        <h3 style="color: #1976d2; margin: 0 0 10px 0;">✅ ¡Configuración de Email Funcionando!</h3>
                        <p style="margin: 0; color: #333;">
                            Si estás leyendo este email, significa que la configuración SMTP está funcionando correctamente.
                        </p>
                    </div>
                    
                    <div>
                        <h3>Hola %s!</h3>
                        <p>Este es un email de prueba para verificar que el sistema de confirmación funciona.</p>
                        <p>En un registro real, harías clic en el siguiente enlace:</p>
                        
                        <div style="text-align: center; margin: 25px 0;">
                            <a href="%s" style="display: inline-block; background-color: #4CAF50; color: white; text-decoration: none; padding: 12px 25px; border-radius: 5px; font-weight: bold;">
                                🔗 Enlace de Confirmación (PRUEBA)
                            </a>
                        </div>
                        
                        <p><strong>URL del enlace:</strong></p>
                        <p style="word-break: break-all; background-color: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace;">%s</p>
                    </div>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666;">
                        <p style="margin: 0;">Email enviado desde PelisApp - %s</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, confirmationUrl, confirmationUrl, java.time.LocalDateTime.now());
    }

    public String getEmailStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== ESTADO DEL SERVICIO DE EMAIL ===\n");
        status.append("Email habilitado: ").append(emailEnabled ? "✅ SÍ" : "❌ NO").append("\n");
        status.append("JavaMailSender: ").append(mailSender != null ? "✅ Disponible" : "❌ No disponible").append("\n");
        status.append("Host SMTP: ").append(mailHost).append("\n");
        status.append("Puerto: ").append(mailPort).append("\n");
        status.append("Usuario: ").append(mailUsername).append("\n");
        status.append("Contraseña: ").append((mailPassword != null && !mailPassword.isEmpty()) ? "✅ Configurada" : "❌ No configurada").append("\n");
        return status.toString();
    }
}
