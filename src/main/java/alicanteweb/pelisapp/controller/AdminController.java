package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.TMDBMovieLoaderService;
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
}
