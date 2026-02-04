package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.service.EmailConfirmationService;
import alicanteweb.pelisapp.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para testing de email
 */
@RestController
@RequestMapping("/api/email-test")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final IEmailService emailService;
    private final EmailConfirmationService emailConfirmationService;

    /**
     * Test básico de envío de email
     */
    @PostMapping("/send-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendTestEmail(@RequestParam String toEmail) {
        try {
            log.info("🧪 Iniciando test de email a: {}", toEmail);

            // Crear un usuario ficticio para el test
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail(toEmail);
            testUser.setEmailConfirmed(false);

            // Generar token de prueba
            String token = emailConfirmationService.generateConfirmationToken(testUser);

            // Enviar email de prueba
            emailService.sendConfirmationEmail(toEmail, "testuser", token);

            log.info("✅ Email de prueba enviado exitosamente");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email de prueba enviado exitosamente a " + toEmail,
                "token", token
            ));

        } catch (Exception e) {
            log.error("❌ Error enviando email de prueba: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "help", "Verifica la configuración de email en application.properties"
            ));
        }
    }

    /**
     * Verificar configuración de email
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEmailConfig() {
        return ResponseEntity.ok(Map.of(
            "message", "Revisa los logs del servidor para ver la configuración de email",
            "help", "Los detalles de configuración aparecen en los logs al iniciar la aplicación"
        ));
    }

    /**
     * Test simple de conectividad SMTP
     */
    @PostMapping("/test-connection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testSmtpConnection() {
        try {
            log.info("🧪 Probando conexión SMTP...");

            // Intentar enviar un email muy simple
            emailService.sendSimpleConfirmationEmail(
                "javierbarcelo2106@gmail.com",
                "admin",
                "test-token-123"
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conexión SMTP exitosa"
            ));

        } catch (Exception e) {
            log.error("❌ Error de conexión SMTP: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "suggestion", "Verifica si necesitas una 'Contraseña de aplicación' de Gmail"
            ));
        }
    }
}
