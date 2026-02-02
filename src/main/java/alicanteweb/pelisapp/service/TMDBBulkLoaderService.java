package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para carga masiva paginada de películas desde TMDB
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TMDBBulkLoaderService {

    private final TMDBClient tmdbClient;
    private final TMDBMovieLoaderService movieLoaderService;
    private final MovieRepository movieRepository;

    private volatile boolean loadingInProgress = false;
    private volatile LoadingStatus currentStatus = new LoadingStatus();

    /**
     * Carga masiva de películas populares con paginación
     * @param maxPages Número máximo de páginas a cargar (500 páginas = ~10,000 películas)
     * @param delayBetweenPages Delay en milisegundos entre páginas
     * @return CompletableFuture con el resultado
     */
    @Async
    public CompletableFuture<LoadingResult> loadAllPopularMovies(int maxPages, int delayBetweenPages) {
        if (loadingInProgress) {
            return CompletableFuture.completedFuture(
                LoadingResult.error("Ya hay una carga en progreso. Espera a que termine.")
            );
        }

        loadingInProgress = true;
        currentStatus = new LoadingStatus();
        currentStatus.startTime = LocalDateTime.now();
        currentStatus.totalPages = maxPages;
        currentStatus.type = "Popular Movies";

        log.info("🚀 Iniciando carga masiva de películas populares: {} páginas máximo", maxPages);

        try {
            int processedMovies = 0;
            int skippedMovies = 0;
            int errorPages = 0;

            for (int page = 1; page <= maxPages; page++) {
                currentStatus.currentPage = page;

                try {
                    // Verificar si la página tiene resultados
                    JsonNode response = tmdbClient.getPopular(page);

                    if (response == null || !response.has("results")) {
                        log.debug("Página {} sin resultados, puede que hayamos llegado al final", page);
                        break;
                    }

                    JsonNode results = response.path("results");
                    if (results.size() == 0) {
                        log.debug("Página {} vacía, terminando carga", page);
                        break;
                    }

                    // Obtener información de la respuesta
                    int totalPages = response.path("total_pages").asInt(0);
                    int totalResults = response.path("total_results").asInt(0);

                    currentStatus.totalPagesAvailable = totalPages;
                    currentStatus.totalMoviesAvailable = totalResults;

                    // Procesar películas de esta página
                    int pageProcessed = processPageMovies(results);
                    processedMovies += pageProcessed;
                    skippedMovies += (results.size() - pageProcessed);

                    currentStatus.processedMovies = processedMovies;
                    currentStatus.skippedMovies = skippedMovies;

                    log.info("📄 Página {}/{} procesada: +{} películas (Total: {}, Omitidas: {})",
                        page, Math.min(maxPages, totalPages), pageProcessed, processedMovies, skippedMovies);

                    // Pausa entre páginas para no sobrecargar la API
                    if (delayBetweenPages > 0 && page < maxPages) {
                        Thread.sleep(delayBetweenPages);
                    }

                    // Si hemos alcanzado el final real de TMDB, parar
                    if (page >= totalPages) {
                        log.info("✅ Alcanzado el final de páginas disponibles en TMDB ({})", totalPages);
                        break;
                    }

                } catch (Exception e) {
                    errorPages++;
                    log.error("❌ Error procesando página {}: {}", page, e.getMessage());

                    // Si hay muchos errores consecutivos, parar
                    if (errorPages >= 5) {
                        log.error("Demasiados errores consecutivos, deteniendo carga");
                        break;
                    }
                }
            }

            currentStatus.endTime = LocalDateTime.now();
            currentStatus.completed = true;

            LoadingResult result = LoadingResult.success(
                processedMovies,
                skippedMovies,
                currentStatus.currentPage,
                currentStatus.totalPagesAvailable,
                currentStatus.totalMoviesAvailable
            );

            log.info("🎉 Carga masiva completada: {} nuevas películas procesadas en {} páginas",
                processedMovies, currentStatus.currentPage);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("💥 Error en carga masiva: {}", e.getMessage());
            return CompletableFuture.completedFuture(LoadingResult.error(e.getMessage()));
        } finally {
            loadingInProgress = false;
        }
    }

    /**
     * Carga masiva de diferentes categorías de películas
     */
    @Async
    public CompletableFuture<LoadingResult> loadAllMovieCategories(int pagesPerCategory, int delayBetweenPages) {
        if (loadingInProgress) {
            return CompletableFuture.completedFuture(
                LoadingResult.error("Ya hay una carga en progreso")
            );
        }

        loadingInProgress = true;
        log.info("🎬 Iniciando carga masiva de múltiples categorías: {} páginas por categoría", pagesPerCategory);

        try {
            int totalProcessed = 0;

            // 1. Películas populares
            log.info("📈 Cargando películas populares...");
            for (int page = 1; page <= pagesPerCategory; page++) {
                JsonNode response = tmdbClient.getPopular(page);
                if (response != null && response.has("results")) {
                    totalProcessed += processPageMovies(response.path("results"));
                }
                Thread.sleep(delayBetweenPages);
            }

            // 2. Películas mejor valoradas
            log.info("⭐ Cargando películas mejor valoradas...");
            for (int page = 1; page <= pagesPerCategory; page++) {
                JsonNode response = tmdbClient.getTopRated(page);
                if (response != null && response.has("results")) {
                    totalProcessed += processPageMovies(response.path("results"));
                }
                Thread.sleep(delayBetweenPages);
            }

            // 3. Películas en tendencia
            log.info("🔥 Cargando películas en tendencia...");
            JsonNode trending = tmdbClient.getTrending("movie", "week");
            if (trending != null && trending.has("results")) {
                totalProcessed += processPageMovies(trending.path("results"));
            }

            return CompletableFuture.completedFuture(
                LoadingResult.success(totalProcessed, 0, pagesPerCategory * 2 + 1, 0, 0)
            );

        } catch (Exception e) {
            log.error("Error en carga de múltiples categorías: {}", e.getMessage());
            return CompletableFuture.completedFuture(LoadingResult.error(e.getMessage()));
        } finally {
            loadingInProgress = false;
        }
    }

    /**
     * Procesa las películas de una página específica
     */
    @Transactional
    protected int processPageMovies(JsonNode resultsNode) {
        int processed = 0;

        for (JsonNode movieNode : resultsNode) {
            try {
                Long tmdbId = movieNode.path("id").asLong(0);
                if (tmdbId == 0) continue;

                // Verificar si ya existe
                if (movieRepository.findByTmdbId(tmdbId).isPresent()) {
                    continue; // Ya existe, saltar
                }

                // Delegar al servicio existente para obtener detalles completos
                movieLoaderService.loadMovieByTmdbId(tmdbId);
                processed++;

            } catch (Exception e) {
                log.debug("Error procesando película: {}", e.getMessage());
            }
        }

        return processed;
    }

    /**
     * Obtiene el estado actual de la carga
     */
    public LoadingStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Verifica si hay una carga en progreso
     */
    public boolean isLoadingInProgress() {
        return loadingInProgress;
    }

    /**
     * Cancela la carga en progreso (si es posible)
     */
    public boolean cancelLoading() {
        if (loadingInProgress) {
            loadingInProgress = false;
            log.info("🛑 Carga cancelada por el usuario");
            return true;
        }
        return false;
    }

    // Clases de datos para el estado y resultado
    public static class LoadingStatus {
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public String type;
        public int currentPage = 0;
        public int totalPages = 0;
        public int totalPagesAvailable = 0;
        public int processedMovies = 0;
        public int skippedMovies = 0;
        public int totalMoviesAvailable = 0;
        public boolean completed = false;

        public double getProgress() {
            if (totalPages == 0) return 0.0;
            return (double) currentPage / totalPages * 100.0;
        }
    }

    public static class LoadingResult {
        public final boolean success;
        public final String message;
        public final int processedMovies;
        public final int skippedMovies;
        public final int pagesProcessed;
        public final int totalPagesAvailable;
        public final int totalMoviesAvailable;

        private LoadingResult(boolean success, String message, int processedMovies,
                            int skippedMovies, int pagesProcessed, int totalPagesAvailable,
                            int totalMoviesAvailable) {
            this.success = success;
            this.message = message;
            this.processedMovies = processedMovies;
            this.skippedMovies = skippedMovies;
            this.pagesProcessed = pagesProcessed;
            this.totalPagesAvailable = totalPagesAvailable;
            this.totalMoviesAvailable = totalMoviesAvailable;
        }

        public static LoadingResult success(int processed, int skipped, int pages,
                                          int totalPages, int totalMovies) {
            return new LoadingResult(true,
                String.format("✅ Completado: %d películas procesadas, %d omitidas, %d páginas",
                    processed, skipped, pages),
                processed, skipped, pages, totalPages, totalMovies);
        }

        public static LoadingResult error(String errorMessage) {
            return new LoadingResult(false, "❌ Error: " + errorMessage, 0, 0, 0, 0, 0);
        }
    }
}
