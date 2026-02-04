package alicanteweb.pelisapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Controlador API para funciones de moderación
 */
@RestController
@RequestMapping("/admin/api/moderation")
@RequiredArgsConstructor
@Slf4j
public class AdminModerationApiController {

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getModerationStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // TODO: Replace with actual data from review repository
            stats.put("pendingReviews", 12);
            stats.put("flaggedContent", 3);
            stats.put("approvedToday", 8);
            stats.put("totalReviews", 147);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error loading moderation stats: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "pendingReviews", 0,
                "flaggedContent", 0,
                "approvedToday", 0,
                "totalReviews", 0,
                "error", "Error loading statistics"
            ));
        }
    }

    @GetMapping("/queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getModerationQueue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String toxicity,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search) {

        try {
            // TODO: Replace with actual data from review repository
            List<Map<String, Object>> mockReviews = new ArrayList<>();

            // Mock review 1
            Map<String, Object> review1 = new HashMap<>();
            review1.put("id", 1);
            review1.put("username", "usuario123");
            review1.put("userAvatar", null);
            review1.put("movieTitle", "Avatar: El Camino del Agua");
            review1.put("rating", 4);
            review1.put("text", "Película increíble con efectos visuales espectaculares. La historia es emocionante.");
            review1.put("status", "pending");
            review1.put("toxicityScore", 0.15);
            review1.put("reports", 0);
            review1.put("createdAt", "Hace 2 horas");

            // Mock review 2
            Map<String, Object> review2 = new HashMap<>();
            review2.put("id", 2);
            review2.put("username", "critico456");
            review2.put("userAvatar", null);
            review2.put("movieTitle", "Top Gun: Maverick");
            review2.put("rating", 2);
            review2.put("text", "Una película terrible que no entiendo cómo puede gustar a la gente. Los efectos son malos.");
            review2.put("status", "flagged");
            review2.put("toxicityScore", 0.75);
            review2.put("reports", 3);
            review2.put("createdAt", "Hace 5 horas");

            // Mock review 3
            Map<String, Object> review3 = new HashMap<>();
            review3.put("id", 3);
            review3.put("username", "moviefan789");
            review3.put("userAvatar", null);
            review3.put("movieTitle", "Black Panther: Wakanda Forever");
            review3.put("rating", 5);
            review3.put("text", "Excelente película que honra el legado de Chadwick Boseman. Muy emotiva.");
            review3.put("status", "pending");
            review3.put("toxicityScore", 0.05);
            review3.put("reports", 0);
            review3.put("createdAt", "Hace 1 día");

            mockReviews.add(review1);
            mockReviews.add(review2);
            mockReviews.add(review3);

            // Apply filters (basic implementation)
            List<Map<String, Object>> filteredReviews = mockReviews;

            if (status != null && !status.isEmpty()) {
                filteredReviews = filteredReviews.stream()
                    .filter(review -> status.equals(review.get("status")))
                    .toList();
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredReviews = filteredReviews.stream()
                    .filter(review ->
                        review.get("username").toString().toLowerCase().contains(searchLower) ||
                        review.get("movieTitle").toString().toLowerCase().contains(searchLower) ||
                        review.get("text").toString().toLowerCase().contains(searchLower))
                    .toList();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("reviews", filteredReviews);
            result.put("total", filteredReviews.size());

            log.info("Moderation queue loaded: {} reviews", filteredReviews.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error loading moderation queue: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "reviews", new ArrayList<>(),
                "total", 0,
                "error", "Error loading moderation queue"
            ));
        }
    }

    @GetMapping("/review/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getReviewDetail(@PathVariable Long id) {
        try {
            // TODO: Load actual review details
            String detailHtml = String.format("""
                <div class="review-detail">
                    <h6>Detalles de la Reseña #%d</h6>
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Usuario:</strong> usuario123</p>
                            <p><strong>Película:</strong> Avatar: El Camino del Agua</p>
                            <p><strong>Puntuación:</strong> 4/5 estrellas</p>
                            <p><strong>Fecha:</strong> 2026-02-04 14:30</p>
                        </div>
                        <div class="col-md-6">
                            <p><strong>Reportes:</strong> 0</p>
                            <p><strong>Toxicidad:</strong> 15%% (Baja)</p>
                            <p><strong>Estado:</strong> Pendiente</p>
                            <p><strong>IP:</strong> 192.168.1.100</p>
                        </div>
                    </div>
                    <div class="mt-3">
                        <h6>Contenido de la reseña:</h6>
                        <div class="border p-3 rounded bg-light">
                            <p>Película increíble con efectos visuales espectaculares. La historia es emocionante y los personajes están bien desarrollados.</p>
                        </div>
                    </div>
                </div>
                """, id);

            return ResponseEntity.ok(detailHtml);

        } catch (Exception e) {
            log.error("Error loading review detail for ID {}: {}", id, e.getMessage());
            return ResponseEntity.ok("<div class='alert alert-danger'>Error: " + e.getMessage() + "</div>");
        }
    }

    @PostMapping("/review/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveReview(@PathVariable Long id) {
        try {
            // TODO: Implement actual approval logic
            log.info("Review {} approved by admin", id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reseña aprobada correctamente"
            ));

        } catch (Exception e) {
            log.error("Error approving review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error aprobando la reseña"
            ));
        }
    }

    @PostMapping("/review/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectReview(@PathVariable Long id) {
        try {
            // TODO: Implement actual rejection logic
            log.info("Review {} rejected by admin", id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reseña rechazada correctamente"
            ));

        } catch (Exception e) {
            log.error("Error rejecting review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error rechazando la reseña"
            ));
        }
    }

    @PostMapping("/bulk/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkApprove(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> reviewIds = request.get("reviewIds");
            if (reviewIds == null || reviewIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "No se proporcionaron IDs de reseñas"
                ));
            }

            // TODO: Implement actual bulk approval logic
            log.info("Bulk approved {} reviews by admin", reviewIds.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", reviewIds.size() + " reseñas aprobadas correctamente"
            ));

        } catch (Exception e) {
            log.error("Error in bulk approval: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error en la aprobación masiva"
            ));
        }
    }

    @PostMapping("/bulk/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkReject(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> reviewIds = request.get("reviewIds");
            if (reviewIds == null || reviewIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "No se proporcionaron IDs de reseñas"
                ));
            }

            // TODO: Implement actual bulk rejection logic
            log.info("Bulk rejected {} reviews by admin", reviewIds.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", reviewIds.size() + " reseñas rechazadas correctamente"
            ));

        } catch (Exception e) {
            log.error("Error in bulk rejection: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error en el rechazo masivo"
            ));
        }
    }
}
