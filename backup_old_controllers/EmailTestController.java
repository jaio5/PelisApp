package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.EmailConfirmationService;
import alicanteweb.pelisapp.service.IEmailService;
import alicanteweb.pelisapp.service.EmailDiagnosticService;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para pruebas públicas de email - SIN autenticación
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final IEmailService emailService;
    private final UserRepository userRepository;
    private final EmailDiagnosticService emailDiagnosticService;

    @Autowired(required = false)
    private EmailConfirmationService emailConfirmationService;

    @PostMapping("/test-email-direct")
    public ResponseEntity<String> testEmailDirect(@RequestParam String email) {
        try {
            log.info("🧪 [EMAIL TEST] Probando envío directo de email a: {}", email);

            StringBuilder result = new StringBuilder();
            result.append("=== PRUEBA DIRECTA DE EMAIL ===\n");
            result.append("📧 Email destino: ").append(email).append("\n");
            result.append("🔧 Probando envío directo con IEmailService...\n\n");

            try {
                // Crear token temporal para prueba
                String testToken = "test-token-123456789";

                // Enviar email directamente
                emailService.sendConfirmationEmail(email, "UsuarioPrueba", testToken);

                result.append("✅ EMAIL ENVIADO EXITOSAMENTE!\n");
                result.append("📬 Revisa tu bandeja de entrada en: ").append(email).append("\n");
                result.append("📁 También revisa la carpeta de SPAM\n");
                result.append("🔗 El enlace de confirmación contiene el token: ").append(testToken).append("\n");

                log.info("✅ [EMAIL TEST] Email enviado exitosamente a: {}", email);
                return ResponseEntity.ok(result.toString());

            } catch (Exception e) {
                result.append("❌ ERROR AL ENVIAR EMAIL: ").append(e.getMessage()).append("\n");
                result.append("🔧 Revisa los logs para más detalles\n");
                log.error("❌ [EMAIL TEST] Error enviando email: {}", e.getMessage(), e);
                return ResponseEntity.ok(result.toString());
            }

        } catch (Exception e) {
            log.error("❌ [EMAIL TEST] Error general: {}", e.getMessage(), e);
            return ResponseEntity.ok("❌ ERROR GENERAL: " + e.getMessage());
        }
    }

    @GetMapping("/email-diagnostic")
    public ResponseEntity<String> emailDiagnostic() {
        log.info("🔍 [EMAIL DIAGNOSTIC] Iniciando diagnóstico de email...");

        StringBuilder response = new StringBuilder();
        response.append("🔍 === DIAGNÓSTICO COMPLETO DE EMAIL ===\n\n");

        // Estado del servicio
        response.append(emailDiagnosticService.getEmailStatus()).append("\n\n");

        // Test de conexión
        response.append("🧪 === PRUEBA DE CONEXIÓN ===\n");
        boolean connectionTest = emailDiagnosticService.testEmailConnection();
        response.append("Resultado: ").append(connectionTest ? "✅ ÉXITO" : "❌ FALLO").append("\n\n");

        if (connectionTest) {
            response.append("✅ La configuración de email está funcionando correctamente!\n");
            response.append("📧 Deberías haber recibido un email de prueba en javierbarcelo2106@gmail.com\n");
        } else {
            response.append("❌ Hay problemas con la configuración de email.\n");
            response.append("🔧 Revisa los logs para más detalles.\n");
        }

        log.info("🔍 [EMAIL DIAGNOSTIC] Diagnóstico completado. Conexión: {}", connectionTest);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/test-confirmation-email")
    public ResponseEntity<String> testConfirmationEmail(@RequestParam String email,
                                                       @RequestParam(defaultValue = "UsuarioPrueba") String username) {
        log.info("📧 [EMAIL TEST] Probando email de confirmación a: {} para usuario: {}", email, username);

        StringBuilder response = new StringBuilder();
        response.append("📧 === PRUEBA DE EMAIL DE CONFIRMACIÓN ===\n\n");
        response.append("Destinatario: ").append(email).append("\n");
        response.append("Usuario: ").append(username).append("\n\n");

        boolean success = emailDiagnosticService.sendConfirmationTestEmail(email, username);

        if (success) {
            response.append("✅ Email de confirmación enviado exitosamente!\n");
            response.append("📬 Revisa tu bandeja de entrada y la carpeta de spam\n");
            response.append("🔗 El email contiene un enlace de confirmación de prueba\n");
        } else {
            response.append("❌ Error al enviar el email de confirmación\n");
            response.append("🔧 Revisa los logs del servidor para más detalles\n");
        }

        log.info("📧 [EMAIL TEST] Resultado del envío: {}", success);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/test-user-confirmation")
    public ResponseEntity<String> testUserConfirmation(@RequestParam String username) {
        try {
            log.info("👤 [USER TEST] Probando confirmación para usuario existente: {}", username);

            StringBuilder result = new StringBuilder();
            result.append("=== PRUEBA DE CONFIRMACIÓN DE USUARIO EXISTENTE ===\n");
            result.append("👤 Usuario: ").append(username).append("\n\n");

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                result.append("❌ Usuario no encontrado\n");
                return ResponseEntity.ok(result.toString());
            }

            result.append("📧 Email del usuario: ").append(user.getEmail()).append("\n");
            result.append("✅ Estado confirmación: ").append(user.isEmailConfirmed() ? "CONFIRMADO" : "PENDIENTE").append("\n\n");

            if (emailConfirmationService != null) {
                try {
                    String token = emailConfirmationService.generateConfirmationToken(user);
                    emailConfirmationService.sendConfirmationEmail(user, token);
                    result.append("✅ EMAIL DE CONFIRMACIÓN REENVIADO!\n");
                    result.append("📬 Revisa la bandeja de: ").append(user.getEmail()).append("\n");
                } catch (Exception e) {
                    result.append("❌ Error enviando confirmación: ").append(e.getMessage()).append("\n");
                }
            } else {
                result.append("❌ EmailConfirmationService no disponible\n");
            }

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            log.error("❌ [USER TEST] Error: {}", e.getMessage(), e);
            return ResponseEntity.ok("❌ ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/email-config-info")
    public ResponseEntity<String> getEmailConfigInfo() {
        try {
            StringBuilder info = new StringBuilder();
            info.append("=== INFORMACIÓN DE CONFIGURACIÓN EMAIL ===\n\n");

            // Verificar si el servicio está disponible
            if (emailService != null) {
                info.append("✅ IEmailService está disponible: ").append(emailService.getClass().getSimpleName()).append("\n");
            } else {
                info.append("❌ IEmailService NO está disponible\n");
            }

            if (emailConfirmationService != null) {
                info.append("✅ EmailConfirmationService está disponible\n");
            } else {
                info.append("❌ EmailConfirmationService NO está disponible\n");
            }

            if (emailDiagnosticService != null) {
                info.append("✅ EmailDiagnosticService está disponible\n");
            } else {
                info.append("❌ EmailDiagnosticService NO está disponible\n");
            }

            info.append("\n📋 Para verificar configuración completa:\n");
            info.append("   • Revisa los logs de arranque de Spring Boot\n");
            info.append("   • Busca mensajes de configuración de JavaMailSender\n");
            info.append("   • Verifica que RealEmailService esté activo\n");

            return ResponseEntity.ok(info.toString());

        } catch (Exception e) {
            return ResponseEntity.ok("❌ Error obteniendo información: " + e.getMessage());
        }
    }
}
