package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Controlador API para estadísticas del dashboard administrativo
 */
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
@Slf4j
public class AdminStatsController {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // User statistics
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByEmailConfirmed(true);

            // Movie statistics
            long totalMovies = movieRepository.count();

            // Calculate growth rates (mock data for now)
            stats.put("totalUsers", totalUsers);
            stats.put("totalMovies", totalMovies);
            stats.put("totalReviews", 0L); // TODO: Add review repository
            stats.put("usersGrowthMonth", totalUsers / 4); // Mock calculation
            stats.put("moviesGrowthWeek", Math.max(0, totalMovies - 50)); // Mock calculation
            stats.put("reviewsGrowthToday", 5L); // Mock data
            stats.put("totalRecords", totalUsers + totalMovies);
            stats.put("storageUsedMB", 45.6); // Mock data
            stats.put("activeUsers", activeUsers);
            stats.put("newUsersThisWeek", totalUsers / 10); // Mock calculation

            log.info("Dashboard stats loaded: {} users, {} movies", totalUsers, totalMovies);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error loading dashboard stats: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "totalUsers", 0,
                "totalMovies", 0,
                "totalReviews", 0,
                "error", "Error loading statistics"
            ));
        }
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();

            // Check database connectivity
            long userCount = userRepository.count();
            boolean dbHealthy = userCount >= 0;

            health.put("database", dbHealthy ? "healthy" : "error");
            health.put("email", "configured"); // TODO: Check actual email service
            health.put("tmdb", "connected"); // TODO: Check TMDB API
            health.put("storage", "available");
            health.put("uptime", System.currentTimeMillis() / 1000); // Seconds since start

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Error checking system health: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "database", "error",
                "error", e.getMessage()
            ));
        }
    }
}
