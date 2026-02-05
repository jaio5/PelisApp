package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para la redescarga masiva de carátulas de películas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MoviePosterRedownloadService {

    private final MovieRepository movieRepository;
    private final ImageStorageService imageStorageService;
    private final TMDBClient tmdbClient;

    /**
     * Redescarga todas las carátulas de las películas de forma sincrónica
     */
    @Transactional
    public String redownloadAllPosters() {
        log.info("🎬 Iniciando redescarga de todas las carátulas de películas...");

        List<Movie> movies = movieRepository.findAll();

        if (movies.isEmpty()) {
            return "No hay películas en la base de datos para redescargar carátulas.";
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (Movie movie : movies) {
            try {
                boolean success = redownloadMoviePoster(movie);
                if (success) {
                    successCount.incrementAndGet();
                    log.info("✅ Carátula redescargada: {} (ID: {})", movie.getTitle(), movie.getId());
                } else {
                    failureCount.incrementAndGet();
                    log.warn("⚠️ No se pudo redescargar carátula: {} (ID: {})", movie.getTitle(), movie.getId());
                }

                // Pausa pequeña entre descargas para no sobrecargar TMDB
                Thread.sleep(200);

            } catch (Exception e) {
                failureCount.incrementAndGet();
                log.error("❌ Error redescargando carátula de {}: {}", movie.getTitle(), e.getMessage());
            }
        }

        String result = String.format(
            "🎬 Redescarga de carátulas completada: %d exitosas, %d fallidas de %d total",
            successCount.get(), failureCount.get(), movies.size()
        );

        log.info(result);
        return result;
    }

    /**
     * Redescarga todas las carátulas de forma asincrónica
     */
    public CompletableFuture<String> redownloadAllPostersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return redownloadAllPosters();
            } catch (Exception e) {
                log.error("Error en redescarga asincrónica: {}", e.getMessage());
                return "Error durante la redescarga: " + e.getMessage();
            }
        });
    }

    /**
     * Redescarga la carátula de una película específica
     */
    @Transactional
    public boolean redownloadMoviePoster(Movie movie) {
        if (movie.getPosterPath() == null || movie.getPosterPath().isBlank()) {
            log.debug("Película {} no tiene poster_path, saltando...", movie.getTitle());
            return false;
        }

        try {
            // Construir URL completa de TMDB
            String fullUrl = tmdbClient.buildImageUrl(movie.getPosterPath());
            if (fullUrl == null) {
                return false;
            }

            // Forzar redescarga de la imagen
            String filename = "movie_" + movie.getTmdbId();
            String localPath = imageStorageService.forceDownloadAndStoreImage(
                fullUrl, "posters", filename);

            if (localPath != null) {
                // Actualizar la ruta local en la base de datos
                movie.setPosterLocalPath(localPath);
                movieRepository.save(movie);
                return true;
            }

        } catch (Exception e) {
            log.error("Error redescargando poster de {}: {}", movie.getTitle(), e.getMessage());
        }

        return false;
    }

    /**
     * Redescarga carátulas de películas específicas por IDs
     */
    @Transactional
    public String redownloadPostersByIds(List<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return "No se proporcionaron IDs de películas.";
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (Long movieId : movieIds) {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                failureCount.incrementAndGet();
                continue;
            }

            try {
                boolean success = redownloadMoviePoster(movie);
                if (success) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }

                Thread.sleep(200); // Pausa entre descargas

            } catch (Exception e) {
                failureCount.incrementAndGet();
                log.error("Error redescargando película ID {}: {}", movieId, e.getMessage());
            }
        }

        return String.format(
            "Redescarga selectiva completada: %d exitosas, %d fallidas de %d solicitadas",
            successCount.get(), failureCount.get(), movieIds.size()
        );
    }

    /**
     * Obtiene estadísticas de imágenes
     */
    public String getImageStats() {
        List<Movie> movies = movieRepository.findAll();

        long totalMovies = movies.size();
        long moviesWithPosterPath = movies.stream()
            .mapToLong(m -> m.getPosterPath() != null && !m.getPosterPath().isBlank() ? 1 : 0)
            .sum();
        long moviesWithLocalPath = movies.stream()
            .mapToLong(m -> m.getPosterLocalPath() != null && !m.getPosterLocalPath().isBlank() ? 1 : 0)
            .sum();

        return String.format(
            "📊 Estadísticas de carátulas: %d películas total, %d con poster_path, %d con imagen local",
            totalMovies, moviesWithPosterPath, moviesWithLocalPath
        );
    }

    /**
     * Descarga carátulas solo si no existen localmente (descarga inteligente)
     */
    @Transactional
    public String downloadMissingPosters() {
        log.info("🎬 Iniciando descarga inteligente de carátulas faltantes...");

        List<Movie> movies = movieRepository.findAll();

        if (movies.isEmpty()) {
            return "No hay películas en la base de datos para verificar carátulas.";
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        for (Movie movie : movies) {
            try {
                // Verificar si ya existe la imagen
                if (posterExists(movie)) {
                    skippedCount.incrementAndGet();
                    log.debug("⏭️ Carátula ya existe, saltando: {} (ID: {})", movie.getTitle(), movie.getId());
                    continue;
                }

                boolean success = downloadMoviePosterIfMissing(movie);
                if (success) {
                    successCount.incrementAndGet();
                    log.info("✅ Carátula descargada: {} (ID: {})", movie.getTitle(), movie.getId());
                } else {
                    failureCount.incrementAndGet();
                    log.warn("⚠️ No se pudo descargar carátula: {} (ID: {})", movie.getTitle(), movie.getId());
                }

                // Pausa pequeña entre descargas para no sobrecargar TMDB
                Thread.sleep(200);

            } catch (Exception e) {
                failureCount.incrementAndGet();
                log.error("❌ Error descargando carátula de {}: {}", movie.getTitle(), e.getMessage());
            }
        }

        String result = String.format(
            "🎬 Descarga inteligente completada: %d nuevas descargadas, %d fallidas, %d ya existían de %d total",
            successCount.get(), failureCount.get(), skippedCount.get(), movies.size()
        );

        log.info(result);
        return result;
    }

    /**
     * Verifica si ya existe la carátula de una película localmente
     */
    private boolean posterExists(Movie movie) {
        if (movie.getPosterLocalPath() == null || movie.getPosterLocalPath().isBlank()) {
            return false;
        }

        try {
            // Convertir ruta relativa a absoluta y verificar existencia
            String relativePath = movie.getPosterLocalPath();
            String pathWithoutBase = relativePath.replace("/images/", "");

            java.nio.file.Path fullPath = java.nio.file.Paths.get("./data/images", pathWithoutBase);
            boolean exists = java.nio.file.Files.exists(fullPath);

            if (!exists) {
                log.debug("📂 Archivo no encontrado: {}", fullPath);
            }

            return exists;
        } catch (Exception e) {
            log.debug("❌ Error verificando existencia de imagen para {}: {}", movie.getTitle(), e.getMessage());
            return false;
        }
    }

    /**
     * Descarga la carátula de una película solo si no existe
     */
    @Transactional
    public boolean downloadMoviePosterIfMissing(Movie movie) {
        if (movie.getPosterPath() == null || movie.getPosterPath().isBlank()) {
            log.debug("Película {} no tiene poster_path, saltando...", movie.getTitle());
            return false;
        }

        try {
            // Construir URL completa de TMDB
            String fullUrl = tmdbClient.buildImageUrl(movie.getPosterPath());
            if (fullUrl == null) {
                return false;
            }

            // Usar descarga normal (que verifica existencia)
            String filename = "movie_" + movie.getTmdbId();
            String localPath = imageStorageService.downloadAndStoreImage(
                fullUrl, "posters", filename);

            if (localPath != null) {
                // Actualizar la ruta local en la base de datos
                movie.setPosterLocalPath(localPath);
                movieRepository.save(movie);
                return true;
            }

        } catch (Exception e) {
            log.error("Error descargando poster de {}: {}", movie.getTitle(), e.getMessage());
        }

        return false;
    }
}
