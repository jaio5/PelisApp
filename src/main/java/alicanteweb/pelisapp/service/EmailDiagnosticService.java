package alicanteweb.pelisapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Properties;

@Service
@Slf4j
public class EmailDiagnosticService {

    private final JavaMailSender mailSender;

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

    public EmailDiagnosticService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void diagnosticEmailConfiguration() {
        log.info("🔍 === DIAGNÓSTICO DE CONFIGURACIÓN DE EMAIL ===");
        log.info("✉️ Email habilitado: {}", emailEnabled);
        log.info("🏠 Host SMTP: {}", mailHost);
        log.info("🚪 Puerto SMTP: {}", mailPort);
        log.info("👤 Usuario: {}", mailUsername);
        log.info("🔑 Contraseña configurada: {}", (mailPassword != null && !mailPassword.isEmpty()) ? "SÍ" : "NO");
        log.info("📬 JavaMailSender disponible: {}", mailSender != null ? "SÍ" : "NO");

        if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl senderImpl) {
            Properties props = senderImpl.getJavaMailProperties();
            log.info("🔧 Propiedades SMTP:");
            props.forEach((key, value) -> log.info("   {} = {}", key, value));
        } else if (mailSender != null) {
            log.info("🔧 JavaMailSender no es una implementación estándar, no se pueden obtener propiedades");
        }
        log.info("🔍 === FIN DIAGNÓSTICO DE EMAIL ===");
    }
}
