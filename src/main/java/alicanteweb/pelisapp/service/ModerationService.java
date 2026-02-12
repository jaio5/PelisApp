package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.CommentModeration;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.repository.CommentModerationRepository;
import alicanteweb.pelisapp.service.moderation.ContentAnalyzer;
import alicanteweb.pelisapp.service.moderation.OllamaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de moderación refactorizado usando principios SOLID.
 * Responsabilidades separadas:
 * - ContentAnalyzer: análisis de contenido con reglas
 * - OllamaClient: comunicación con IA
 * - ModerationService: coordinación y persistencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {

    private final CommentModerationRepository commentModerationRepository;
    private final ContentAnalyzer contentAnalyzer;
    private final OllamaClient ollamaClient;
    private final ModeratingAI moderatingAI;

    @Value("${app.moderation.toxicity.threshold:0.7}")
    private double toxicityThreshold;

    @Value("${app.moderation.enabled:true}")
    private boolean moderationEnabled;

    @Value("${app.moderation.fallback.enabled:true}")
    private boolean fallbackEnabled;

    @Value("${app.moderation.ia.threshold:0.7}")
    private double iaToxicityThreshold;

    @PostConstruct
    public void init() {
        log.info("🛡️ ModerationService inicializado:");
        log.info("  ⚖️ Umbral toxicidad: {}", toxicityThreshold);
        log.info("  ✅ Moderación activa: {}", moderationEnabled);
        log.info("  🔄 Fallback activo: {}", fallbackEnabled);
    }

    /**
     * Moderar una reseña de manera asíncrona.
     */
    @Async
    @Transactional
    public CompletableFuture<CommentModeration> moderateReviewAsync(Review review) {
        if (!moderationEnabled) {
            log.debug("Moderación deshabilitada, aprobando reseña ID: {}", review.getId());
            return CompletableFuture.completedFuture(createApprovedModeration(review));
        }

        log.info("🛡️ Iniciando moderación para reseña ID: {} - Usuario: {}",
                review.getId(), review.getUser().getUsername());

        CommentModeration moderation = createPendingModeration(review);

        try {
            // Intentar moderación con Ollama
            OllamaClient.OllamaAnalysisResult result = ollamaClient.analyzeContent(review.getText());

            moderation.setToxicityScore(result.toxicityScore());
            moderation.setModerationReason(result.reason());
            moderation.setAiProcessed(true);

            applyModerationDecision(moderation, result.toxicityScore(), review.getId());

        } catch (Exception e) {
            log.warn("⚠️ Error en Ollama, usando fallback - Reseña ID: {}, Error: {}",
                    review.getId(), e.getMessage());

            if (fallbackEnabled) {
                applyFallbackModeration(moderation, review.getText(), review.getId());
            } else {
                log.error("❌ Fallback deshabilitado y Ollama falló - Rechazando por seguridad");
                moderation.setStatus(CommentModeration.ModerationStatus.REJECTED);
                moderation.setModerationReason("Error en sistema de moderación");
            }
        }

        moderation.setReviewedAt(Instant.now());
        CommentModeration savedModeration = commentModerationRepository.save(moderation);
        return CompletableFuture.completedFuture(savedModeration);
    }

    /**
     * Moderación síncrona para bloquear contenido antes de publicar.
     */
    public ModerationResult moderateContentSync(String text) {
        if (!moderationEnabled) {
            log.debug("Moderación deshabilitada, aprobando contenido");
            return new ModerationResult(0.0, "Moderación deshabilitada", true);
        }

        try {
            // Intentar con Ollama primero
            OllamaClient.OllamaAnalysisResult result = ollamaClient.analyzeContent(text);

            if (result.toxicityScore() >= toxicityThreshold) {
                throw new ContentModerationException(
                    String.format("Contenido inapropiado detectado (puntuación: %.2f). %s",
                            result.toxicityScore(), result.reason()));
            }

            return new ModerationResult(result.toxicityScore(), result.reason(), true);

        } catch (OllamaClient.OllamaException e) {
            log.warn("Ollama no disponible, usando IA local ModeratingAI para moderación síncrona");

            // --- INTEGRACIÓN DE IA LOCAL ---
            double iaScore = moderatingAI.analyzeText(text);
            if (iaScore < iaToxicityThreshold) {
                throw new ContentModerationException(
                    String.format("Contenido inapropiado detectado por IA local (puntuación: %.2f).", iaScore));
            }
            return new ModerationResult(iaScore, "Moderación por IA local (ModeratingAI)", false);
        }
    }

    // Métodos privados de apoyo

    private CommentModeration createApprovedModeration(Review review) {
        CommentModeration moderation = new CommentModeration();
        moderation.setReview(review);
        moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
        moderation.setToxicityScore(0.0);
        moderation.setModerationReason("Moderación deshabilitada");
        moderation.setAiProcessed(false);
        moderation.setCreatedAt(Instant.now());
        moderation.setReviewedAt(Instant.now());
        return moderation;
    }

    private CommentModeration createPendingModeration(Review review) {
        CommentModeration moderation = new CommentModeration();
        moderation.setReview(review);
        moderation.setStatus(CommentModeration.ModerationStatus.PENDING);
        moderation.setCreatedAt(Instant.now());
        return moderation;
    }

    private void applyModerationDecision(CommentModeration moderation, double toxicityScore, Long reviewId) {
        if (toxicityScore >= toxicityThreshold) {
            moderation.setStatus(CommentModeration.ModerationStatus.REJECTED);
            log.warn("❌ Reseña rechazada por IA - ID: {}, Puntuación: {}", reviewId, String.format("%.2f", toxicityScore));
        } else if (toxicityScore >= 0.5) {
            moderation.setStatus(CommentModeration.ModerationStatus.MANUAL_REVIEW);
            log.info("⚠️ Reseña marcada para revisión manual - ID: {}, Puntuación: {}", reviewId, String.format("%.2f", toxicityScore));
        } else {
            moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
            log.info("✅ Reseña aprobada por IA - ID: {}, Puntuación: {}", reviewId, String.format("%.2f", toxicityScore));
        }
    }

    private void applyFallbackModeration(CommentModeration moderation, String text, Long reviewId) {
        ContentAnalyzer.ContentAnalysisResult result = contentAnalyzer.analyze(text);

        moderation.setToxicityScore(result.toxicityScore());
        moderation.setModerationReason("Fallback: " + buildFallbackReason(result));
        moderation.setAiProcessed(false);

        if (result.toxicityScore() >= toxicityThreshold) {
            moderation.setStatus(CommentModeration.ModerationStatus.REJECTED);
            log.warn("❌ Reseña rechazada por Fallback - ID: {}, Puntuación: {}", reviewId, String.format("%.2f", result.toxicityScore()));
        } else if (result.toxicityScore() >= 0.4) {
            moderation.setStatus(CommentModeration.ModerationStatus.MANUAL_REVIEW);
            log.info("⚠️ Reseña marcada para revisión manual por Fallback - ID: {}", reviewId);
        } else {
            moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
            log.info("✅ Reseña aprobada por Fallback - ID: {}", reviewId);
        }
    }

    private String buildFallbackReason(ContentAnalyzer.ContentAnalysisResult result) {
        return result.badWordCount() == 0
                ? "Contenido limpio según reglas estrictas"
                : String.format("CONTENIDO INAPROPIADO detectado - %d problemas: %s",
                        result.badWordCount(), result.detectedWords());
    }

    /**
     * Verifica si Ollama está disponible para moderación.
     * Método de conveniencia para el controlador de administración.
     */
    public boolean isOllamaAvailable() {
        try {
            ollamaClient.analyzeContent("test");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Records y excepciones

    public record ModerationResult(double toxicityScore, String reason, boolean aiProcessed) {
        // Método de compatibilidad
        public boolean ollamaUsed() {
            return aiProcessed;
        }
    }

    public static class ContentModerationException extends RuntimeException {
        public ContentModerationException(String message) {
            super(message);
        }
    }
}
