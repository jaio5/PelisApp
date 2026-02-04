package alicanteweb.pelisapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.annotation.PostConstruct;
import java.util.Properties;

/**
 * Configuración de email para asegurar que JavaMailSender esté correctamente configurado
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true")
public class EmailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:#{null}}")
    private String username;

    @Value("${spring.mail.password:#{null}}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean starttlsEnabled;

    @PostConstruct
    public void logEmailConfiguration() {
        log.info("🔧 Configuración de Email:");
        log.info("  📧 Host: {}", host);
        log.info("  🔌 Puerto: {}", port);
        log.info("  👤 Usuario: {}", username != null ? username : "❌ NO CONFIGURADO");
        log.info("  🔐 Contraseña: {}", password != null && !password.isEmpty() ? "✅ CONFIGURADA (" + password.length() + " caracteres)" : "❌ NO CONFIGURADA");
        log.info("  🔒 SMTP Auth: {}", smtpAuth);
        log.info("  🔐 StartTLS: {}", starttlsEnabled);

        if (username == null || username.isEmpty()) {
            log.error("❌ ERROR: spring.mail.username no está configurado.");
            log.error("   💡 Configura tu email de Gmail en las variables de entorno o application.properties");
        }

        if (password == null || password.isEmpty()) {
            log.error("❌ ERROR: spring.mail.password no está configurado.");
            log.error("   💡 Para Gmail, debes usar una 'Contraseña de aplicación', NO tu contraseña normal:");
            log.error("   📋 1. Ve a https://myaccount.google.com/security");
            log.error("   📋 2. Activa verificación en 2 pasos");
            log.error("   📋 3. En 'Contraseñas de aplicaciones', crea una nueva para 'PelisApp'");
            log.error("   📋 4. Usa esa contraseña de 16 caracteres en spring.mail.password");
        } else {
            // Validar formato de contraseña de aplicación de Gmail
            if (password.length() == 16 && password.matches("[a-z]+")) {
                log.info("✅ Formato de contraseña parece ser contraseña de aplicación de Gmail");
            } else if (password.length() != 16) {
                log.warn("⚠️  La contraseña tiene {} caracteres. Las contraseñas de aplicación de Gmail tienen exactamente 16.", password.length());
                log.warn("   💡 Si usas Gmail, asegúrate de usar una contraseña de aplicación");
            }
        }

        // Test de configuración básica
        if (username != null && password != null && !password.isEmpty()) {
            log.info("🚀 Configuración básica completa. Email habilitado para envío.");
        } else {
            log.error("🚫 Configuración incompleta. Email DESHABILITADO.");
        }
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnabled);
        props.put("mail.smtp.starttls.required", starttlsEnabled);
        props.put("mail.debug", "false");

        log.info("✅ JavaMailSender configurado correctamente");
        return mailSender;
    }
}
