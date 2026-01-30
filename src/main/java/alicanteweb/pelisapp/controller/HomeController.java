package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.CategoryEntity;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.CategoryRepository;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import alicanteweb.pelisapp.service.ImageService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final TMDBClient tmdbClient;
    private final ImageService imageService;
    private final String tmdbImageBaseUrl;

    public HomeController(MovieRepository movieRepository,
                          CategoryRepository categoryRepository,
                          TMDBClient tmdbClient,
                          ImageService imageService,
                          @Value("${app.tmdb.image-base-url}") String tmdbImageBaseUrl) {
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.tmdbClient = tmdbClient;
        this.imageService = imageService;
        this.tmdbImageBaseUrl = tmdbImageBaseUrl;
    }

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "12") int size,
                       @RequestParam(value = "genre", required = false) String genre) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<Movie> moviesPage;
        if (genre != null && !genre.isBlank()) {
            moviesPage = movieRepository.findByCategories_Name(genre, pageable);
        } else {
            moviesPage = movieRepository.findAll(pageable);
        }

        // Si no hay resultados en la BD para esa página/filtro, traemos popular desde TMDB
        if (moviesPage.getTotalElements() == 0) {
            JsonNode popular = tmdbClient.getPopular(Math.max(1, page + 1)); // TMDB pages are 1-based
            if (popular != null && popular.has("results")) {
                List<Movie> toSave = new ArrayList<>();
                for (JsonNode item : popular.path("results")) {
                    long tmdbId = item.path("id").asLong();
                    // si ya existe, saltar
                    if (movieRepository.findByTmdbId(tmdbId).isPresent()) continue;

                    JsonNode details = tmdbClient.getMovieDetails(tmdbId);
                    if (details == null) continue;

                    Movie m = new Movie();
                    m.setTmdbId(tmdbId);
                    m.setTitle(details.path("title").asText(null));
                    m.setDescription(details.path("overview").asText(null));

                    if (details.hasNonNull("runtime")) {
                        try { m.setRuntimeMinutes(details.path("runtime").asInt()); } catch (Exception ignored) {}
                    }
                    if (details.hasNonNull("release_date")) {
                        String rd = details.path("release_date").asText(null);
                        if (rd != null && !rd.isBlank()) {
                            try { m.setReleaseDate(LocalDate.parse(rd)); } catch (DateTimeParseException ignored) {}
                        }
                    }

                    // genres
                    if (details.has("genres")) {
                        for (JsonNode g : details.path("genres")) {
                            String gname = g.path("name").asText(null);
                            if (gname == null || gname.isBlank()) continue;
                            CategoryEntity cat = categoryRepository.findByName(gname).orElseGet(() -> {
                                CategoryEntity ce = new CategoryEntity(); ce.setName(gname); return categoryRepository.save(ce);
                            });
                            m.getCategories().add(cat);
                        }
                    }

                    String posterPath = details.path("poster_path").asText(null);
                    m.setPosterPath(posterPath);
                    if (posterPath != null && !posterPath.isBlank() && tmdbImageBaseUrl != null) {
                        String posterUrl = tmdbImageBaseUrl + "/w500" + (posterPath.startsWith("/") ? posterPath : ("/" + posterPath));
                        String local = imageService.downloadAndStore(posterUrl);
                        if (local != null) m.setPosterLocalPath(local);
                    }

                    toSave.add(m);
                }
                if (!toSave.isEmpty()) movieRepository.saveAll(toSave);
                // volver a leer la página desde BD
                if (genre != null && !genre.isBlank()) moviesPage = movieRepository.findByCategories_Name(genre, pageable);
                else moviesPage = movieRepository.findAll(pageable);
            }
        }

        List<Movie> movies = moviesPage.getContent();
        List<Movie> downloadSave = new ArrayList<>();
        for (Movie m : movies) {
            // Asegurar posterPath poblado basándose en tmdbId
            if ((m.getPosterPath() == null || m.getPosterPath().isBlank()) && m.getTmdbId() != null) {
                try {
                    JsonNode details = tmdbClient.getMovieDetails(m.getTmdbId());
                    if (details != null && details.hasNonNull("poster_path")) {
                        String poster = details.path("poster_path").asText(null);
                        if (poster != null && !poster.isBlank()) m.setPosterPath(poster);
                    }
                } catch (Exception ignored) {}
            }
            // descargar local si falta
            if ((m.getPosterLocalPath() == null || m.getPosterLocalPath().isBlank()) && m.getPosterPath() != null) {
                String posterUrl = m.getPosterPath().startsWith("http") ? m.getPosterPath() : (tmdbImageBaseUrl + "/w500" + (m.getPosterPath().startsWith("/") ? m.getPosterPath() : "/" + m.getPosterPath()));
                try {
                    String local = imageService.downloadAndStore(posterUrl);
                    if (local != null) { m.setPosterLocalPath(local); downloadSave.add(m); }
                } catch (Exception ignored) {}
            }
        }
        if (!downloadSave.isEmpty()) movieRepository.saveAll(downloadSave);

        List<CategoryEntity> categories = categoryRepository.findAll();

        int totalPages = moviesPage.getTotalPages();
        int current = moviesPage.getNumber();
        int window = 5; // show up to 5 page links
        int start = Math.max(0, current - window/2);
        int end = Math.min(totalPages - 1, start + window - 1);
        if (end - start < window - 1) start = Math.max(0, end - window + 1);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = start; i <= end; i++) pageNumbers.add(i);

        model.addAttribute("moviesPage", moviesPage);
        model.addAttribute("movies", movies);
        model.addAttribute("tmdbBaseUrl", tmdbImageBaseUrl);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("currentPage", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", moviesPage.getSize());
        model.addAttribute("pageNumbers", pageNumbers);
        return "main_menu";
    }
}
