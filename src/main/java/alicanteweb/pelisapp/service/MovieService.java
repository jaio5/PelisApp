package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.CommentRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;
    private final CommentRepository commentRepository;

    public MovieService(TMDBClient tmdbClient,
                        MovieRepository movieRepository,
                        CommentRepository commentRepository) {
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
        this.commentRepository = commentRepository;
    }

    // Mapper explícito: fusiona campos desde el JsonNode de TMDB a la entidad Movie
    private boolean mergeFromTmdb(Movie movie, JsonNode details) {
        if (details == null) return false;

        boolean changed = false;

        // Título
        if (isBlank(movie.getTitle()) && details.hasNonNull("title")) {
            movie.setTitle(details.path("title").asText(null));
            changed = true;
        }

        // Overview/Descripción
        if ((isBlank(movie.getDescription())) && details.hasNonNull("overview")) {
            movie.setDescription(details.path("overview").asText(null));
            changed = true;
        }

        // Fecha de estreno
        if (movie.getReleaseDate() == null && details.hasNonNull("release_date")) {
            String rd = details.path("release_date").asText(null);
            if (rd != null && !rd.isBlank()) {
                try {
                    LocalDate date = LocalDate.parse(rd);
                    movie.setReleaseDate(date);
                    changed = true;
                } catch (DateTimeParseException e) {
                    // formato inesperado: ignorar
                }
            }
        }

        // Runtime
        if (movie.getRuntimeMinutes() == null && details.hasNonNull("runtime")) {
            if (details.path("runtime").canConvertToInt()) {
                movie.setRuntimeMinutes(details.path("runtime").asInt());
                changed = true;
            }
        }

        // Poster path -> construir URL completa si es necesario
        if (isBlank(movie.getPosterPath()) && details.hasNonNull("poster_path")) {
            String poster = details.path("poster_path").asText(null);
            if (poster != null && !poster.isBlank()) {
                movie.setPosterPath(poster);
                changed = true;
            }
        }

        // Otros campos (tmdb id ya debe estar establecido por quien llama)
        return changed;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public MovieDetailsDTO getCombinedByMovieId(Long id) {
        Optional<Movie> opt = movieRepository.findById(id);
        if (opt.isEmpty()) return null;
        Movie movie = opt.get();

        JsonNode details = null;
        if (movie.getTmdbId() != null) {
            details = tmdbClient.getMovieDetails(movie.getTmdbId());
        }

        // Si hay datos remotos, aplicar merge no destructivo
        if (details != null) {
            boolean changed = mergeFromTmdb(movie, details);
            if (changed) movieRepository.save(movie);
        }

        MovieDetailsDTO dto = new MovieDetailsDTO();
        dto.setId(movie.getId());
        dto.setTmdbId(movie.getTmdbId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getDescription());
        dto.setPosterPath(movie.getPosterPath());
        dto.setReleaseDate(movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null);

        // Cast (primeros 6)
        List<String> cast = new ArrayList<>();
        if (details != null && details.has("credits") && details.path("credits").has("cast")) {
            int limit = 6;
            int count = 0;
            for (JsonNode c : details.path("credits").path("cast")) {
                if (count++ >= limit) break;
                cast.add(c.path("name").asText());
            }
        }
        dto.setCast(cast);

        // Comments
        dto.setComments(commentRepository.findByMovieId(movie.getId()).stream().map(c -> {
            MovieDetailsDTO.CommentDTO cd = new MovieDetailsDTO.CommentDTO();
            cd.setId(c.getId());
            cd.setAuthor(c.getAuthor());
            cd.setText(c.getText());
            cd.setRating(c.getRating());
            return cd;
        }).toList());

        return dto;
    }

    public MovieDetailsDTO getCombinedByTmdbId(Long tmdbId) {
        Optional<Movie> existing = movieRepository.findByTmdbId(tmdbId);
        if (existing.isPresent()) return getCombinedByMovieId(existing.get().getId());

        JsonNode details = tmdbClient.getMovieDetails(tmdbId);
        if (details == null) return null;

        Movie m = new Movie();
        m.setTmdbId(tmdbId);
        mergeFromTmdb(m, details);
        movieRepository.save(m);

        return getCombinedByMovieId(m.getId());
    }

}
