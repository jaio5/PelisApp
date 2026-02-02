package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.CategoryEntity;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.CategoryRepository;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio para cargar películas desde TMDB API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TMDBMovieLoaderService {

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;

    /**
     * Carga películas populares desde TMDB
     */
    @Transactional
    public void loadPopularMovies(int pages) {
        log.info("Cargando películas populares desde TMDB - {} páginas", pages);

        for (int page = 1; page <= pages; page++) {
            try {
                JsonNode response = tmdbClient.getPopular(page);
                if (response != null && response.has("results")) {
                    processMoviesFromResponse(response.path("results"));
                }

                // Pausa entre requests para no sobrecargar la API
                Thread.sleep(250);

            } catch (Exception e) {
                log.error("Error cargando página {} de películas populares: {}", page, e.getMessage());
            }
        }
    }

    /**
     * Busca y carga una película específica por ID de TMDB
     */
    @Transactional
    public Movie loadMovieByTmdbId(Long tmdbId) {
        // Verificar si ya existe
        Optional<Movie> existing = movieRepository.findByTmdbId(tmdbId);
        if (existing.isPresent()) {
            log.debug("Película con TMDB ID {} ya existe: {}", tmdbId, existing.get().getTitle());
            return existing.get();
        }

        try {
            JsonNode movieDetails = tmdbClient.getMovieDetails(tmdbId);
            if (movieDetails != null) {
                Movie movie = processMovieDetails(movieDetails);
                if (movie != null) {
                    movie = movieRepository.save(movie);
                    log.info("Película cargada desde TMDB: {} (ID: {})", movie.getTitle(), tmdbId);
                    return movie;
                }
            }
        } catch (Exception e) {
            log.error("Error cargando película con TMDB ID {}: {}", tmdbId, e.getMessage());
        }

        return null;
    }

    /**
     * Procesa una lista de películas desde TMDB
     */
    private void processMoviesFromResponse(JsonNode resultsNode) {
        for (JsonNode movieNode : resultsNode) {
            try {
                Long tmdbId = movieNode.path("id").asLong(0);
                if (tmdbId == 0) continue;

                // Verificar si ya existe
                if (movieRepository.findByTmdbId(tmdbId).isPresent()) {
                    continue; // Ya existe, saltar
                }

                // Obtener detalles completos de la película
                JsonNode movieDetails = tmdbClient.getMovieDetails(tmdbId);
                if (movieDetails != null) {
                    Movie movie = processMovieDetails(movieDetails);
                    if (movie != null) {
                        movieRepository.save(movie);
                        log.debug("Película guardada: {}", movie.getTitle());
                    }
                }

                // Pausa entre requests
                Thread.sleep(100);

            } catch (Exception e) {
                log.warn("Error procesando película: {}", e.getMessage());
            }
        }
    }

    /**
     * Procesa los detalles de una película individual
     */
    private Movie processMovieDetails(JsonNode details) {
        try {
            Movie movie = new Movie();

            // Datos básicos
            movie.setTmdbId(details.path("id").asLong());
            movie.setTitle(details.path("title").asText());
            movie.setDescription(details.path("overview").asText());

            // Fecha de lanzamiento
            String releaseDateStr = details.path("release_date").asText();
            if (!releaseDateStr.isBlank()) {
                try {
                    movie.setReleaseDate(LocalDate.parse(releaseDateStr));
                } catch (Exception e) {
                    log.debug("Error parseando fecha para {}: {}", movie.getTitle(), releaseDateStr);
                }
            }

            // Duración
            int runtime = details.path("runtime").asInt(0);
            if (runtime > 0) {
                movie.setRuntimeMinutes(runtime);
            }

            // Poster
            String posterPath = details.path("poster_path").asText();
            if (!posterPath.isBlank()) {
                movie.setPosterPath(posterPath);

                // Intentar descargar imagen local
                try {
                    String fullUrl = tmdbClient.buildImageUrl(posterPath);
                    String filename = "movie_" + movie.getTmdbId();
                    String localPath = imageStorageService.downloadAndStoreImage(fullUrl, "posters", filename);
                    if (localPath != null) {
                        movie.setPosterLocalPath(localPath);
                        log.debug("Imagen descargada para {}: {}", movie.getTitle(), localPath);
                    }
                } catch (Exception e) {
                    log.debug("No se pudo descargar imagen para {}: {}", movie.getTitle(), e.getMessage());
                }
            }

            // Géneros/Categorías
            Set<CategoryEntity> categories = new HashSet<>();
            if (details.has("genres")) {
                for (JsonNode genreNode : details.path("genres")) {
                    String genreName = genreNode.path("name").asText();
                    if (!genreName.isBlank()) {
                        CategoryEntity category = findOrCreateCategory(genreName);
                        categories.add(category);
                    }
                }
            }
            movie.setCategories(categories);

            return movie;

        } catch (Exception e) {
            log.error("Error procesando detalles de película: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Busca o crea una categoría
     */
    private CategoryEntity findOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    CategoryEntity category = new CategoryEntity();
                    category.setName(name);
                    return categoryRepository.save(category);
                });
    }

    /**
     * Carga películas trending (tendencias)
     */
    @Transactional
    public void loadTrendingMovies() {
        try {
            JsonNode response = tmdbClient.getTrending("movie", "week");
            if (response != null && response.has("results")) {
                processMoviesFromResponse(response.path("results"));
                log.info("Películas trending cargadas exitosamente");
            }
        } catch (Exception e) {
            log.error("Error cargando películas trending: {}", e.getMessage());
        }
    }

    /**
     * Carga películas top rated
     */
    @Transactional
    public void loadTopRatedMovies(int pages) {
        log.info("Cargando películas top rated desde TMDB - {} páginas", pages);

        for (int page = 1; page <= pages; page++) {
            try {
                JsonNode response = tmdbClient.getTopRated(page);
                if (response != null && response.has("results")) {
                    processMoviesFromResponse(response.path("results"));
                }
                Thread.sleep(250);
            } catch (Exception e) {
                log.error("Error cargando página {} de top rated: {}", page, e.getMessage());
            }
        }
    }
}
