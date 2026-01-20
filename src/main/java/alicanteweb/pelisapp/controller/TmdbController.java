package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.PeliculaDTO;
import alicanteweb.pelisapp.service.TmdbService;
import alicanteweb.pelisapp.tmdb.TmdbSearchResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/external/tmdb")
public class TmdbController {

    private final TmdbService tmdbService;

    public TmdbController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<TmdbSearchResult>> search(@RequestParam String q, @RequestParam(defaultValue = "1") int page) {
        return tmdbService.searchMovies(q, page).map(ResponseEntity::ok);
    }

    @PostMapping("/import/{tmdbId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Object>> importMovie(@PathVariable Integer tmdbId) {
        return tmdbService.importMovie(tmdbId)
                .map(dto -> ResponseEntity.ok().body((Object) dto))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body((Object) e.getMessage())));
    }
}
