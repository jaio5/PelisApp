package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.service.DirectMovieLoader;
import alicanteweb.pelisapp.service.TMDBBulkLoaderService;
import alicanteweb.pelisapp.service.TMDBMovieLoaderService;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para carga manual de películas desde TMDB con reparto completo
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class TMDBLoadController {

    private final TMDBMovieLoaderService tmdbMovieLoaderService;
    private final TMDBBulkLoaderService tmdbBulkLoaderService;
    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;
    private final DirectMovieLoader directMovieLoader;

    @GetMapping("/test-tmdb")
    public String testTMDB() {
        try {
            log.info("Probando conexión con TMDB...");

            // Probar obtener una película conocida (The Dark Knight tiene ID 155)
            JsonNode movieDetails = tmdbClient.getMovieDetails(155L);

            if (movieDetails != null) {
                String title = movieDetails.path("title").asText("Sin título");
                boolean hasCredits = movieDetails.has("credits");
                int castSize = hasCredits ? movieDetails.path("credits").path("cast").size() : 0;
                int crewSize = hasCredits ? movieDetails.path("credits").path("crew").size() : 0;

                return String.format("✅ TMDB conectado correctamente!\n" +
                    "Película de prueba: %s\n" +
                    "Tiene créditos: %s\n" +
                    "Actores: %d\n" +
                    "Crew: %d\n" +
                    "Respuesta completa: %s",
                    title, hasCredits, castSize, crewSize,
                    movieDetails.toString().length() > 200 ?
                        movieDetails.toString().substring(0, 200) + "..." :
                        movieDetails.toString()
                );
            } else {
                return "❌ Error: TMDB devolvió null. Verifica tu API key/bearer token.";
            }
        } catch (Exception e) {
            log.error("Error probando TMDB: {}", e.getMessage());
            return "❌ Error conectando con TMDB: " + e.getMessage();
        }
    }

    @GetMapping("/test-movie/{id}")
    public String testSpecificMovie(@PathVariable Long id) {
        try {
            JsonNode movieDetails = tmdbClient.getMovieDetails(id);

            if (movieDetails != null) {
                String title = movieDetails.path("title").asText("Sin título");
                boolean hasCredits = movieDetails.has("credits");

                StringBuilder result = new StringBuilder();
                result.append("🎬 Película: ").append(title).append("\n");
                result.append("ID TMDB: ").append(id).append("\n");
                result.append("Tiene créditos: ").append(hasCredits).append("\n");

                if (hasCredits) {
                    JsonNode credits = movieDetails.path("credits");
                    JsonNode cast = credits.path("cast");
                    JsonNode crew = credits.path("crew");

                    result.append("Actores: ").append(cast.size()).append("\n");
                    result.append("Crew: ").append(crew.size()).append("\n");

                    // Mostrar primeros 3 actores
                    result.append("\nPrimeros actores:\n");
                    for (int i = 0; i < Math.min(3, cast.size()); i++) {
                        JsonNode actor = cast.get(i);
                        result.append("  - ").append(actor.path("name").asText())
                              .append(" como ").append(actor.path("character").asText())
                              .append(" (foto: ").append(actor.path("profile_path").asText("sin foto"))
                              .append(")\n");
                    }

                    // Mostrar directores
                    result.append("\nDirectores:\n");
                    for (JsonNode member : crew) {
                        if ("Director".equals(member.path("job").asText())) {
                            result.append("  - ").append(member.path("name").asText())
                                  .append(" (foto: ").append(member.path("profile_path").asText("sin foto"))
                                  .append(")\n");
                        }
                    }
                }

                return result.toString();
            } else {
                return "❌ No se encontró la película con ID: " + id;
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/load-movies")
    public String loadMovies() {
        try {
            log.info("Iniciando carga masiva de películas desde TMDB...");
            // Cargar 50 páginas = ~1000 películas populares
            tmdbMovieLoaderService.loadPopularMovies(50);
            return "✅ Carga completada. ~1000 películas populares cargadas con reparto y fotos.";
        } catch (Exception e) {
            log.error("Error cargando películas: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/load-all-movies")
    public String loadAllMovies() {
        try {
            log.info("Iniciando carga MASIVA de películas desde TMDB (múltiples categorías)...");
            // Cargar películas populares (100 páginas)
            log.info("Cargando películas populares...");
            tmdbMovieLoaderService.loadPopularMovies(100);

            // Cargar películas top rated (50 páginas)
            log.info("Cargando películas top rated...");
            tmdbMovieLoaderService.loadTopRatedMovies(50);

            // Cargar películas trending
            log.info("Cargando películas trending...");
            tmdbMovieLoaderService.loadTrendingMovies();

            return "✅ Carga MASIVA completada. ~3000+ películas cargadas con reparto y fotos desde múltiples categorías.";
        } catch (Exception e) {
            log.error("Error cargando películas: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/load-trending")
    public String loadTrending() {
        try {
            log.info("Iniciando carga de películas trending...");
            tmdbMovieLoaderService.loadTrendingMovies();
            // También cargar algunas populares
            tmdbMovieLoaderService.loadPopularMovies(20);
            return "✅ Carga de trending completada (~400 películas).";
        } catch (Exception e) {
            log.error("Error cargando trending: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Carga masiva asíncrona de películas - No bloquea la aplicación
     * Puedes cargar hasta 500 páginas (~10,000 películas) sin afectar el rendimiento
     */
    @GetMapping("/load-massive")
    public String loadMassive(
            @RequestParam(defaultValue = "200") int pages,
            @RequestParam(defaultValue = "300") int delayMs) {
        try {
            log.info("Iniciando carga MASIVA ASÍNCRONA de películas...");

            // Validar parámetros
            if (pages > 500) {
                return "⚠️ Límite de páginas: 500 (intentaste " + pages + "). " +
                       "Reduce el número o divide en múltiples cargas.";
            }

            if (delayMs < 250) {
                return "⚠️ Delay mínimo: 250ms para no sobrecargar la API de TMDB.";
            }

            // Iniciar carga asíncrona
            tmdbBulkLoaderService.loadAllPopularMovies(pages, delayMs);

            return String.format("🚀 Carga MASIVA iniciada en segundo plano!\n" +
                "📊 Páginas a cargar: %d (~%d películas)\n" +
                "⏱️ Tiempo estimado: ~%d minutos\n" +
                "📡 Consulta el estado en: GET /api/admin/load-status\n" +
                "💡 La carga no bloquea la aplicación, puedes seguir usando PelisApp normalmente.",
                pages, pages * 20, (pages * delayMs) / 60000);

        } catch (Exception e) {
            log.error("Error iniciando carga masiva: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Consultar estado de la carga masiva en progreso
     */
    @GetMapping("/load-status")
    public String getLoadStatus() {
        try {
            TMDBBulkLoaderService.LoadingStatus status = tmdbBulkLoaderService.getCurrentStatus();

            if (status == null || !tmdbBulkLoaderService.isLoadingInProgress()) {
                return "📊 No hay ninguna carga en progreso.\n" +
                       "💡 Inicia una con: GET /api/admin/load-massive?pages=200";
            }

            double progress = status.totalPages > 0 ?
                (status.currentPage * 100.0 / status.totalPages) : 0;

            long elapsedMinutes = java.time.Duration.between(
                status.startTime,
                status.endTime != null ? status.endTime : java.time.LocalDateTime.now()
            ).toMinutes();

            return String.format("🔄 Carga en progreso...\n\n" +
                "📊 Progreso: %.1f%% (%d/%d páginas)\n" +
                "🎬 Películas procesadas: %d\n" +
                "⏭️ Películas omitidas (ya existen): %d\n" +
                "📦 Total disponible en TMDB: ~%d películas\n" +
                "⏱️ Tiempo transcurrido: %d minutos\n" +
                "📡 Tipo de carga: %s\n" +
                "✅ Completada: %s",
                progress,
                status.currentPage,
                status.totalPages,
                status.processedMovies,
                status.skippedMovies,
                status.totalMoviesAvailable,
                elapsedMinutes,
                status.type != null ? status.type : "Unknown",
                status.completed ? "SÍ" : "NO"
            );

        } catch (Exception e) {
            log.error("Error consultando estado: {}", e.getMessage());
            return "❌ Error consultando estado: " + e.getMessage();
        }
    }

    /**
     * Verificar cuántas películas hay en la base de datos
     */
    @GetMapping("/movie-count")
    public String getMovieCount() {
        try {
            long count = tmdbBulkLoaderService.getMovieCount();
            return String.format("📊 Total de películas en la base de datos: %d\n\n" +
                "💡 Si necesitas más películas:\n" +
                "• Carga rápida (1000): GET /api/admin/load-movies\n" +
                "• Carga masiva (4000): GET /api/admin/load-massive?pages=200\n" +
                "• Carga completa (3000+ categorías): GET /api/admin/load-all-movies", count);
        } catch (Exception e) {
            log.error("Error consultando películas: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Endpoint automático: carga películas solo si hay menos de 100 en la DB
     */
    @GetMapping("/auto-load")
    public String autoLoad() {
        try {
            long count = tmdbBulkLoaderService.getMovieCount();

            if (count >= 100) {
                return String.format("✅ Ya tienes %d películas. No es necesario cargar más.\n" +
                    "💡 Si quieres más, usa: GET /api/admin/load-massive?pages=200", count);
            }

            log.info("🚀 Auto-carga iniciada - Solo hay {} películas", count);

            // Cargar 50 páginas automáticamente
            tmdbMovieLoaderService.loadPopularMovies(50);

            long newCount = tmdbBulkLoaderService.getMovieCount();

            return String.format("✅ Auto-carga completada!\n" +
                "📊 Antes: %d películas\n" +
                "📊 Ahora: %d películas\n" +
                "➕ Nuevas: %d películas", count, newCount, newCount - count);

        } catch (Exception e) {
            log.error("Error en auto-carga: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * ⚠️ PELIGRO: Elimina TODAS las películas de la base de datos
     * Úsalo solo para empezar desde cero
     */
    @GetMapping("/clear-all-movies")
    public String clearAllMovies(@RequestParam(required = false) String confirm) {
        try {
            if (!"YES_DELETE_ALL".equals(confirm)) {
                long count = tmdbBulkLoaderService.getMovieCount();
                return String.format("⚠️ ADVERTENCIA: Esto eliminará TODAS las %d películas de la base de datos.\n\n" +
                    "Para confirmar, añade: ?confirm=YES_DELETE_ALL\n\n" +
                    "Ejemplo: GET /api/admin/clear-all-movies?confirm=YES_DELETE_ALL\n\n" +
                    "💡 Después de limpiar, reinicia la aplicación para cargar automáticamente 1000 películas desde TMDB.", count);
            }

            long beforeCount = tmdbBulkLoaderService.getMovieCount();
            log.warn("🗑️ Eliminando TODAS las películas de la base de datos...");

            movieRepository.deleteAll();

            long afterCount = tmdbBulkLoaderService.getMovieCount();

            log.info("✅ Base de datos limpiada. {} películas eliminadas", beforeCount);

            return String.format("✅ Base de datos limpiada!\n\n" +
                "📊 Películas eliminadas: %d\n" +
                "📊 Películas actuales: %d\n\n" +
                "🔄 Ahora tienes 2 opciones:\n\n" +
                "OPCIÓN 1 (RECOMENDADA): Reiniciar la aplicación\n" +
                "   • Detén la app (Ctrl+C)\n" +
                "   • Ejecuta: mvn spring-boot:run\n" +
                "   • Espera 3-5 minutos mientras carga automáticamente ~1000 películas\n\n" +
                "OPCIÓN 2: Cargar manualmente sin reiniciar\n" +
                "   • GET /api/admin/load-movies (1000 películas, 2-3 min)\n" +
                "   • GET /api/admin/load-massive?pages=200 (4000 películas, 15 min)\n\n" +
                "💡 Con la OPCIÓN 1, las películas se cargarán automáticamente al iniciar.",
                beforeCount, afterCount);

        } catch (Exception e) {
            log.error("Error limpiando base de datos: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * 🔥 NUEVO: Carga películas usando DirectMovieLoader (más simple y directo)
     */
    @GetMapping("/load-direct")
    public String loadDirect(@RequestParam(defaultValue = "5") int pages) {
        try {
            log.info("Iniciando carga DIRECTA de películas...");

            long beforeCount = movieRepository.count();
            int loaded = directMovieLoader.loadMoviesDirectly(pages);
            long afterCount = movieRepository.count();

            return String.format("✅ Carga DIRECTA completada!\n\n" +
                "📊 Páginas cargadas: %d\n" +
                "📊 Películas antes: %d\n" +
                "📊 Películas después: %d\n" +
                "📊 Nuevas cargadas: %d\n\n" +
                "🎬 Ver películas: http://localhost:8080/peliculas\n" +
                "📊 Ver total: GET /api/admin/movie-count",
                pages, beforeCount, afterCount, loaded);

        } catch (Exception e) {
            log.error("Error en carga directa: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }
}
