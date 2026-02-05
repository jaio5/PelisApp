package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.EmailConfirmationService;
import alicanteweb.pelisapp.service.IEmailService;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired(required = false)
    private EmailConfirmationService emailConfirmationService;

    @PostMapping("/test-email-direct")
    public String testEmailDirect(@RequestParam String email) {
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

            } catch (Exception e) {
                result.append("❌ ERROR ENVIANDO EMAIL: ").append(e.getMessage()).append("\n");
                result.append("💡 Detalles del error:\n");
                result.append("   Tipo: ").append(e.getClass().getSimpleName()).append("\n");
                result.append("   Causa: ").append(e.getCause() != null ? e.getCause().getMessage() : "N/A").append("\n");

                log.error("❌ [EMAIL TEST] Error enviando email: {}", e.getMessage(), e);
            }

            return result.toString();

        } catch (Exception e) {
            log.error("❌ [EMAIL TEST] Error general en prueba de email: {}", e.getMessage(), e);
            return "❌ Error general: " + e.getMessage();
        }
    }

    @GetMapping("/email-config-info")
    public String getEmailConfigInfo() {
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

            info.append("\n📋 Para verificar configuración completa:\n");
            info.append("   • Revisa los logs de arranque de Spring Boot\n");
            info.append("   • Busca mensajes de configuración de JavaMailSender\n");
            info.append("   • Verifica que RealEmailService esté activo\n");

            return info.toString();

        } catch (Exception e) {
            return "❌ Error obteniendo información: " + e.getMessage();
        }
    }
}
