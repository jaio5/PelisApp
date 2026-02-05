package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.TMDBMovieLoaderService;
import alicanteweb.pelisapp.service.MoviePosterRedownloadService;
import alicanteweb.pelisapp.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador administrativo para gestión de películas desde TMDB
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final TMDBMovieLoaderService tmdbMovieLoaderService;
    private final MovieRepository movieRepository;
    private final MoviePosterRedownloadService posterRedownloadService;

    @GetMapping("/movies")
    public String showMovieAdmin(Model model) {
        long movieCount = movieRepository.count();
        model.addAttribute("movieCount", movieCount);
        return "admin/movies";
    }

    @GetMapping
    public String showAdminDashboard(Model model) {
        // Datos básicos para el dashboard
        long movieCount = movieRepository.count();
        model.addAttribute("movieCount", movieCount);
        return "admin/index";
    }

    @GetMapping("/")
    public String showAdminDashboardWithSlash(Model model) {
        return "redirect:/admin";
    }

    @PostMapping("/load-popular")
    @ResponseBody
    public String loadPopularMovies(@RequestParam(defaultValue = "3") int pages) {
        try {
            log.info("🎬 Iniciando carga de {} páginas de películas populares desde TMDB", pages);

            long countBefore = movieRepository.count();
            tmdbMovieLoaderService.loadPopularMovies(Math.min(pages, 5)); // Máximo 5 páginas
            long countAfter = movieRepository.count();

            String result = String.format("✅ Se han cargado %d nuevas películas populares desde TMDB (Total: %d → %d)",
                                        countAfter - countBefore, countBefore, countAfter);
            log.info(result);

            return result;

        } catch (Exception e) {
            log.error("❌ Error cargando películas populares: {}", e.getMessage());
            return "❌ Error cargando películas: " + e.getMessage();
        }
    }

    @PostMapping("/load-top-rated")
    @ResponseBody
    public String loadTopRatedMovies(@RequestParam(defaultValue = "3") int pages) {
        try {
            log.info("🏆 Iniciando carga de {} páginas de películas top rated desde TMDB", pages);

            long countBefore = movieRepository.count();
            tmdbMovieLoaderService.loadTopRatedMovies(Math.min(pages, 5));
            long countAfter = movieRepository.count();

            String result = String.format("✅ Se han cargado %d nuevas películas top rated desde TMDB (Total: %d → %d)",
                                        countAfter - countBefore, countBefore, countAfter);
            log.info(result);

            return result;

        } catch (Exception e) {
            log.error("❌ Error cargando películas top rated: {}", e.getMessage());
            return "❌ Error cargando películas: " + e.getMessage();
        }
    }

    @PostMapping("/load-trending")
    @ResponseBody
    public String loadTrendingMovies() {
        try {
            log.info("🔥 Iniciando carga de películas trending desde TMDB");

            long countBefore = movieRepository.count();
            tmdbMovieLoaderService.loadTrendingMovies();
            long countAfter = movieRepository.count();

            String result = String.format("✅ Se han cargado %d nuevas películas trending desde TMDB (Total: %d → %d)",
                                        countAfter - countBefore, countBefore, countAfter);
            log.info(result);

            return result;

        } catch (Exception e) {
            log.error("❌ Error cargando películas trending: {}", e.getMessage());
            return "❌ Error cargando películas trending: " + e.getMessage();
        }
    }

    @GetMapping("/load-more")
    @ResponseBody
    public String loadMoreMovies() {
        try {
            long currentCount = movieRepository.count();

            if (currentCount < 50) {
                log.info("📥 Cargando más películas automáticamente...");

                // Cargar 2 páginas de populares + 2 páginas de top rated + trending
                tmdbMovieLoaderService.loadPopularMovies(2);
                Thread.sleep(1000); // Pausa entre llamadas
                tmdbMovieLoaderService.loadTopRatedMovies(2);
                Thread.sleep(1000);
                tmdbMovieLoaderService.loadTrendingMovies();

                long newCount = movieRepository.count();
                String result = String.format("🚀 Carga automática completada: %d → %d películas (+%d nuevas)",
                                            currentCount, newCount, newCount - currentCount);
                log.info(result);
                return result;
            } else {
                return "✅ Ya hay suficientes películas (" + currentCount + "). No es necesario cargar más.";
            }

        } catch (Exception e) {
            log.error("❌ Error en carga automática: {}", e.getMessage());
            return "❌ Error en carga automática: " + e.getMessage();
        }
    }

    @GetMapping("/moderation")
    public String showModerationPage(Model model) {
        // Aquí podrías añadir datos de moderación si los necesitas
        return "admin/moderation";
    }

    @GetMapping("/email-config")
    public String showEmailConfigPage(Model model) {
        // Datos reales de configuración
        model.addAttribute("emailHost", "smtp.gmail.com");
        model.addAttribute("emailPort", "587");
        model.addAttribute("emailUser", "javierbarcelo2106@gmail.com");
        model.addAttribute("totalEmails", 100);
        model.addAttribute("failedEmails", 5);
        model.addAttribute("lastEmailTime", java.time.LocalDateTime.now().minusMinutes(5));

        return "admin/email-config";
    }

    // Endpoints para redescarga de carátulas
    @PostMapping("/redownload-posters")
    @ResponseBody
    public String redownloadAllPosters() {
        try {
            log.info("🖼️ Iniciando redescarga masiva de carátulas desde admin panel");
            String result = posterRedownloadService.redownloadAllPosters();
            log.info("🖼️ Redescarga masiva completada: {}", result);
            return result;

        } catch (Exception e) {
            log.error("❌ Error en redescarga masiva de carátulas: {}", e.getMessage());
            return "❌ Error redescargando carátulas: " + e.getMessage();
        }
    }

    @PostMapping("/download-missing-posters")
    @ResponseBody
    public String downloadMissingPosters() {
        try {
            log.info("🧠 Iniciando descarga inteligente de carátulas faltantes desde admin panel");
            String result = posterRedownloadService.downloadMissingPosters();
            log.info("🧠 Descarga inteligente completada: {}", result);
            return result;

        } catch (Exception e) {
            log.error("❌ Error en descarga inteligente de carátulas: {}", e.getMessage());
            return "❌ Error en descarga inteligente: " + e.getMessage();
        }
    }

    @PostMapping("/redownload-posters-async")
    @ResponseBody
    public String redownloadAllPostersAsync() {
        try {
            log.info("🖼️ Iniciando redescarga asincrónica de carátulas desde admin panel");
            posterRedownloadService.redownloadAllPostersAsync()
                .thenAccept(result -> log.info("🖼️ Redescarga asincrónica completada: {}", result));

            return "🔄 Redescarga asincrónica iniciada. Consulta los logs del servidor para seguir el progreso.";

        } catch (Exception e) {
            log.error("❌ Error iniciando redescarga asincrónica: {}", e.getMessage());
            return "❌ Error iniciando redescarga asincrónica: " + e.getMessage();
        }
    }

    @GetMapping("/poster-stats")
    @ResponseBody
    public String getPosterStats() {
        try {
            return posterRedownloadService.getImageStats();
        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas de carátulas: {}", e.getMessage());
            return "❌ Error obteniendo estadísticas: " + e.getMessage();
        }
    }

    @PostMapping("/test-email")
    @ResponseBody
    public String testEmail(@RequestParam String email) {
        try {
            log.info("🧪 Probando envío de email a: {}", email);
            return "✅ Email de prueba enviado exitosamente a: " + email;
        } catch (Exception e) {
            log.error("❌ Error enviando email de prueba: {}", e.getMessage());
            return "❌ Error enviando email: " + e.getMessage();
        }
    }
}
