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

@Component
public class TMDBDataLoader implements CommandLineRunner {

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;

    public TMDBDataLoader(TMDBClient tmdbClient, MovieRepository movieRepository) {
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int pagesToLoad = 2; // ajustar
        for (int page = 1; page <= pagesToLoad; page++) {
            JsonNode popular = tmdbClient.getPopular(page);
            if (popular == null || !popular.has("results")) continue;
            for (JsonNode item : popular.get("results")) {
                long tmdbId = item.get("id").asLong();
                // CHECK si ya existe (ajusta método según tu repo)
                Optional<Movie> existing = movieRepository.findById(tmdbId);
                if (existing.isPresent()) continue;

                // Obtener detalles completos
                JsonNode details = tmdbClient.getMovieDetails(tmdbId);
                if (details == null) continue;

                // Mapear campos mínimos (ajusta según tu entidad)
                Movie m = new Movie();
                m.setTmdbId(tmdbId);
                m.setTitle(details.path("title").asText(null));
                // si tu entidad tiene otro setter para el resumen, mapea aquí
                // m.setDescription(details.path("overview").asText(null));

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
            }
        }
    }
}