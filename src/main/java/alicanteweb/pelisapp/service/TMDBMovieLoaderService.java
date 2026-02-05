package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.constants.AppConstants;
import alicanteweb.pelisapp.entity.Actor;
import alicanteweb.pelisapp.entity.CategoryEntity;
import alicanteweb.pelisapp.entity.Director;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.ActorRepository;
import alicanteweb.pelisapp.repository.CategoryRepository;
import alicanteweb.pelisapp.repository.DirectorRepository;
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
import java.util.stream.Collectors;

/**
 * Servicio para cargar películas desde TMDB API
 *
 * Responsabilidades:
 * - Carga películas con todos sus detalles desde TMDB
 * - Procesa reparto y directores automáticamente
 * - Gestiona descarga de imágenes
 * - Evita duplicados
 *
 * Aplica principios de código limpio:
 * - SRP: Una responsabilidad específica (carga completa de películas)
 * - Métodos pequeños y enfocados
 * - Uso de constantes centralizadas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TMDBMovieLoaderService {

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final ImageStorageService imageStorageService;

    /**
     * Carga películas populares desde TMDB
     *
     * @param pages número de páginas a cargar (cada página ~20 películas)
     */
    @Transactional
    public void loadPopularMovies(int pages) {
        log.info("{} Iniciando carga de películas populares - {} páginas solicitadas",
                 AppConstants.LOG_FIRE_EMOJI, pages);

        validatePagesInput(pages);

        MovieLoadingStats stats = new MovieLoadingStats();

        for (int page = 1; page <= pages; page++) {
            try {
                processPopularMoviesPage(page, pages, stats);
                addDelayBetweenRequests();
            } catch (Exception e) {
                handlePageError(page, e);
            }
        }

        logLoadingCompletion(stats, pages);
    }

    private void validatePagesInput(int pages) {
        if (pages <= 0) {
            throw new IllegalArgumentException("El número de páginas debe ser positivo");
        }
        if (pages > AppConstants.MAX_PAGES_LIMIT) {
            log.warn("{} Cargando {} páginas, esto puede tardar mucho tiempo",
                     AppConstants.LOG_WARNING_EMOJI, pages);
        }
    }

    private void processPopularMoviesPage(int page, int totalPages, MovieLoadingStats stats) {
        log.info("{} Solicitando página {} de {} a TMDB...",
                 AppConstants.LOG_INFO_EMOJI, page, totalPages);

        JsonNode response = tmdbClient.getPopular(page);

        if (response == null) {
            log.error("{} TMDB devolvió NULL para página {}. Verifica Bearer Token/API Key",
                      AppConstants.LOG_ERROR_EMOJI, page);
            return;
        }

        if (!response.has(AppConstants.TMDB_RESULTS_KEY)) {
            log.error("{} Respuesta de TMDB sin '{}' para página {}: {}",
                      AppConstants.LOG_ERROR_EMOJI, AppConstants.TMDB_RESULTS_KEY, page, response.toString());
            return;
        }

        JsonNode results = response.path(AppConstants.TMDB_RESULTS_KEY);
        int resultsCount = results.size();
        log.info("{} Página {} obtenida: {} películas en respuesta",
                 AppConstants.LOG_SUCCESS_EMOJI, page, resultsCount);

        int processed = processMoviesFromResponse(results);
        stats.addProcessed(processed);
        stats.addSkipped(resultsCount - processed);

        log.info("{} Página {} procesada: {} nuevas, {} omitidas (duplicadas)",
                 AppConstants.LOG_INFO_EMOJI, page, processed, resultsCount - processed);
    }

    private void addDelayBetweenRequests() {
        try {
            Thread.sleep(AppConstants.DELAY_BETWEEN_REQUESTS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Carga interrumpida", e);
        }
    }

    private void handlePageError(int page, Exception e) {
        log.error("{} Error cargando página {} de películas populares: {}",
                  AppConstants.LOG_ERROR_EMOJI, page, e.getMessage(), e);
    }

    private void logLoadingCompletion(MovieLoadingStats stats, int pages) {
        log.info("{} Carga completada: {} películas procesadas, {} omitidas, {} páginas",
                 AppConstants.LOG_FIRE_EMOJI, stats.getProcessed(), stats.getSkipped(), pages);
    }

    // Clase interna para estadísticas de carga
    private static class MovieLoadingStats {
        private int processed = 0;
        private int skipped = 0;

        void addProcessed(int count) { this.processed += count; }
        void addSkipped(int count) { this.skipped += count; }
        int getProcessed() { return processed; }
        int getSkipped() { return skipped; }
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
     * @return número de películas nuevas procesadas
     */
    private int processMoviesFromResponse(JsonNode resultsNode) {
        int processed = 0;

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
                        processed++;
                        log.debug("✅ Guardada: {} (tmdbId: {})", movie.getTitle(), tmdbId);
                    }
                }

                // Pausa entre requests
                Thread.sleep(100);

            } catch (Exception e) {
                log.warn("⚠️ Error procesando película: {}", e.getMessage());
            }
        }

        return processed;
    }

    /**
     * Procesa los detalles de una película individual incluyendo reparto y directores
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
                    String filename = AppConstants.MOVIE_FILE_PREFIX + movie.getTmdbId();
                    String localPath = imageStorageService.downloadAndStoreImage(fullUrl, AppConstants.POSTERS_SUBFOLDER, filename);
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

            // 🎬 NUEVO: Procesar reparto y directores desde créditos
            if (details.has(AppConstants.TMDB_CREDITS_KEY)) {
                JsonNode credits = details.path(AppConstants.TMDB_CREDITS_KEY);

                // Procesar actores
                Set<Actor> actors = processCast(credits.path(AppConstants.TMDB_CAST_KEY));
                movie.setActors(actors);
                log.debug("Procesados {} actores para {}", actors.size(), movie.getTitle());

                // Procesar directores
                Set<Director> directors = processCrew(credits.path(AppConstants.TMDB_CREW_KEY));
                movie.setDirectors(directors);
                log.debug("Procesados {} directores para {}", directors.size(), movie.getTitle());
            } else {
                log.debug("No se encontraron créditos para la película: {}", movie.getTitle());
            }

            return movie;

        } catch (Exception e) {
            log.error("Error procesando detalles de película: {}", e.getMessage(), e);
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

    /**
     * Procesa el reparto (cast) de una película con información detallada
     */
    private Set<Actor> processCast(JsonNode castNode) {
        Set<Actor> actors = new HashSet<>();

        if (castNode == null || !castNode.isArray()) {
            log.debug("No se encontró información de reparto");
            return actors;
        }

        // Limitar usando constante para evitar sobrecarga
        int maxActors = Math.min(AppConstants.MAX_ACTORS_PER_MOVIE, castNode.size());

        log.debug("Procesando reparto: {} actores disponibles, limitando a {}", castNode.size(), maxActors);

        for (int i = 0; i < maxActors; i++) {
            JsonNode actorNode = castNode.get(i);

            try {
                Long tmdbId = actorNode.path(AppConstants.TMDB_ID_KEY).asLong(0);
                String name = actorNode.path(AppConstants.TMDB_NAME_KEY).asText();
                String character = actorNode.path("character").asText();
                String creditId = actorNode.path("credit_id").asText();
                Integer order = actorNode.path("order").asInt(i);

                if (tmdbId == 0 || name.isBlank()) {
                    log.debug("Saltando actor sin ID o nombre válido: tmdbId={}, name={}", tmdbId, name);
                    continue;
                }

                // Buscar o crear actor con información detallada
                Actor actor = findOrCreateActorWithDetails(tmdbId, name,
                    actorNode.path(AppConstants.TMDB_PROFILE_PATH_KEY).asText());

                actors.add(actor);

                log.debug("✓ Actor procesado: {} como '{}' (orden: {})", name, character, order);

            } catch (Exception e) {
                log.warn("Error procesando actor en posición {}: {}", i, e.getMessage());
            }
        }

        log.info("Reparto procesado: {} actores principales añadidos", actors.size());
        return actors;
    }

    /**
     * Procesa la crew de una película para extraer directores con información detallada
     */
    private Set<Director> processCrew(JsonNode crewNode) {
        Set<Director> directors = new HashSet<>();

        if (crewNode == null || !crewNode.isArray()) {
            log.debug("No se encontró información de crew");
            return directors;
        }

        log.debug("Procesando crew: {} miembros disponibles", crewNode.size());

        for (JsonNode crewMember : crewNode) {
            try {
                String job = crewMember.path(AppConstants.TMDB_JOB_KEY).asText();
                String department = crewMember.path("department").asText();

                // Procesar directores y otros roles importantes de dirección
                if (!AppConstants.TMDB_DIRECTOR_JOB.equals(job) &&
                    !"Executive Producer".equals(job) &&
                    !"Producer".equals(job)) {
                    continue;
                }

                // Solo guardar directores reales
                if (!AppConstants.TMDB_DIRECTOR_JOB.equals(job)) {
                    continue;
                }

                Long tmdbId = crewMember.path(AppConstants.TMDB_ID_KEY).asLong(0);
                String name = crewMember.path(AppConstants.TMDB_NAME_KEY).asText();
                String creditId = crewMember.path("credit_id").asText();

                if (tmdbId == 0 || name.isBlank()) {
                    log.debug("Saltando crew member sin ID o nombre válido: tmdbId={}, name={}", tmdbId, name);
                    continue;
                }

                // Buscar o crear director con información detallada
                Director director = findOrCreateDirectorWithDetails(tmdbId, name,
                    crewMember.path(AppConstants.TMDB_PROFILE_PATH_KEY).asText());

                directors.add(director);

                log.debug("✓ Director procesado: {} ({})", name, job);

            } catch (Exception e) {
                log.warn("Error procesando crew member: {}", e.getMessage());
            }
        }

        log.info("Directores procesados: {} directores añadidos", directors.size());
        return directors;
    }

    /**
     * Busca o crea un actor con información detallada
     */
    private Actor findOrCreateActorWithDetails(Long tmdbId, String name, String profilePath) {
        return actorRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> {
                    log.debug("Creando nuevo actor: {}", name);
                    Actor actor = new Actor();
                    actor.setTmdbId(tmdbId);
                    actor.setName(name);

                    // Configurar foto de perfil
                    if (!profilePath.isBlank() && !"/".equals(profilePath) && !"null".equals(profilePath)) {
                        actor.setProfilePath(profilePath);

                        // Intentar descargar foto del perfil
                        try {
                            String fullUrl = tmdbClient.buildImageUrl(profilePath);
                            String filename = AppConstants.ACTOR_FILE_PREFIX + tmdbId;
                            String localPath = imageStorageService.downloadAndStoreImage(
                                fullUrl, AppConstants.PROFILES_SUBFOLDER, filename);
                            if (localPath != null) {
                                actor.setProfileLocalPath(localPath);
                                log.debug("✓ Foto descargada para actor {}: {}", name, localPath);
                            }
                        } catch (Exception e) {
                            log.debug("⚠ No se pudo descargar foto para actor {}: {}", name, e.getMessage());
                        }
                    }

                    // Aquí se podría añadir lógica para obtener información biográfica adicional
                    // del actor mediante una llamada separada a la API de TMDB si es necesario

                    Actor savedActor = actorRepository.save(actor);
                    log.debug("✓ Actor guardado: {} (ID: {})", name, savedActor.getId());
                    return savedActor;
                });
    }

    /**
     * Busca o crea un director con información detallada
     */
    private Director findOrCreateDirectorWithDetails(Long tmdbId, String name, String profilePath) {
        return directorRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> {
                    log.debug("Creando nuevo director: {}", name);
                    Director director = new Director();
                    director.setTmdbId(tmdbId);
                    director.setName(name);

                    // Configurar foto de perfil
                    if (!profilePath.isBlank() && !"/".equals(profilePath) && !"null".equals(profilePath)) {
                        director.setProfilePath(profilePath);

                        // Intentar descargar foto del perfil
                        try {
                            String fullUrl = tmdbClient.buildImageUrl(profilePath);
                            String filename = AppConstants.DIRECTOR_FILE_PREFIX + tmdbId;
                            String localPath = imageStorageService.downloadAndStoreImage(
                                fullUrl, AppConstants.PROFILES_SUBFOLDER, filename);
                            if (localPath != null) {
                                director.setProfileLocalPath(localPath);
                                log.debug("✓ Foto descargada para director {}: {}", name, localPath);
                            }
                        } catch (Exception e) {
                            log.debug("⚠ No se pudo descargar foto para director {}: {}", name, e.getMessage());
                        }
                    }

                    // Aquí se podría añadir lógica para obtener información biográfica adicional
                    // del director mediante una llamada separada a la API de TMDB si es necesario

                    Director savedDirector = directorRepository.save(director);
                    log.debug("✓ Director guardado: {} (ID: {})", name, savedDirector.getId());
                    return savedDirector;
                });
    }

    // Métodos legacy mantenidos para compatibilidad
    private Actor findOrCreateActor(Long tmdbId, String name, String profilePath) {
        return findOrCreateActorWithDetails(tmdbId, name, profilePath);
    }

    private Director findOrCreateDirector(Long tmdbId, String name, String profilePath) {
        return findOrCreateDirectorWithDetails(tmdbId, name, profilePath);
    }
}
