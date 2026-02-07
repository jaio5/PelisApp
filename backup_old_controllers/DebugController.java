package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para debug y verificación del reparto en las películas
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final MovieRepository movieRepository;

    /**
     * Verifica el reparto de una película específica
     */
    @GetMapping("/movie/{id}")
    public Map<String, Object> debugMovie(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Cargar película con relaciones
            Movie movie = movieRepository.findByIdWithCastAndDirectors(id).orElse(null);

            if (movie == null) {
                result.put("error", "Película no encontrada");
                return result;
            }

            result.put("movieId", movie.getId());
            result.put("tmdbId", movie.getTmdbId());
            result.put("title", movie.getTitle());
            result.put("actorsCount", movie.getActors().size());
            result.put("directorsCount", movie.getDirectors().size());

            // Detalles de actores
            result.put("actors", movie.getActors().stream()
                .map(actor -> Map.of(
                    "name", actor.getName(),
                    "tmdbId", actor.getTmdbId() != null ? actor.getTmdbId() : "null",
                    "hasProfilePath", actor.getProfilePath() != null,
                    "hasLocalPath", actor.getProfileLocalPath() != null
                ))
                .toList());

            // Detalles de directores
            result.put("directors", movie.getDirectors().stream()
                .map(director -> Map.of(
                    "name", director.getName(),
                    "tmdbId", director.getTmdbId() != null ? director.getTmdbId() : "null",
                    "hasProfilePath", director.getProfilePath() != null,
                    "hasLocalPath", director.getProfileLocalPath() != null
                ))
                .toList());

            result.put("status", "success");

        } catch (Exception e) {
            log.error("Error debuggeando película {}: {}", id, e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Verifica todas las películas y su estado de reparto
     */
    @GetMapping("/movies/cast-status")
    public Map<String, Object> debugAllMoviesCast() {
        Map<String, Object> result = new HashMap<>();

        try {
            var movies = movieRepository.findAll();

            long moviesWithActors = movies.stream()
                .mapToLong(movie -> !movie.getActors().isEmpty() ? 1 : 0)
                .sum();

            long moviesWithDirectors = movies.stream()
                .mapToLong(movie -> !movie.getDirectors().isEmpty() ? 1 : 0)
                .sum();

            result.put("totalMovies", movies.size());
            result.put("moviesWithActors", moviesWithActors);
            result.put("moviesWithDirectors", moviesWithDirectors);
            result.put("moviesWithActorsPercent", movies.size() > 0 ? (moviesWithActors * 100.0 / movies.size()) : 0);
            result.put("moviesWithDirectorsPercent", movies.size() > 0 ? (moviesWithDirectors * 100.0 / movies.size()) : 0);

            // Películas sin reparto
            var moviesWithoutCast = movies.stream()
                .filter(movie -> movie.getActors().isEmpty())
                .limit(10)
                .map(movie -> Map.of(
                    "id", movie.getId(),
                    "title", movie.getTitle(),
                    "tmdbId", movie.getTmdbId() != null ? movie.getTmdbId() : "null"
                ))
                .toList();

            result.put("moviesWithoutCast", moviesWithoutCast);
            result.put("status", "success");

        } catch (Exception e) {
            log.error("Error debuggeando estado de reparto: {}", e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }
}
