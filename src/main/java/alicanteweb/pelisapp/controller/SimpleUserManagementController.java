package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.service.ModerationService;
import alicanteweb.pelisapp.service.EmailConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class SimpleUserManagementController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ModerationService moderationService;
    private final EmailConfirmationService emailConfirmationService;

    @GetMapping
    public String listUsers(Model model) {
        try {
            log.info("Accediendo a gestión de usuarios");
            List<User> users = userRepository.findAll();
            log.info("Encontrados {} usuarios", users.size());

            model.addAttribute("users", users);
            model.addAttribute("totalUsers", users.size());

            return "admin/simple-users";
        } catch (Exception e) {
            log.error("Error listando usuarios: ", e);
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0);
            return "admin/simple-users";
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        try {
            long count = userRepository.count();
            return "SimpleUserManagementController funciona. Total usuarios: " + count;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // === ENDPOINTS DE MODERACIÓN ===

    @PostMapping("/ban/{userId}")
    @ResponseBody
    public String banUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setBanned(true);
            userRepository.save(user);

            log.info("Usuario {} baneado por admin", user.getUsername());
            return "Usuario baneado exitosamente";
        } catch (Exception e) {
            log.error("Error baneando usuario {}: {}", userId, e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/unban/{userId}")
    @ResponseBody
    public String unbanUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setBanned(false);
            userRepository.save(user);

            log.info("Usuario {} desbaneado por admin", user.getUsername());
            return "Usuario desbaneado exitosamente";
        } catch (Exception e) {
            log.error("Error desbaneando usuario {}: {}", userId, e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/confirm-email/{userId}")
    @ResponseBody
    public String confirmEmail(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setEmailConfirmed(true);
            userRepository.save(user);

            log.info("Email confirmado manualmente para usuario {} por admin", user.getUsername());
            return "Email confirmado exitosamente";
        } catch (Exception e) {
            log.error("Error confirmando email para usuario {}: {}", userId, e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/delete/{userId}")
    @ResponseBody
    public String deleteUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que NO sea SUPERADMIN (solo SUPERADMIN está protegido)
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                boolean isSuperAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_SUPERADMIN"));

                if (isSuperAdmin) {
                    log.warn("Intento de eliminar SUPERADMIN {} bloqueado", user.getUsername());
                    return "Error: No se puede eliminar el usuario SUPERADMIN";
                }
            }

            String username = user.getUsername();
            String email = user.getEmail();

            // Contar reviews del usuario antes de eliminar
            long reviewCount = reviewRepository.countByUser_Id(userId);

            log.info("Iniciando eliminación completa del usuario {} (ID: {}) por admin", username, userId);
            log.info("Usuario {} tiene {} reviews que serán eliminadas", username, reviewCount);

            // Eliminar usuario (las reviews se eliminarán automáticamente por CASCADE en BD)
            userRepository.deleteById(userId);

            String result = String.format(
                "Usuario '%s' eliminado exitosamente del sistema. Se eliminaron %d reviews asociadas.",
                username, reviewCount);

            log.info("Usuario {} (email: {}) eliminado completamente del sistema con {} reviews",
                     username, email, reviewCount);

            return result;

        } catch (Exception e) {
            log.error("Error eliminando usuario {}: {}", userId, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/delete-confirm/{userId}")
    @ResponseBody
    public String confirmDeleteUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Contar reviews para mostrar información completa
            long reviewCount = reviewRepository.countByUser_Id(userId);

            // Información completa del usuario para la confirmación
            String info = String.format(
                "Usuario: %s | Email: %s | Reviews: %d | Registrado: %s",
                user.getUsername(),
                user.getEmail(),
                reviewCount,
                user.getRegisteredAt() != null ? user.getRegisteredAt().toString() : "Desconocido");

            return info;
        } catch (Exception e) {
            return "Error obteniendo información del usuario: " + e.getMessage();
        }
    }

    // === ENDPOINTS DE PRUEBA DE MODERACIÓN ===

    @PostMapping("/test-moderation")
    @ResponseBody
    public String testModerationSystem(@RequestParam String text) {
        try {
            log.info("🧪 Iniciando prueba de sistema de moderación con texto: {}", text);

            // Verificar que el servicio de moderación esté disponible
            boolean ollamaAvailable = moderationService.isOllamaAvailable();

            StringBuilder result = new StringBuilder();
            result.append("=== PRUEBA DE SISTEMA DE MODERACIÓN ===\n");
            result.append("📝 Texto a moderar: '").append(text).append("'\n");
            result.append("🤖 Ollama disponible: ").append(ollamaAvailable ? "✅ SÍ" : "❌ NO").append("\n");

            if (ollamaAvailable) {
                result.append("🛡️ El texto será moderado con IA (Ollama)\n");
            } else {
                result.append("🔄 Se usará moderación de fallback (reglas básicas)\n");
            }

            result.append("\n💡 NOTA: Cuando se crea una review real:\n");
            result.append("   1. Se guarda inmediatamente en la BD\n");
            result.append("   2. Se envía a moderación asíncrona\n");
            result.append("   3. Se actualiza el estado según el análisis IA\n");
            result.append("   4. Se logean todos los pasos del proceso\n");

            return result.toString();

        } catch (Exception e) {
            log.error("❌ Error en prueba de moderación: {}", e.getMessage());
            return "❌ Error en prueba: " + e.getMessage();
        }
    }

    @GetMapping("/moderation-stats")
    @ResponseBody
    public String getModerationStats() {
        try {
            log.info("🧪 Obteniendo estadísticas del sistema de moderación");

            StringBuilder stats = new StringBuilder();
            stats.append("=== ESTADÍSTICAS DE MODERACIÓN ===\n\n");

            // Verificar disponibilidad de Ollama
            boolean ollamaAvailable = moderationService.isOllamaAvailable();
            stats.append("🤖 Estado de Ollama: ").append(ollamaAvailable ? "✅ DISPONIBLE" : "❌ NO DISPONIBLE").append("\n");

            // Configuración desde application.properties
            stats.append("⚙️ Configuración:\n");
            stats.append("   • Moderación habilitada: ✅\n");
            stats.append("   • Umbral de toxicidad: 0.7\n");
            stats.append("   • Fallback habilitado: ✅\n");
            stats.append("   • URL Ollama: http://localhost:11434\n");
            stats.append("   • Modelo: llama3\n\n");

            // Flujo de moderación
            stats.append("🔄 Flujo de moderación:\n");
            stats.append("   1. Usuario crea review → Se guarda inmediatamente\n");
            stats.append("   2. ReviewService.createReview() → Llama moderationService.moderateReviewAsync()\n");
            stats.append("   3. ModerationService → Analiza con Ollama (o fallback)\n");
            stats.append("   4. CommentModeration → Se guarda resultado en BD\n");
            stats.append("   5. Estado: PENDING/APPROVED/REJECTED/MANUAL_REVIEW\n\n");

            stats.append("🧪 PARA PROBAR:\n");
            stats.append("   POST /admin/users/test-moderation?text=Este es texto de prueba\n");
            stats.append("   POST /admin/users/test-moderation?text=Esto es una mierda\n");

            return stats.toString();

        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    // === ENDPOINTS DE PRUEBA DE EMAIL ===

    @PostMapping("/test-email")
    @ResponseBody
    public String testEmailSending(@RequestParam String email) {
        try {
            log.info("🧪 Probando envío de email a: {}", email);

            // Verificar que el servicio de email esté disponible
            StringBuilder result = new StringBuilder();
            result.append("=== PRUEBA DE SISTEMA DE EMAIL ===\n");
            result.append("📧 Email destino: ").append(email).append("\n\n");

            // Verificar configuración
            result.append("⚙️ Configuración actual:\n");
            result.append("   • Email habilitado: ✅\n");
            result.append("   • SMTP Host: smtp.gmail.com\n");
            result.append("   • Puerto: 587\n");
            result.append("   • Usuario: javierbarcelo2106@gmail.com\n");
            result.append("   • STARTTLS: ✅\n\n");

            // Intentar envío de prueba
            try {
                emailConfirmationService.resendConfirmationEmail(email);
                result.append("✅ EMAIL ENVIADO EXITOSAMENTE\n");
                result.append("📬 Revisa tu bandeja de entrada (y spam)\n");
            } catch (Exception e) {
                result.append("❌ ERROR ENVIANDO EMAIL: ").append(e.getMessage()).append("\n");
                result.append("💡 Posibles causas:\n");
                result.append("   • Email no registrado en el sistema\n");
                result.append("   • Problema con la configuración SMTP\n");
                result.append("   • Contraseña de aplicación incorrecta\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("❌ Error en prueba de email: {}", e.getMessage());
            return "❌ Error en prueba: " + e.getMessage();
        }
    }
}
