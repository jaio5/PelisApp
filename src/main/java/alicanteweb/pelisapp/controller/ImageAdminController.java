package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.Pelicula;
import alicanteweb.pelisapp.repository.PeliculaRepository;
import alicanteweb.pelisapp.service.TmdbImageDownloader;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/images")
public class ImageAdminController {

    private final PeliculaRepository peliculaRepository;
    private final TmdbImageDownloader imageDownloader;

    public ImageAdminController(PeliculaRepository peliculaRepository, TmdbImageDownloader imageDownloader) {
        this.peliculaRepository = peliculaRepository;
        this.imageDownloader = imageDownloader;
    }

    @PostMapping("/refresh/pelicula/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> refreshPeliculaImages(@PathVariable Integer id) {
        return Mono.fromCallable(() -> peliculaRepository.findById(id))
                .flatMap(optional -> {
                    if (optional.isEmpty()) return Mono.just(ResponseEntity.notFound().build());
                    Pelicula p = optional.get();
                    // descargar poster/backdrop
                    Mono<String> posterMono = imageDownloader.downloadAndStoreReactive(p.getPosterPath(), "posters", p.getTmdbId()).onErrorResume(e -> Mono.empty());
                    Mono<String> backdropMono = imageDownloader.downloadAndStoreReactive(p.getBackdropPath(), "backdrops", p.getTmdbId()).onErrorResume(e -> Mono.empty());

                    // descargar fotos de actores y guardarlas
                    var actorMonos = p.getActores().stream()
                            .map(a -> imageDownloader.downloadAndStoreReactive(a.getTmdbProfilePath() != null ? a.getTmdbProfilePath() : a.getTmdbProfilePath(), "actors", a.getTmdbId())
                                    .flatMap(url -> Mono.fromCallable(() -> { a.setFotoUrl(url); return a; }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()))
                                    .flatMap(actor -> Mono.fromCallable(() -> { /* guardar actor */ return actor; }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()))
                                    .onErrorResume(e -> Mono.empty()))
                            .collect(Collectors.toList());

                    // descargar fotos de directores y guardarlas
                    var directorMonos = p.getDirectores().stream()
                            .map(d -> imageDownloader.downloadAndStoreReactive(d.getTmdbProfilePath() != null ? d.getTmdbProfilePath() : d.getTmdbProfilePath(), "directors", d.getTmdbId())
                                    .flatMap(url -> Mono.fromCallable(() -> { d.setFotoUrl(url); return d; }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()))
                                    .flatMap(dir -> Mono.fromCallable(() -> { /* guardar director */ return dir; }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()))
                                    .onErrorResume(e -> Mono.empty()))
                            .collect(Collectors.toList());

                    return Mono.zip(posterMono.defaultIfEmpty(null), backdropMono.defaultIfEmpty(null), Mono.when(actorMonos), Mono.when(directorMonos))
                            .map(tuple -> ResponseEntity.ok().build());
                });
    }
}
