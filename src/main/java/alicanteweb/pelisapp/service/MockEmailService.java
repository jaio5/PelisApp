package alicanteweb.pelisapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock del servicio de email para desarrollo.
 * Se activa cuando app.email.enabled=false O cuando no hay configuración de mail
 */
@Service("mockEmailService")
@Slf4j
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false", matchIfMissing = true)
public class MockEmailService implements IEmailService {

    @Override
    public void sendConfirmationEmail(String toEmail, String username, String confirmationToken) {
        log.info("📧 [MOCK EMAIL] Enviando confirmación a: {}", toEmail);
        log.info("👤 Usuario: {}", username);
        log.info("🔗 URL de confirmación: http://localhost:8080/confirm-account?token={}", confirmationToken);
        log.info("✅ [SIMULADO] Email enviado exitosamente");

        // En desarrollo, puedes copiar la URL del log y pegarla en el navegador
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📧 EMAIL DE CONFIRMACIÓN SIMULADO");
        System.out.println("=".repeat(80));
        System.out.println("Para: " + toEmail);
        System.out.println("Usuario: " + username);
        System.out.println("URL de confirmación:");
        System.out.println("http://localhost:8080/confirm-account?token=" + confirmationToken);
        System.out.println("=".repeat(80) + "\n");
    }

    @Override
    public void sendSimpleConfirmationEmail(String toEmail, String username, String confirmationToken) {
        sendConfirmationEmail(toEmail, username, confirmationToken);
    }
}

