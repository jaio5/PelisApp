package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.dto.MovieDetailsDTO.CommentDTO;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.entity.Comment;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.CommentRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    private String readStringProp(Object bean, String... props) {
        BeanWrapper bw = new BeanWrapperImpl(bean);
        for (String p : props) {
            try {
                if (bw.isReadableProperty(p)) {
                    Object v = bw.getPropertyValue(p);
                    if (v != null) return String.valueOf(v);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Object readProp(Object bean, String prop) {
        BeanWrapper bw = new BeanWrapperImpl(bean);
        try {
            if (bw.isReadableProperty(prop)) return bw.getPropertyValue(prop);
        } catch (Exception ignored) {}
        return null;
    }

    private void setPropIfBlank(Object bean, String prop, String value) {
        if (value == null) return;
        BeanWrapper bw = new BeanWrapperImpl(bean);
        try {
            if (!bw.isWritableProperty(prop)) return;
            Object current = bw.getPropertyValue(prop);
            boolean empty = current == null || (current instanceof String && ((String) current).isBlank());
            if (!empty) return;

            Class<?> type = bw.getPropertyType(prop);
            if (type != null && type.equals(LocalDate.class)) {
                try { bw.setPropertyValue(prop, LocalDate.parse(value)); }
                catch (Exception ignored) { /* ignore parsing errors */ }
            } else {
                bw.setPropertyValue(prop, value);
            }
        } catch (Exception ignored) {}
    }

    public MovieDetailsDTO getCombinedByMovieId(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Película no encontrada"));

        // obtener tmdbId dinámicamente
        Long tmdbId = null;
        Object tmdbObj = readProp(movie, "tmdbId");
        if (tmdbObj instanceof Number) tmdbId = ((Number) tmdbObj).longValue();

        JsonNode details = null;
        if (tmdbId != null) details = tmdbClient.getMovieDetails(tmdbId);

        MovieDetailsDTO dto = new MovieDetailsDTO();
        // id si existe en la entidad
        Object idObj = readProp(movie, "id");
        if (idObj instanceof Number) dto.setId(((Number) idObj).longValue());
        dto.setTmdbId(tmdbId);

        // Título
        String titleLocal = readStringProp(movie, "title", "name");
        String titleRemote = details != null ? details.path("title").asText(null) : null;
        if ((titleLocal == null || titleLocal.isBlank()) && titleRemote != null && !titleRemote.isBlank()) {
            setPropIfBlank(movie, "title", titleRemote);
            movieRepository.save(movie);
            dto.setTitle(titleRemote);
        } else {
            dto.setTitle(titleLocal != null ? titleLocal : titleRemote);
        }

        // Overview / descripción (intenta varios nombres)
        String overviewLocal = readStringProp(movie, "overview", "description", "synopsis");
        String overviewRemote = details != null ? details.path("overview").asText(null) : null;
        if ((overviewLocal == null || overviewLocal.isBlank()) && overviewRemote != null && !overviewRemote.isBlank()) {
            setPropIfBlank(movie, "overview", overviewRemote);
            setPropIfBlank(movie, "description", overviewRemote);
            movieRepository.save(movie);
            dto.setOverview(overviewRemote);
        } else {
            dto.setOverview(overviewLocal);
        }

        // Poster
        String posterLocal = readStringProp(movie, "posterPath", "poster", "poster_path");
        String posterRemote = details != null ? details.path("poster_path").asText(null) : null;
        if ((posterLocal == null || posterLocal.isBlank()) && posterRemote != null && !posterRemote.isBlank()) {
            setPropIfBlank(movie, "posterPath", posterRemote);
            movieRepository.save(movie);
            dto.setPosterPath(posterRemote);
        } else {
            dto.setPosterPath(posterLocal);
        }

        // Release date
        String releaseLocal = null;
        Object releaseObj = readProp(movie, "releaseDate");
        if (releaseObj instanceof LocalDate) releaseLocal = releaseObj.toString();
        else if (releaseObj != null) releaseLocal = String.valueOf(releaseObj);
        String releaseRemote = details != null ? details.path("release_date").asText(null) : null;
        if ((releaseLocal == null || releaseLocal.isBlank()) && releaseRemote != null && !releaseRemote.isBlank()) {
            setPropIfBlank(movie, "releaseDate", releaseRemote);
            movieRepository.save(movie);
            dto.setReleaseDate(releaseRemote);
        } else {
            dto.setReleaseDate(releaseLocal);
        }

        // Cast (primeros 6)
        List<String> cast = new ArrayList<>();
        if (details != null && details.has("credits") && details.path("credits").has("cast")) {
            int limit = 6;
            for (JsonNode a : details.path("credits").path("cast")) {
                if (cast.size() >= limit) break;
                String name = a.path("name").asText(null);
                if (name != null && !name.isBlank()) cast.add(name);
            }
        }
        dto.setCast(cast);

        // Comentarios desde BD
        List<Comment> comments = new ArrayList<>();
        Object movieIdObj = readProp(movie, "id");
        Long movieId = null;
        if (movieIdObj instanceof Number) movieId = ((Number) movieIdObj).longValue();
        if (movieId != null) {
            comments = commentRepository.findByMovieId(movieId);
        }
        List<CommentDTO> commentDTOs = new ArrayList<>();
        for (Comment c : comments) {
            commentDTOs.add(new CommentDTO(c.getId(), c.getAuthor(), c.getText(), c.getRating()));
        }
        dto.setComments(commentDTOs);

        return dto;
    }

    public MovieDetailsDTO getCombinedByTmdbId(Long tmdbId) {
        // intentar localizar localmente (sin depender de findByTmdbId)
        for (Movie m : movieRepository.findAll()) {
            Object t = readProp(m, "tmdbId");
            if (t instanceof Number && ((Number) t).longValue() == tmdbId) {
                Object idObj = readProp(m, "id");
                Long id = idObj instanceof Number ? ((Number) idObj).longValue() : null;
                if (id != null) return getCombinedByMovieId(id);
            }
        }

        // no existe localmente: obtener de TMDB y devolver sin comentarios
        JsonNode details = tmdbClient.getMovieDetails(tmdbId);
        if (details == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No encontrado en TMDB");

        MovieDetailsDTO dto = new MovieDetailsDTO();
        dto.setTmdbId(tmdbId);
        dto.setTitle(details.path("title").asText(null));
        dto.setOverview(details.path("overview").asText(null));
        dto.setPosterPath(details.path("poster_path").asText(null));
        dto.setReleaseDate(details.path("release_date").asText(null));

        List<String> cast = new ArrayList<>();
        if (details.has("credits") && details.path("credits").has("cast")) {
            int limit = 6;
            for (JsonNode a : details.path("credits").path("cast")) {
                if (cast.size() >= limit) break;
                String name = a.path("name").asText(null);
                if (name != null && !name.isBlank()) cast.add(name);
            }
        }
        dto.setCast(cast);
        dto.setComments(new ArrayList<>());

        return dto;
    }
}
