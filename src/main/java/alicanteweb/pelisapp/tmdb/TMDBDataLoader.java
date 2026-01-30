package alicanteweb.pelisapp.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.entity.Movie;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(prefix = "app.tmdb", name = "load-on-startup", havingValue = "true")
public class TMDBDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TMDBDataLoader.class);

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;

    public TMDBDataLoader(TMDBClient tmdbClient, MovieRepository movieRepository) {
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public void run(@Nullable String... args) {
        log.info("TMDBDataLoader starting (load-on-startup=true)");
        int pagesToLoad = 2; // ajustar
        int saved = 0;
        int checked = 0;
        for (int page = 1; page <= pagesToLoad; page++) {
            log.info("Loading TMDB popular page {}", page);
            JsonNode popular = tmdbClient.getPopular(page);
            if (popular == null || !popular.has("results")) {
                log.warn("No results for popular page {} or API returned null", page);
                continue;
            }
            for (JsonNode item : popular.get("results")) {
                long tmdbId = item.get("id").asLong();
                checked++;
                // Buscar por tmdbId para evitar duplicados
                Optional<Movie> existing = movieRepository.findByTmdbId(tmdbId);
                if (existing.isPresent()) {
                    continue;
                }

                // Obtener detalles completos
                JsonNode details = tmdbClient.getMovieDetails(tmdbId);
                if (details == null) {
                    log.warn("No details for tmdbId {} (page {})", tmdbId, page);
                    continue;
                }

                try {
                    // Mapear campos mínimos (ajusta según tu entidad)
                    Movie m = new Movie();
                    m.setTmdbId(tmdbId);
                    m.setTitle(details.path("title").asText(null));

                    if (details.hasNonNull("release_date")) {
                        String rd = details.path("release_date").asText(null);
                        if (rd != null && !rd.isBlank()) {
                            try {
                                LocalDate date = LocalDate.parse(rd);
                                m.setReleaseDate(date);
                            } catch (DateTimeParseException e) {
                                // formato inesperado: ignora la fecha
                            }
                        }
                    }

                    m.setPosterPath(details.path("poster_path").asText(null));
                    // TODO: mapear actores, directores, categorias si los necesitas

                    movieRepository.save(m);
                    saved++;
                } catch (Exception e) {
                    log.error("Failed to save movie tmdbId={} due to {}", tmdbId, e.toString());
                }
            }
        }
        log.info("TMDBDataLoader finished: checked={}, saved={}", checked, saved);
    }
}