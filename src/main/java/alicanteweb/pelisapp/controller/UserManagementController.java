package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.repository.ReviewLikeRepository;
import alicanteweb.pelisapp.repository.CommentModerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador temporal para administración de usuarios - SOLO PARA DESARROLLO
 */
@RestController
@RequestMapping("/admin/users-management")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CommentModerationRepository commentModerationRepository;

    /**
     * Listar todos los usuarios existentes
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAllUsers() {
        List<User> users = userRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("total_users", users.size());
        response.put("users", users.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("registeredAt", user.getRegisteredAt());
            userInfo.put("emailConfirmed", user.isEmailConfirmed());
            return userInfo;
        }).toList());

        log.info("📋 Listando {} usuarios existentes", users.size());
        return ResponseEntity.ok(response);
    }

    /**
     * BORRAR TODOS LOS USUARIOS - ¡CUIDADO! Esta acción es irreversible
     */
    @DeleteMapping("/delete-all")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteAllUsers(@RequestParam(required = false) String confirm) {

        if (!"YES_DELETE_ALL".equals(confirm)) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Se requiere confirmación");
            response.put("message", "Para confirmar, usa: ?confirm=YES_DELETE_ALL");
            response.put("warning", "⚠️ Esta acción BORRARÁ TODOS LOS USUARIOS y es irreversible");
            return ResponseEntity.badRequest().body(response);
        }

        List<User> users = userRepository.findAll();
        int userCount = users.size();

        log.warn("🚨 INICIANDO BORRADO DE TODOS LOS USUARIOS ({} usuarios)", userCount);

        try {
            // Borrar datos relacionados primero para evitar violaciones de foreign key
            for (User user : users) {
                // Borrar moderaciones de comentarios
                commentModerationRepository.deleteAll(
                    commentModerationRepository.findAll().stream()
                        .filter(mod -> mod.getReview() != null &&
                                     mod.getReview().getUser() != null &&
                                     mod.getReview().getUser().getId().equals(user.getId()))
                        .toList()
                );

                // Borrar likes de reviews
                reviewLikeRepository.deleteAll(
                    reviewLikeRepository.findAll().stream()
                        .filter(like -> like.getUser().getId().equals(user.getId()))
                        .toList()
                );

                // Borrar reviews del usuario
                reviewRepository.deleteAll(
                    reviewRepository.findAllByUser_Id(user.getId())
                );

                log.info("🗑️ Datos relacionados borrados para usuario: {}", user.getUsername());
            }

            // Finalmente borrar todos los usuarios
            userRepository.deleteAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Todos los usuarios han sido eliminados exitosamente");
            response.put("deleted_count", userCount);
            response.put("timestamp", java.time.Instant.now());

            log.warn("🗑️ BORRADO COMPLETADO: {} usuarios eliminados", userCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error borrando usuarios: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Borrar un usuario específico por ID
     */
    @DeleteMapping("/delete/{userId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUserById(@PathVariable Long userId) {

        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("error", "Usuario no encontrado");
            return ResponseEntity.notFound().build();
        }

        String username = user.getUsername();
        log.warn("🚨 BORRANDO usuario: {} (ID: {})", username, userId);

        try {
            // Borrar datos relacionados
            commentModerationRepository.deleteAll(
                commentModerationRepository.findAll().stream()
                    .filter(mod -> mod.getReview() != null &&
                                 mod.getReview().getUser() != null &&
                                 mod.getReview().getUser().getId().equals(userId))
                    .toList()
            );

            reviewLikeRepository.deleteAll(
                reviewLikeRepository.findAll().stream()
                    .filter(like -> like.getUser().getId().equals(userId))
                    .toList()
            );

            reviewRepository.deleteAll(reviewRepository.findAllByUser_Id(userId));

            // Borrar el usuario
            userRepository.delete(user);

            response.put("success", true);
            response.put("message", "✅ Usuario eliminado exitosamente");
            response.put("deleted_user", username);
            response.put("user_id", userId);

            log.warn("🗑️ Usuario eliminado: {}", username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error borrando usuario {}: {}", username, e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Error interno del servidor");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener información detallada de un usuario
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable Long userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("registeredAt", user.getRegisteredAt());
        userInfo.put("emailConfirmed", user.isEmailConfirmed());
        userInfo.put("criticLevel", user.getCriticLevel());

        // Contar datos relacionados
        long reviewCount = reviewRepository.countByUser_Id(userId);
        long likeCount = reviewLikeRepository.findAll().stream()
            .filter(like -> like.getUser().getId().equals(userId))
            .count();

        userInfo.put("review_count", reviewCount);
        userInfo.put("like_count", likeCount);

        return ResponseEntity.ok(userInfo);
    }
}
