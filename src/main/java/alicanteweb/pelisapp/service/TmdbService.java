package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.PeliculaDTO;
import alicanteweb.pelisapp.entity.Actor;
import alicanteweb.pelisapp.entity.Categoria;
import alicanteweb.pelisapp.entity.Director;
import alicanteweb.pelisapp.entity.Pelicula;
import alicanteweb.pelisapp.repository.ActorRepository;
import alicanteweb.pelisapp.repository.CategoriaRepository;
import alicanteweb.pelisapp.repository.DirectorRepository;
import alicanteweb.pelisapp.repository.PeliculaRepository;
import alicanteweb.pelisapp.tmdb.TmdbClient;
import alicanteweb.pelisapp.tmdb.TmdbCredits;
import alicanteweb.pelisapp.tmdb.TmdbMovieDetail;
import alicanteweb.pelisapp.tmdb.TmdbSearchResult;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.List;

@NullMarked
@Service
public class TmdbService {

    private final TmdbClient tmdbClient;
    private final PeliculaRepository peliculaRepository;
    private final TmdbImageService imageService;
    private final TmdbImageDownloader imageDownloader;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final CategoriaRepository categoriaRepository;

    public TmdbService(TmdbClient tmdbClient, PeliculaRepository peliculaRepository, TmdbImageService imageService,
                       TmdbImageDownloader imageDownloader, ActorRepository actorRepository, DirectorRepository directorRepository,
                       CategoriaRepository categoriaRepository) {
        this.tmdbClient = tmdbClient;
        this.peliculaRepository = peliculaRepository;
        this.imageService = imageService;
        this.imageDownloader = imageDownloader;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Cacheable("tmdb.search")
    public Mono<TmdbSearchResult> searchMovies(String query, int page) {
        return tmdbClient.searchMovies(query, page);
    }

    @Cacheable(value = "tmdb.movie")
    public Mono<PeliculaDTO> importMovie(Integer tmdbId) {
        Mono<TmdbMovieDetail> detailMono = tmdbClient.getMovieDetail(tmdbId);
        Mono<TmdbCredits> creditsMono = tmdbClient.getCredits(tmdbId).defaultIfEmpty(new TmdbCredits());

        return Mono.zip(detailMono, creditsMono)
                .flatMap(tuple -> {
                    TmdbMovieDetail detail = tuple.getT1();
                    TmdbCredits credits = tuple.getT2();

                    // guardar/actualizar pelicula y categorias en hilo bloqueante
                    Mono<Pelicula> savedMovieMono = Mono.fromCallable(() -> {
                        Pelicula p = mapToPelicula(detail);
                        Pelicula saved = peliculaRepository.findByTmdbId(p.getTmdbId())
                                .map(existing -> {
                                    existing.setPosterPath(p.getPosterPath());
                                    existing.setBackdropPath(p.getBackdropPath());
                                    existing.setOriginalLanguage(p.getOriginalLanguage());
                                    existing.setPopularity(p.getPopularity());
                                    existing.setReleaseDate(p.getReleaseDate());
                                    existing.setTitulo(p.getTitulo());
                                    existing.setAnio(p.getAnio());
                                    return peliculaRepository.save(existing);
                                })
                                .orElseGet(() -> peliculaRepository.save(p));

                        for (var g : detail.getGenres()) {
                            Categoria cat = categoriaRepository.findByNombre(g.getName())
                                    .orElseGet(() -> {
                                        Categoria c = new Categoria();
                                        c.setNombre(g.getName());
                                        return categoriaRepository.save(c);
                                    });
                            saved.getCategorias().add(cat);
                        }
                        peliculaRepository.save(saved);
                        return saved;
                    }).subscribeOn(Schedulers.boundedElastic());

                    // procesar actores: para cada cast, obtener o crear Actor (persistencia bloqueante), y si creado, descargar imagen reactivamente y actualizar actor
                    Mono<List<Actor>> actoresMono = savedMovieMono.flatMap(savedMovie -> Flux.fromIterable(credits.getCast())
                            .take(10)
                            .flatMap(cast -> Mono.fromCallable(() -> actorRepository.findByTmdbId(cast.getId())
                                            .orElseGet(() -> actorRepository.findByNombre(cast.getName())
                                                    .orElseGet(() -> {
                                                        Actor a = new Actor();
                                                        a.setTmdbId(cast.getId());
                                                        a.setNombre(cast.getName());
                                                        a.setBiografia(null);
                                                        return actorRepository.save(a);
                                                    })) )
                                    .subscribeOn(Schedulers.boundedElastic())
                                    // si actor creado sin foto, descargar y guardar foto (reactivo)
                                    .flatMap(actor -> {
                                        if (actor.getFotoUrl() == null || actor.getFotoUrl().isBlank()) {
                                            return imageDownloader.downloadAndStoreReactive(cast.getProfilePath(), "actors", cast.getId())
                                                    .flatMap(url -> Mono.fromCallable(() -> {
                                                        actor.setFotoUrl(url);
                                                        return actorRepository.save(actor);
                                                    }).subscribeOn(Schedulers.boundedElastic()));
                                        }
                                        return Mono.just(actor);
                                    })
                            , 4)
                            .collectList());

                    // procesar directores (simétrico, pero sin descarga porque TmdbCrew no expone profilePath en DTO)
                    Mono<List<Director>> directoresMono = savedMovieMono.flatMap(savedMovie -> Flux.fromIterable(credits.getCrew())
                            .filter(crew -> "Director".equalsIgnoreCase(crew.getJob()))
                            .take(5)
                            .flatMap(crew -> Mono.fromCallable(() -> directorRepository.findByTmdbId(crew.getId())
                                            .orElseGet(() -> directorRepository.findByNombre(crew.getName())
                                                    .orElseGet(() -> {
                                                        Director dir = new Director();
                                                        dir.setTmdbId(crew.getId());
                                                        dir.setNombre(crew.getName());
                                                        dir.setBiografia(null);
                                                        return directorRepository.save(dir);
                                                    })) )
                                    .subscribeOn(Schedulers.boundedElastic())
                            , 4)
                            .collectList());

                    // descargar poster/backdrop de forma reactiva
                    Mono<String> posterMono = imageDownloader.downloadAndStoreReactive(detail.getPosterPath(), "posters", detail.getId())
                            .switchIfEmpty(Mono.just((String) null));
                    Mono<String> backdropMono = imageDownloader.downloadAndStoreReactive(detail.getBackdropPath(), "backdrops", detail.getId())
                            .switchIfEmpty(Mono.just((String) null));

                    return Mono.zip(savedMovieMono, actoresMono, directoresMono, posterMono, backdropMono)
                            .map(tuple2 -> {
                                Pelicula saved = tuple2.getT1();
                                List<Actor> actores = tuple2.getT2();
                                List<Director> directores = tuple2.getT3();
                                String posterUrl = tuple2.getT4();
                                String backdropUrl = tuple2.getT5();

                                // asociar actores y directores a la pelicula (bloqueante)
                                saved.getActores().addAll(actores);
                                saved.getDirectores().addAll(directores);
                                peliculaRepository.save(saved);

                                return new PeliculaDTO(saved.getId(), saved.getTitulo(), saved.getAnio(), posterUrl, backdropUrl, saved.getReleaseDate(), saved.getTmdbId());
                            }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    private Pelicula mapToPelicula(TmdbMovieDetail d) {
        Pelicula p = new Pelicula();
        p.setTmdbId(d.getId());
        p.setTitulo(d.getTitle());
        if (!d.getReleaseDate().isBlank()) {
            p.setReleaseDate(LocalDate.parse(d.getReleaseDate()));
            p.setAnio(p.getReleaseDate().getYear());
        }
        p.setPosterPath(d.getPosterPath());
        p.setBackdropPath(d.getBackdropPath());
        p.setOriginalLanguage(d.getOriginalLanguage());
        p.setPopularity(d.getPopularity());
        return p;
    }
}
