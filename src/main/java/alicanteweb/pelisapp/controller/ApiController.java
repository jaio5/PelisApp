package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.*;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador unificado para toda la API REST pública
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final MovieService movieService;
    private final ReviewService reviewService;
    private final AuthService authService;
    private final IEmailService emailService;

    // ============= MOVIES API =============

    @GetMapping("/movies/{id}/details")
    public MovieDetailsDTO getMovieDetails(@PathVariable Long id) {
        return movieService.getCombinedByMovieId(id);
    }

    @GetMapping("/movies/tmdb/{tmdbId}/details")
    public MovieDetailsDTO getMovieDetailsByTmdbId(@PathVariable Long tmdbId) {
        return movieService.getCombinedByTmdbId(tmdbId);
    }

    // ============= REVIEWS API =============

    @PostMapping("/reviews")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewCreateRequest req) {
        Review review = reviewService.createReview(req.getUserId(), req.getMovieId(), req.getText(), req.getStars());
        return ResponseEntity.ok(review);
    }

    @PostMapping("/reviews/{id}/like")
    public ResponseEntity<Void> likeReview(@PathVariable("id") Long reviewId, @RequestParam("userId") Long userId) {
        reviewService.likeReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }

    // ============= AUTH API =============

    @PostMapping("/auth/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse resp = authService.register(req);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        LoginResponse resp = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(resp);
    }

    // ============= HEALTH/STATUS =============

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ============= EMAIL TESTING =============

    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        try {
            log.info("🧪 Testing email send to: {}", email);

            String testToken = "test-token-" + System.currentTimeMillis();
            emailService.sendConfirmationEmail(email, "TestUser", testToken);

            return ResponseEntity.ok("✅ Email enviado exitosamente a " + email +
                "\n📬 Revisa tu bandeja de entrada y carpeta de SPAM");

        } catch (Exception e) {
            log.error("❌ Error enviando email de prueba: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error enviando email: " + e.getMessage());
        }
    }
}
