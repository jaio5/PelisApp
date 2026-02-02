package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.CommentRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de películas con funcionalidad básica.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final TMDBClient tmdbClient;
    private final MovieRepository movieRepository;
    private final CommentRepository commentRepository;
    private final MovieImportService movieImportService;

    public MovieDetailsDTO getCombinedByMovieId(Long id) {
        Optional<Movie> opt = movieRepository.findById(id);
        if (opt.isEmpty()) return null;
        Movie movie = opt.get();

        // Si la película tiene tmdbId, delegar a MovieImportService para actualizar campos desde TMDB
        if (movie.getTmdbId() != null) {
            movieImportService.importOrUpdateByTmdb(movie.getTmdbId());
            // refrescar entidad
            movie = movieRepository.findById(id).orElse(movie);
        }

        MovieDetailsDTO dto = new MovieDetailsDTO();
        dto.setId(movie.getId());
        dto.setTmdbId(movie.getTmdbId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getDescription());
        dto.setPosterPath(movie.getPosterPath() != null ? movie.getPosterPath() : movie.getPosterLocalPath());
        dto.setReleaseDate(movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null);

        // Try to get credits via TMDB directly for display (non-authoritative)
        JsonNode details = null;
        if (movie.getTmdbId() != null) {
            details = tmdbClient.getMovieDetails(movie.getTmdbId());
        }

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
        // Delegar en MovieImportService: importOrUpdateByTmdb devuelve la entidad persistida
        Movie m = movieImportService.importOrUpdateByTmdb(tmdbId);
        if (m == null) return null;
        return getCombinedByMovieId(m.getId());
    }
}

