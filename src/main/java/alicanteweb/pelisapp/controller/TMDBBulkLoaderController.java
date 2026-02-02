package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.service.TMDBBulkLoaderService;
import alicanteweb.pelisapp.service.TMDBBulkLoaderService.LoadingResult;
import alicanteweb.pelisapp.service.TMDBBulkLoaderService.LoadingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para la carga masiva de películas desde TMDB
 */
@Controller
@RequestMapping("/admin/bulk-loader")
@RequiredArgsConstructor
@Slf4j
public class TMDBBulkLoaderController {

    private final TMDBBulkLoaderService bulkLoaderService;
    private final MovieRepository movieRepository;

    /**
     * Página principal de carga masiva
     */
    @GetMapping
    public String showBulkLoader(Model model) {
        long currentMovieCount = movieRepository.count();
        boolean loadingInProgress = bulkLoaderService.isLoadingInProgress();
        LoadingStatus status = bulkLoaderService.getCurrentStatus();

        model.addAttribute("currentMovieCount", currentMovieCount);
        model.addAttribute("loadingInProgress", loadingInProgress);
        model.addAttribute("loadingStatus", status);

        return "admin/bulk-loader";
    }

    /**
     * Iniciar carga masiva de películas populares
     */
    @PostMapping("/start-popular")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startPopularMoviesLoad(
            @RequestParam(defaultValue = "100") int maxPages,
            @RequestParam(defaultValue = "500") int delayMs) {

        Map<String, Object> response = new HashMap<>();

        if (bulkLoaderService.isLoadingInProgress()) {
            response.put("success", false);
            response.put("message", "Ya hay una carga en progreso");
            return ResponseEntity.badRequest().body(response);
        }

        // Validar parámetros
        maxPages = Math.min(maxPages, 500); // Máximo 500 páginas
        delayMs = Math.max(delayMs, 100);   // Mínimo 100ms de delay

        log.info("🚀 Iniciando carga masiva: {} páginas con delay de {}ms", maxPages, delayMs);

        // Iniciar carga asíncrona
        CompletableFuture<LoadingResult> future = bulkLoaderService.loadAllPopularMovies(maxPages, delayMs);

        response.put("success", true);
        response.put("message", String.format("Carga masiva iniciada: %d páginas máximo", maxPages));
        response.put("maxPages", maxPages);
        response.put("delayMs", delayMs);

        return ResponseEntity.ok(response);
    }

    /**
     * Iniciar carga masiva de múltiples categorías
     */
    @PostMapping("/start-categories")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startCategoriesLoad(
            @RequestParam(defaultValue = "50") int pagesPerCategory,
            @RequestParam(defaultValue = "500") int delayMs) {

        Map<String, Object> response = new HashMap<>();

        if (bulkLoaderService.isLoadingInProgress()) {
            response.put("success", false);
            response.put("message", "Ya hay una carga en progreso");
            return ResponseEntity.badRequest().body(response);
        }

        pagesPerCategory = Math.min(pagesPerCategory, 100);
        delayMs = Math.max(delayMs, 100);

        log.info("🎬 Iniciando carga de múltiples categorías: {} páginas por categoría", pagesPerCategory);

        CompletableFuture<LoadingResult> future = bulkLoaderService.loadAllMovieCategories(pagesPerCategory, delayMs);

        response.put("success", true);
        response.put("message", String.format("Carga de categorías iniciada: %d páginas por categoría", pagesPerCategory));
        response.put("pagesPerCategory", pagesPerCategory);
        response.put("delayMs", delayMs);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener estado actual de la carga
     */
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLoadingStatus() {
        Map<String, Object> response = new HashMap<>();

        boolean isLoading = bulkLoaderService.isLoadingInProgress();
        LoadingStatus status = bulkLoaderService.getCurrentStatus();
        long currentMovieCount = movieRepository.count();

        response.put("isLoading", isLoading);
        response.put("currentMovieCount", currentMovieCount);

        if (status != null) {
            response.put("type", status.type);
            response.put("currentPage", status.currentPage);
            response.put("totalPages", status.totalPages);
            response.put("totalPagesAvailable", status.totalPagesAvailable);
            response.put("processedMovies", status.processedMovies);
            response.put("skippedMovies", status.skippedMovies);
            response.put("totalMoviesAvailable", status.totalMoviesAvailable);
            response.put("progress", status.getProgress());
            response.put("completed", status.completed);
            response.put("startTime", status.startTime);
            response.put("endTime", status.endTime);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar carga en progreso
     */
    @PostMapping("/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelLoading() {
        Map<String, Object> response = new HashMap<>();

        boolean cancelled = bulkLoaderService.cancelLoading();

        response.put("success", cancelled);
        response.put("message", cancelled ? "Carga cancelada" : "No había ninguna carga en progreso");

        return ResponseEntity.ok(response);
    }

    /**
     * Información sobre límites y recomendaciones
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("recommendations", Map.of(
            "maxPages", "100-500 páginas (2,000-10,000 películas)",
            "delayMs", "500-1000ms para evitar límites de la API",
            "timeEstimate", "100 páginas ≈ 5-10 minutos",
            "diskSpace", "~50MB por 1,000 películas (con imágenes)"
        ));

        info.put("tmdbLimits", Map.of(
            "popularMovies", "~500 páginas disponibles (~10,000 películas)",
            "topRated", "~500 páginas disponibles (~10,000 películas)",
            "trending", "20 películas por semana",
            "rateLimit", "40 requests por 10 segundos"
        ));

        long currentCount = movieRepository.count();
        info.put("currentStats", Map.of(
            "moviesInDatabase", currentCount,
            "estimatedSpaceUsed", currentCount * 50 + "KB"
        ));

        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint para presets rápidos
     */
    @PostMapping("/preset/{presetName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> usePreset(@PathVariable String presetName) {
        Map<String, Object> response = new HashMap<>();

        if (bulkLoaderService.isLoadingInProgress()) {
            response.put("success", false);
            response.put("message", "Ya hay una carga en progreso");
            return ResponseEntity.badRequest().body(response);
        }

        CompletableFuture<LoadingResult> future;
        String message;

        switch (presetName.toLowerCase()) {
            case "quick":
                // Carga rápida: 10 páginas populares
                future = bulkLoaderService.loadAllPopularMovies(10, 300);
                message = "Carga rápida iniciada: ~200 películas populares";
                break;

            case "medium":
                // Carga media: 50 páginas populares
                future = bulkLoaderService.loadAllPopularMovies(50, 500);
                message = "Carga media iniciada: ~1,000 películas populares";
                break;

            case "full":
                // Carga completa: 200 páginas populares
                future = bulkLoaderService.loadAllPopularMovies(200, 600);
                message = "Carga completa iniciada: ~4,000 películas populares";
                break;

            case "ultimate":
                // Carga masiva: 500 páginas populares
                future = bulkLoaderService.loadAllPopularMovies(500, 800);
                message = "Carga masiva iniciada: ~10,000 películas populares";
                break;

            case "categories":
                // Carga de categorías: populares + top rated + trending
                future = bulkLoaderService.loadAllMovieCategories(30, 500);
                message = "Carga de categorías iniciada: populares + top rated + trending";
                break;

            default:
                response.put("success", false);
                response.put("message", "Preset no válido: " + presetName);
                response.put("availablePresets", new String[]{"quick", "medium", "full", "ultimate", "categories"});
                return ResponseEntity.badRequest().body(response);
        }

        log.info("📋 Preset '{}' ejecutado: {}", presetName, message);

        response.put("success", true);
        response.put("message", message);
        response.put("preset", presetName);

        return ResponseEntity.ok(response);
    }
}
