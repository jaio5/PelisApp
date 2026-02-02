package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AdminTMDBController {

    private static final Logger log = LoggerFactory.getLogger(AdminTMDBController.class);

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;

    public AdminTMDBController(TMDBClient tmdbClient, MovieRepository movieRepository) {
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
    }

    @PostMapping("/admin/tmdb/load")
    @Transactional
    public ResponseEntity<?> loadPopular(@RequestParam(value = "pages", defaultValue = "2") int pages) {
        int saved = 0;
        int checked = 0;
        List<Long> skipped = new ArrayList<>();

        for (int page = 1; page <= pages; page++) {
            JsonNode popular = tmdbClient.getPopular(page);
            if (popular == null || !popular.has("results")) continue;
            for (JsonNode item : popular.get("results")) {
                long tmdbId = item.path("id").asLong();
                checked++;
                Optional<Movie> existing = movieRepository.findByTmdbId(tmdbId);
                if (existing.isPresent()) {
                    skipped.add(tmdbId);
                    continue;
                }

                JsonNode details = tmdbClient.getMovieDetails(tmdbId);
                if (details == null) continue;

                Movie m = new Movie();
                m.setTmdbId(tmdbId);
                m.setTitle(details.path("title").asText(null));

                if (details.hasNonNull("release_date")) {
                    String rd = details.path("release_date").asText(null);
                    if (rd != null && !rd.isBlank()) {
                        try {
                            LocalDate date = LocalDate.parse(rd);
                            m.setReleaseDate(date);
                        } catch (DateTimeParseException ignored) {
                        }
                    }
                }

                m.setPosterPath(details.path("poster_path").asText(null));
                // try to save
                movieRepository.save(m);
                saved++;
                log.info("Admin saved movie tmdbId={} dbId={} title='{}' posterLocalPath={}", tmdbId, m.getId(), m.getTitle(), m.getPosterLocalPath());
            }
        }

        return ResponseEntity.ok("Checked=" + checked + ", saved=" + saved + ", skipped=" + skipped.size());
    }

    @GetMapping("/admin/tmdb/status")
    public ResponseEntity<?> status() {
        long movies = movieRepository.count();
        return ResponseEntity.ok("movies=" + movies);
    }
}
