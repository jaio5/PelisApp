package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.CommentModeration;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.repository.CommentModerationRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para administración del sistema de moderación con Ollama
 */
@RestController
@RequestMapping("/admin/moderation")
@RequiredArgsConstructor
@Slf4j
public class ModerationController {

    private final ModerationService moderationService;
    private final CommentModerationRepository commentModerationRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Obtener estadísticas del sistema de moderación
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getModerationStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_moderations", commentModerationRepository.count());
        stats.put("pending", commentModerationRepository.countByStatus(CommentModeration.ModerationStatus.PENDING));
        stats.put("approved", commentModerationRepository.countByStatus(CommentModeration.ModerationStatus.APPROVED));
        stats.put("rejected", commentModerationRepository.countByStatus(CommentModeration.ModerationStatus.REJECTED));
        stats.put("manual_review", commentModerationRepository.countByStatus(CommentModeration.ModerationStatus.MANUAL_REVIEW));
        stats.put("ollama_available", moderationService.isOllamaAvailable());

        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener reseñas pendientes de moderación manual
     */
    @GetMapping("/pending")
    public ResponseEntity<List<CommentModeration>> getPendingModerations(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<CommentModeration> pending = commentModerationRepository
            .findByStatus(CommentModeration.ModerationStatus.MANUAL_REVIEW, pageRequest)
            .getContent();

        return ResponseEntity.ok(pending);
    }

    /**
     * Probar moderación de un texto específico
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testModeration(@RequestParam String text) {
        log.info("🧪 Test de moderación solicitado para texto: {}", text);

        Map<String, Object> response = new HashMap<>();

        try {
            // Crear una reseña temporal para probar
            Review tempReview = new Review();
            tempReview.setText(text);
            tempReview.setId(-1L); // ID temporal

            // Simular moderación (sin guardar en BD)
            boolean ollamaAvailable = moderationService.isOllamaAvailable();
            response.put("ollama_available", ollamaAvailable);
            response.put("test_text", text);
            response.put("status", "SUCCESS");

            if (ollamaAvailable) {
                response.put("message", "✅ Ollama disponible - El texto sería moderado con IA");
            } else {
                response.put("message", "⚠️ Ollama no disponible - Se usaría moderación de fallback");
            }

            log.info("✅ Test de moderación completado");

        } catch (Exception e) {
            log.error("❌ Error en test de moderación: {}", e.getMessage());
            response.put("status", "ERROR");
            response.put("message", "❌ Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Moderar una reseña específica manualmente
     */
    @PostMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> moderateReview(@PathVariable Long reviewId) {
        log.info("🛡️ Moderación manual solicitada para reseña ID: {}", reviewId);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "❌ Reseña no encontrada");
                return ResponseEntity.badRequest().body(response);
            }

            Review review = reviewOpt.get();

            // Verificar si ya tiene moderación
            Optional<CommentModeration> existingModeration =
                commentModerationRepository.findByReview_Id(reviewId);

            if (existingModeration.isPresent()) {
                response.put("status", "INFO");
                response.put("message", "⚠️ La reseña ya tiene moderación");
                response.put("current_status", existingModeration.get().getStatus());
                response.put("current_score", existingModeration.get().getToxicityScore());
                return ResponseEntity.ok(response);
            }

            // Ejecutar moderación asíncrona
            moderationService.moderateReviewAsync(review)
                .thenAccept(moderation -> {
                    log.info("✅ Moderación completada para reseña ID: {} - Estado: {}",
                            reviewId, moderation.getStatus());
                });

            response.put("status", "SUCCESS");
            response.put("message", "✅ Moderación iniciada - El resultado se procesará asíncronamente");
            response.put("review_id", reviewId);

        } catch (Exception e) {
            log.error("❌ Error moderando reseña ID {}: {}", reviewId, e.getMessage());
            response.put("status", "ERROR");
            response.put("message", "❌ Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Verificar estado de Ollama
     */
    @GetMapping("/ollama-status")
    public ResponseEntity<Map<String, Object>> checkOllamaStatus() {
        Map<String, Object> status = new HashMap<>();

        boolean available = moderationService.isOllamaAvailable();
        status.put("available", available);
        status.put("status", available ? "✅ CONECTADO" : "❌ NO DISPONIBLE");
        status.put("message", available
            ? "Ollama está ejecutándose y disponible para moderación"
            : "Ollama no está disponible. Verifica que esté ejecutándose en localhost:11434");

        return ResponseEntity.ok(status);
    }

    /**
     * Obtener detalles de moderación de una reseña específica
     */
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> getModerationDetails(@PathVariable Long reviewId) {
        Map<String, Object> response = new HashMap<>();

        Optional<CommentModeration> moderation = commentModerationRepository.findByReview_Id(reviewId);

        if (moderation.isPresent()) {
            CommentModeration mod = moderation.get();
            response.put("status", "FOUND");
            response.put("moderation_status", mod.getStatus());
            response.put("toxicity_score", mod.getToxicityScore());
            response.put("reason", mod.getModerationReason());
            response.put("ai_processed", mod.getAiProcessed());
            response.put("created_at", mod.getCreatedAt());
            response.put("reviewed_at", mod.getReviewedAt());
        } else {
            response.put("status", "NOT_FOUND");
            response.put("message", "No se encontró moderación para esta reseña");
        }

        return ResponseEntity.ok(response);
    }
}
