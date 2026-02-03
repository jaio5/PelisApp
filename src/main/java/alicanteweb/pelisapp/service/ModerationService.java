package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.CommentModeration;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.repository.CommentModerationRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de moderación usando Ollama para analizar contenido tóxico en reseñas.
 * Implementa análisis asíncrono y fallback con reglas básicas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {

    private final ReviewRepository reviewRepository;
    private final CommentModerationRepository commentModerationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.moderation.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${app.moderation.ollama.model:llama3}")
    private String ollamaModel;

    @Value("${app.moderation.toxicity.threshold:0.7}")
    private double toxicityThreshold;

    @Value("${app.moderation.enabled:true}")
    private boolean moderationEnabled;

    @Value("${app.moderation.fallback.enabled:true}")
    private boolean fallbackEnabled;

    @PostConstruct
    public void init() {
        log.info("🛡️ ModerationService inicializado:");
        log.info("  📍 Ollama URL: {}", ollamaUrl);
        log.info("  🤖 Modelo: {}", ollamaModel);
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

        CommentModeration moderation = new CommentModeration();
        moderation.setReview(review);
        moderation.setStatus(CommentModeration.ModerationStatus.PENDING);
        moderation.setCreatedAt(Instant.now());

        try {
            // Intentar moderación con Ollama
            ModerationResult result = analyzeWithOllama(review.getText());

            moderation.setToxicityScore(result.toxicityScore());
            moderation.setModerationReason(result.reason());
            moderation.setAiProcessed(true);

            if (result.toxicityScore() >= toxicityThreshold) {
                moderation.setStatus(CommentModeration.ModerationStatus.REJECTED);
                log.warn("❌ Reseña rechazada por IA - ID: {}, Puntuación: {:.2f}, Razón: {}",
                        review.getId(), result.toxicityScore(), result.reason());
            } else if (result.toxicityScore() >= 0.5) {
                moderation.setStatus(CommentModeration.ModerationStatus.MANUAL_REVIEW);
                log.info("⚠️ Reseña marcada para revisión manual - ID: {}, Puntuación: {:.2f}",
                        review.getId(), result.toxicityScore());
            } else {
                moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
                log.info("✅ Reseña aprobada por IA - ID: {}, Puntuación: {:.2f}",
                        review.getId(), result.toxicityScore());
            }

        } catch (Exception e) {
            log.error("❌ Error en moderación con Ollama para reseña ID: {}: {}",
                    review.getId(), e.getMessage());

            if (fallbackEnabled) {
                log.info("🔄 Activando moderación de respaldo para reseña ID: {}", review.getId());
                ModerationResult fallbackResult = analyzeWithFallback(review.getText());
                moderation.setToxicityScore(fallbackResult.toxicityScore());
                moderation.setModerationReason("Fallback: " + fallbackResult.reason());
                moderation.setAiProcessed(false);

                if (fallbackResult.toxicityScore() >= toxicityThreshold) {
                    moderation.setStatus(CommentModeration.ModerationStatus.REJECTED);
                } else {
                    moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
                }
            } else {
                // Sin fallback, aprobar por defecto pero marcar el error
                moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
                moderation.setModerationReason("Error en moderación IA: " + e.getMessage());
                moderation.setAiProcessed(false);
            }
        }

        moderation.setReviewedAt(Instant.now());
        CommentModeration saved = commentModerationRepository.save(moderation);

        log.info("💾 Moderación guardada - ID: {}, Estado: {}, Puntuación: {:.2f}",
                saved.getId(), saved.getStatus(), saved.getToxicityScore());

        return CompletableFuture.completedFuture(saved);
    }

    /**
     * Analizar texto con Ollama usando un prompt especializado para detección de contenido tóxico.
     */
    private ModerationResult analyzeWithOllama(String text) {
        try {
            String prompt = buildModerationPrompt(text);

            Map<String, Object> request = new HashMap<>();
            request.put("model", ollamaModel);
            request.put("prompt", prompt);
            request.put("stream", false);
            request.put("options", Map.of(
                "temperature", 0.1,
                "top_p", 0.9,
                "max_tokens", 200
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            log.debug("📤 Enviando solicitud a Ollama: {}/api/generate", ollamaUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(
                ollamaUrl + "/api/generate",
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseOllamaResponse(response.getBody());
            } else {
                throw new RuntimeException("Respuesta inválida de Ollama: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error conectando con Ollama: {}", e.getMessage());
            throw new RuntimeException("Error en análisis con Ollama", e);
        }
    }

    /**
     * Construir prompt especializado para moderación de contenido.
     */
    private String buildModerationPrompt(String text) {
        return String.format("""
            Eres un moderador de contenido experto. Analiza el siguiente texto de una reseña de película y determina si contiene:
            
            1. Lenguaje malsonante u ofensivo
            2. Insultos o ataques personales
            3. Discurso de odio
            4. Contenido inapropiado
            
            Texto a analizar: "%s"
            
            Responde únicamente en formato JSON con esta estructura:
            {
              "toxicity_score": [número entre 0.0 y 1.0, donde 1.0 es más tóxico],
              "is_toxic": [true/false],
              "reason": "[explicación breve de por qué es tóxico o limpio]",
              "detected_issues": ["lista", "de", "problemas", "detectados"]
            }
            
            Respuesta:""", text);
    }

    /**
     * Parsear respuesta de Ollama y extraer puntuación de toxicidad.
     */
    private ModerationResult parseOllamaResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String aiResponse = jsonNode.get("response").asText();

            // Buscar JSON en la respuesta
            int jsonStart = aiResponse.indexOf("{");
            int jsonEnd = aiResponse.lastIndexOf("}") + 1;

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonPart = aiResponse.substring(jsonStart, jsonEnd);
                JsonNode moderationData = objectMapper.readTree(jsonPart);

                double toxicityScore = moderationData.get("toxicity_score").asDouble();
                boolean isToxic = moderationData.get("is_toxic").asBoolean();
                String reason = moderationData.get("reason").asText();

                log.debug("📊 Análisis Ollama - Puntuación: {:.2f}, Tóxico: {}, Razón: {}",
                        toxicityScore, isToxic, reason);

                return new ModerationResult(toxicityScore, reason, true);
            } else {
                throw new RuntimeException("No se encontró JSON válido en la respuesta de Ollama");
            }

        } catch (Exception e) {
            log.error("Error parseando respuesta de Ollama: {}", e.getMessage());
            throw new RuntimeException("Error interpretando respuesta de Ollama", e);
        }
    }

    /**
     * Sistema de fallback con reglas básicas de moderación.
     */
    private ModerationResult analyzeWithFallback(String text) {
        if (text == null || text.isBlank()) {
            return new ModerationResult(0.0, "Texto vacío", false);
        }

        String lowerText = text.toLowerCase();

        // Lista de palabras prohibidas (expandida)
        String[] badWords = {
            "puta", "idiota", "imbecil", "estupido", "mierda", "joder", "coño",
            "gilipollas", "cabron", "tonto", "subnormal", "retrasado", "marica",
            "fuck", "shit", "damn", "bitch", "asshole", "motherfucker"
        };

        int badWordCount = 0;
        StringBuilder detectedWords = new StringBuilder();

        for (String badWord : badWords) {
            if (lowerText.contains(badWord)) {
                badWordCount++;
                if (detectedWords.length() > 0) {
                    detectedWords.append(", ");
                }
                detectedWords.append(badWord);
            }
        }

        // Calcular puntuación basada en número de palabras prohibidas
        double toxicityScore = Math.min(1.0, badWordCount * 0.3);

        String reason = badWordCount == 0
            ? "Contenido limpio según reglas básicas"
            : String.format("Detectadas %d palabras inapropiadas: %s", badWordCount, detectedWords);

        log.debug("🔄 Análisis Fallback - Palabras prohibidas: {}, Puntuación: {:.2f}",
                badWordCount, toxicityScore);

        return new ModerationResult(toxicityScore, reason, false);
    }

    /**
     * Crear moderación aprobada por defecto cuando la moderación está deshabilitada.
     */
    private CommentModeration createApprovedModeration(Review review) {
        CommentModeration moderation = new CommentModeration();
        moderation.setReview(review);
        moderation.setStatus(CommentModeration.ModerationStatus.APPROVED);
        moderation.setToxicityScore(0.0);
        moderation.setModerationReason("Moderación deshabilitada - Aprobado automáticamente");
        moderation.setAiProcessed(false);
        moderation.setCreatedAt(Instant.now());
        moderation.setReviewedAt(Instant.now());

        return commentModerationRepository.save(moderation);
    }

    /**
     * Verificar si Ollama está disponible.
     */
    public boolean isOllamaAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                ollamaUrl + "/api/version", String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.debug("Ollama no disponible en {}: {}", ollamaUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Record para encapsular el resultado de moderación.
     */
    private record ModerationResult(double toxicityScore, String reason, boolean ollamaUsed) {}
}