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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;

@NullMarked
@Service
public class TmdbService {

    private final TmdbClient tmdbClient;
    private final PeliculaRepository peliculaRepository;
    private final TmdbImageService imageService;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final CategoriaRepository categoriaRepository;

    public TmdbService(TmdbClient tmdbClient, PeliculaRepository peliculaRepository, TmdbImageService imageService,
                       ActorRepository actorRepository, DirectorRepository directorRepository,
                       CategoriaRepository categoriaRepository) {
        this.tmdbClient = tmdbClient;
        this.peliculaRepository = peliculaRepository;
        this.imageService = imageService;
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
                .flatMap(tuple -> Mono.fromCallable(() -> {
                    TmdbMovieDetail detail = tuple.getT1();
                    TmdbCredits credits = tuple.getT2();

                    // Mapear detalle a entidad Pelicula
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

                    // Persistir géneros (desde detail)
                    if (detail.getGenres() != null) {
                        for (var g : detail.getGenres()) {
                            Categoria cat = categoriaRepository.findByNombre(g.getName())
                                    .orElseGet(() -> {
                                        Categoria c = new Categoria();
                                        c.setNombre(g.getName());
                                        return categoriaRepository.save(c);
                                    });
                            saved.getCategorias().add(cat);
                        }
                    }

                    // Persistir credits (actores y directores) usando la información ya obtenida
                    if (credits.getCast() != null) {
                        credits.getCast().stream().limit(10).forEach(cast -> {
                            Actor actor = null;
                            if (cast.getId() != null) {
                                actor = actorRepository.findByTmdbId(cast.getId()).orElse(null);
                            }
                            if (actor == null) {
                                actor = actorRepository.findByNombre(cast.getName()).orElse(null);
                            }
                            if (actor == null) {
                                Actor a = new Actor();
                                a.setTmdbId(cast.getId());
                                a.setNombre(cast.getName());
                                a.setBiografia(null);
                                actor = actorRepository.save(a);
                            }
                            saved.getActores().add(actor);
                        });
                    }
                    if (credits.getCrew() != null) {
                        credits.getCrew().stream().filter(crew -> "Director".equalsIgnoreCase(crew.getJob())).limit(5).forEach(crew -> {
                            Director d = null;
                            if (crew.getId() != null) {
                                d = directorRepository.findByTmdbId(crew.getId()).orElse(null);
                            }
                            if (d == null) {
                                d = directorRepository.findByNombre(crew.getName()).orElse(null);
                            }
                            if (d == null) {
                                Director dir = new Director();
                                dir.setTmdbId(crew.getId());
                                dir.setNombre(crew.getName());
                                d = directorRepository.save(dir);
                            }
                            saved.getDirectores().add(d);
                        });
                    }

                    // Guardar relaciones finales
                    peliculaRepository.save(saved);

                    String posterUrl = imageService.posterUrl(saved.getPosterPath());
                    String backdropUrl = imageService.posterUrl(saved.getBackdropPath(), "w780");
                    return new PeliculaDTO(saved.getId(), saved.getTitulo(), saved.getAnio(), posterUrl, backdropUrl, saved.getReleaseDate(), saved.getTmdbId());
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    private Pelicula mapToPelicula(TmdbMovieDetail d) {
        Pelicula p = new Pelicula();
        p.setTmdbId(d.getId());
        p.setTitulo(d.getTitle());
        if (d.getReleaseDate() != null && !d.getReleaseDate().isBlank()) {
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
