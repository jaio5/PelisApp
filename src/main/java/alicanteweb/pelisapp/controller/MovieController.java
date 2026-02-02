package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.service.MovieService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id}/details")
    public MovieDetailsDTO detailsById(@PathVariable Long id) {
        return movieService.getCombinedByMovieId(id);
    }

    @GetMapping("/tmdb/{tmdbId}/details")
    public MovieDetailsDTO detailsByTmdbId(@PathVariable Long tmdbId) {
        return movieService.getCombinedByTmdbId(tmdbId);
    }
}
