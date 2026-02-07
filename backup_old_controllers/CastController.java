package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.Actor;
import alicanteweb.pelisapp.entity.Director;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.ActorRepository;
import alicanteweb.pelisapp.repository.DirectorRepository;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.service.TMDBMovieLoaderService;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar y verificar el reparto y directores de las películas
 */
@RestController
@RequestMapping("/api/cast")
@RequiredArgsConstructor
@Slf4j
public class CastController {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final TMDBMovieLoaderService tmdbMovieLoaderService;
    private final TMDBClient tmdbClient;

    /**
     * Obtiene el reparto completo de una película
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Map<String, Object>> getMovieCast(@PathVariable Long movieId) {
        try {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("movieId", movieId);
            result.put("movieTitle", movie.getTitle());

            // Información del reparto
            Set<Actor> actors = movie.getActors();
            result.put("actorsCount", actors.size());
            result.put("actors", actors.stream()
                .map(actor -> Map.of(
                    "id", actor.getId(),
                    "tmdbId", actor.getTmdbId(),
                    "name", actor.getName(),
                    "profilePath", actor.getProfilePath() != null ? actor.getProfilePath() : "",
                    "hasLocalPhoto", actor.getProfileLocalPath() != null
                ))
                .collect(Collectors.toList()));

            // Información de los directores
            Set<Director> directors = movie.getDirectors();
            result.put("directorsCount", directors.size());
            result.put("directors", directors.stream()
                .map(director -> Map.of(
                    "id", director.getId(),
                    "tmdbId", director.getTmdbId(),
                    "name", director.getName(),
                    "profilePath", director.getProfilePath() != null ? director.getProfilePath() : "",
                    "hasLocalPhoto", director.getProfileLocalPath() != null
                ))
                .collect(Collectors.toList()));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error obteniendo reparto de película {}: {}", movieId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recarga el reparto de una película específica desde TMDB
     */
    @PostMapping("/movie/{movieId}/reload")
    public ResponseEntity<Map<String, Object>> reloadMovieCast(@PathVariable Long movieId) {
        try {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                return ResponseEntity.notFound().build();
            }

            if (movie.getTmdbId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "La película no tiene TMDB ID"));
            }

            log.info("Recargando reparto para película: {} (TMDB ID: {})", movie.getTitle(), movie.getTmdbId());

            // Recargar película completa desde TMDB
            Movie updatedMovie = tmdbMovieLoaderService.loadMovieByTmdbId(movie.getTmdbId());

            if (updatedMovie != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("movieTitle", updatedMovie.getTitle());
                result.put("actorsCount", updatedMovie.getActors().size());
                result.put("directorsCount", updatedMovie.getDirectors().size());
                result.put("message", "Reparto actualizado exitosamente desde TMDB");

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "No se pudo recargar la información desde TMDB"));
            }

        } catch (Exception e) {
            log.error("Error recargando reparto de película {}: {}", movieId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * Obtiene estadísticas generales del reparto en la base de datos
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCastStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long totalActors = actorRepository.count();
            long totalDirectors = directorRepository.count();
            long totalMovies = movieRepository.count();

            // Contar actores con fotos
            long actorsWithPhotos = actorRepository.findAll().stream()
                .mapToLong(actor -> actor.getProfileLocalPath() != null ? 1 : 0)
                .sum();

            // Contar directores con fotos
            long directorsWithPhotos = directorRepository.findAll().stream()
                .mapToLong(director -> director.getProfileLocalPath() != null ? 1 : 0)
                .sum();

            // Contar películas con reparto
            long moviesWithCast = movieRepository.findAll().stream()
                .mapToLong(movie -> !movie.getActors().isEmpty() ? 1 : 0)
                .sum();

            // Contar películas con directores
            long moviesWithDirectors = movieRepository.findAll().stream()
                .mapToLong(movie -> !movie.getDirectors().isEmpty() ? 1 : 0)
                .sum();

            stats.put("totalActors", totalActors);
            stats.put("totalDirectors", totalDirectors);
            stats.put("totalMovies", totalMovies);
            stats.put("actorsWithPhotos", actorsWithPhotos);
            stats.put("directorsWithPhotos", directorsWithPhotos);
            stats.put("moviesWithCast", moviesWithCast);
            stats.put("moviesWithDirectors", moviesWithDirectors);
            stats.put("actorsWithPhotosPercent", totalActors > 0 ? (actorsWithPhotos * 100.0 / totalActors) : 0);
            stats.put("directorsWithPhotosPercent", totalDirectors > 0 ? (directorsWithPhotos * 100.0 / totalDirectors) : 0);
            stats.put("moviesWithCastPercent", totalMovies > 0 ? (moviesWithCast * 100.0 / totalMovies) : 0);
            stats.put("moviesWithDirectorsPercent", totalMovies > 0 ? (moviesWithDirectors * 100.0 / totalMovies) : 0);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de reparto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Prueba la carga de créditos desde TMDB para una película específica
     */
    @GetMapping("/test-tmdb/{tmdbId}")
    public ResponseEntity<Map<String, Object>> testTmdbCredits(@PathVariable Long tmdbId) {
        try {
            log.info("Probando carga de créditos desde TMDB para película ID: {}", tmdbId);

            JsonNode movieDetails = tmdbClient.getMovieDetails(tmdbId);
            if (movieDetails == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo obtener información de TMDB"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("tmdbId", tmdbId);
            result.put("title", movieDetails.path("title").asText());
            result.put("hasCredits", movieDetails.has("credits"));

            if (movieDetails.has("credits")) {
                JsonNode credits = movieDetails.path("credits");
                JsonNode cast = credits.path("cast");
                JsonNode crew = credits.path("crew");

                result.put("castCount", cast.size());
                result.put("crewCount", crew.size());

                // Contar directores en crew
                int directorCount = 0;
                for (JsonNode member : crew) {
                    if ("Director".equals(member.path("job").asText())) {
                        directorCount++;
                    }
                }
                result.put("directorCount", directorCount);

                // Mostrar primeros 5 actores
                result.put("sampleCast", cast.size() > 0 ?
                    cast.path(0).path("name").asText() + " como " + cast.path(0).path("character").asText() : "N/A");

            } else {
                result.put("castCount", 0);
                result.put("crewCount", 0);
                result.put("directorCount", 0);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error probando créditos de TMDB para película {}: {}", tmdbId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }
}
