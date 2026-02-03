package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para probar la configuración de email
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final IEmailService emailService;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:no-configurado}")
    private String emailUsername;

    /**
     * Endpoint para verificar la configuración de email
     */
    @GetMapping("/email-config")
    public Map<String, Object> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put("emailEnabled", emailEnabled);
        config.put("emailUsername", emailUsername);
        config.put("emailServiceClass", emailService.getClass().getSimpleName());

        if (emailEnabled) {
            config.put("status", "✅ Email HABILITADO - Se enviarán emails reales");
            config.put("mode", "REAL");
        } else {
            config.put("status", "🔧 Email DESHABILITADO - Modo simulado (ver consola)");
            config.put("mode", "MOCK");
        }

        return config;
    }

    /**
     * Endpoint para enviar un email de prueba
     */
    @PostMapping("/send-test-email")
    public Map<String, Object> sendTestEmail(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Enviar email de prueba
            emailService.sendConfirmationEmail(
                email,
                "test_user",
                "test_token_12345"
            );

            result.put("success", true);
            if (emailEnabled) {
                result.put("message", "✅ Email de prueba enviado a: " + email + ". Revisa tu bandeja de entrada.");
            } else {
                result.put("message", "🔧 Email simulado enviado. Revisa la consola del servidor.");
            }

            log.info("Email de prueba enviado a: {}", email);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "❌ Error enviando email: " + e.getMessage());
            log.error("Error enviando email de prueba a {}: {}", email, e.getMessage());
        }

        return result;
    }

    /**
     * Endpoint para generar un enlace de confirmación de prueba
     */
    @GetMapping("/generate-test-link")
    public Map<String, Object> generateTestLink() {
        Map<String, Object> result = new HashMap<>();

        String testToken = "test_token_" + System.currentTimeMillis();
        String confirmationUrl = "http://localhost:8080/confirm-account?token=" + testToken;

        result.put("token", testToken);
        result.put("url", confirmationUrl);
        result.put("instructions", "Este es un enlace de prueba. En el registro real se genera un token JWT válido.");

        return result;
    }
}
