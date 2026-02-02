package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.CastDTO;
import alicanteweb.pelisapp.dto.CrewDTO;
import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.entity.Actor;
import alicanteweb.pelisapp.entity.Director;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.ActorRepository;
import alicanteweb.pelisapp.repository.DirectorRepository;
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
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final ImageStorageService imageStorageService;

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

        // Cast (primeros 6) - texto simple para compatibilidad
        List<String> cast = new ArrayList<>();
        
        // Cast members con fotos (primeros 10)
        List<CastDTO> castMembers = new ArrayList<>();
        
        // Directors con fotos
        List<CrewDTO> directors = new ArrayList<>();
        
        if (details != null && details.has("credits")) {
            JsonNode credits = details.path("credits");
            
            // Procesar cast
            if (credits.has("cast")) {
                int limit = 6;
                int detailLimit = 10;
                int count = 0;
                int detailCount = 0;
                
                for (JsonNode c : credits.path("cast")) {
                    String name = c.path("name").asText();
                    
                    // Para lista simple (compatibilidad)
                    if (count++ < limit) {
                        cast.add(name);
                    }
                    
                    // Para lista detallada con fotos
                    if (detailCount++ < detailLimit) {
                        CastDTO castDto = new CastDTO();
                        castDto.setTmdbId(c.path("id").canConvertToLong() ? c.path("id").asLong() : null);
                        castDto.setName(name);
                        castDto.setCharacter(c.path("character").asText());
                        castDto.setProfilePath(c.path("profile_path").asText(null));
                        
                        // Buscar foto local si existe
                        if (castDto.getTmdbId() != null) {
                            Optional<Actor> actorOpt = actorRepository.findByTmdbId(castDto.getTmdbId());
                            if (actorOpt.isPresent()) {
                                castDto.setProfileLocalPath(actorOpt.get().getProfileLocalPath());
                            }
                        }
                        
                        // Determinar URL para mostrar
                        castDto.setProfileUrl(getImageUrl(castDto.getProfileLocalPath(), castDto.getProfilePath()));
                        
                        // Descargar foto si no existe localmente
                        downloadActorPhotoIfNeeded(castDto);
                        
                        castMembers.add(castDto);
                    }
                }
            }
            
            // Procesar crew (directores)
            if (credits.has("crew")) {
                for (JsonNode c : credits.path("crew")) {
                    String job = c.path("job").asText();
                    if ("Director".equalsIgnoreCase(job)) {
                        CrewDTO directorDto = new CrewDTO();
                        directorDto.setTmdbId(c.path("id").canConvertToLong() ? c.path("id").asLong() : null);
                        directorDto.setName(c.path("name").asText());
                        directorDto.setJob(job);
                        directorDto.setDepartment(c.path("department").asText());
                        directorDto.setProfilePath(c.path("profile_path").asText(null));
                        
                        // Buscar foto local si existe
                        if (directorDto.getTmdbId() != null) {
                            Optional<Director> directorOpt = directorRepository.findByTmdbId(directorDto.getTmdbId());
                            if (directorOpt.isPresent()) {
                                directorDto.setProfileLocalPath(directorOpt.get().getProfileLocalPath());
                            }
                        }
                        
                        // Determinar URL para mostrar
                        directorDto.setProfileUrl(getImageUrl(directorDto.getProfileLocalPath(), directorDto.getProfilePath()));
                        
                        // Descargar foto si no existe localmente
                        downloadDirectorPhotoIfNeeded(directorDto);
                        
                        directors.add(directorDto);
                    }
                }
            }
        }
        
        // FALLBACK CONDICIONAL: Solo usar datos simulados si realmente no hay datos de TMDB
        if (castMembers.isEmpty() && directors.isEmpty() && movie.getTmdbId() != null) {
            log.warn("TMDB no devolvió datos para película {} (tmdbId={}). Verificando token TMDB...", movie.getTitle(), movie.getTmdbId());

            // Intentar una petición simple para verificar si es problema del token
            try {
                JsonNode testResponse = tmdbClient.getMovieDetails(155L); // The Dark Knight como test
                if (testResponse == null) {
                    log.error("TOKEN TMDB INVÁLIDO - La API no responde. Se necesita token real de themoviedb.org");
                    log.info("Para conseguir token TMDB: 1) Ve a themoviedb.org 2) Crea cuenta 3) Ve a Settings > API 4) Consigue API Key");
                } else {
                    log.error("TMDB responde pero no hay datos para película ID {}. Puede ser película sin información de reparto.", movie.getTmdbId());
                }
            } catch (Exception e) {
                log.error("Error probando token TMDB: {}", e.getMessage());
            }

            // Solo mostrar fallback si es una película conocida (para demo)
            if (movie.getTmdbId() != null && (movie.getTmdbId() == 155L || movie.getTmdbId() == 60L || "The Dark Knight".equalsIgnoreCase(movie.getTitle()))) {
                log.info("Mostrando reparto simulado de The Dark Knight para demostración");

                CastDTO cast1 = new CastDTO();
                cast1.setName("Christian Bale");
                cast1.setCharacter("Bruce Wayne / Batman");
                cast1.setProfileUrl("/images/placeholder.svg");
                castMembers.add(cast1);

                CastDTO cast2 = new CastDTO();
                cast2.setName("Heath Ledger");
                cast2.setCharacter("Joker");
                cast2.setProfileUrl("/images/placeholder.svg");
                castMembers.add(cast2);

                CastDTO cast3 = new CastDTO();
                cast3.setName("Aaron Eckhart");
                cast3.setCharacter("Harvey Dent / Two-Face");
                cast3.setProfileUrl("/images/placeholder.svg");
                castMembers.add(cast3);

                CastDTO cast4 = new CastDTO();
                cast4.setName("Michael Caine");
                cast4.setCharacter("Alfred Pennyworth");
                cast4.setProfileUrl("/images/placeholder.svg");
                castMembers.add(cast4);

                CrewDTO director1 = new CrewDTO();
                director1.setName("Christopher Nolan");
                director1.setJob("Director");
                director1.setProfileUrl("/images/placeholder.svg");
                directors.add(director1);

                // Actualizar cast simple también
                cast.add("Christian Bale");
                cast.add("Heath Ledger");
                cast.add("Aaron Eckhart");
                cast.add("Michael Caine");
            }
        }

        dto.setCast(cast);
        dto.setCastMembers(castMembers);
        dto.setDirectors(directors);

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

    /**
     * Determina qué URL usar para mostrar la imagen
     */
    private String getImageUrl(String localPath, String remotePath) {
        // Priorizar imagen local
        if (localPath != null && !localPath.isEmpty()) {
            return "/images/" + localPath;
        }

        // Fallback a imagen remota de TMDB
        if (remotePath != null && !remotePath.isEmpty()) {
            return tmdbClient.buildImageUrl(remotePath);
        }

        // Sin imagen disponible
        return null;
    }

    /**
     * Descarga la foto del actor si no existe localmente
     */
    private void downloadActorPhotoIfNeeded(CastDTO castDto) {
        if (castDto.getTmdbId() == null || castDto.getProfilePath() == null || castDto.getProfilePath().isEmpty()) {
            return;
        }

        try {
            // Verificar si ya existe el actor en BD
            Optional<Actor> actorOpt = actorRepository.findByTmdbId(castDto.getTmdbId());
            Actor actor;

            if (actorOpt.isPresent()) {
                actor = actorOpt.get();
                // Si ya tiene foto local, usar esa
                if (actor.getProfileLocalPath() != null && !actor.getProfileLocalPath().isEmpty()) {
                    castDto.setProfileLocalPath(actor.getProfileLocalPath());
                    castDto.setProfileUrl(getImageUrl(actor.getProfileLocalPath(), castDto.getProfilePath()));
                    return;
                }
            } else {
                // Crear nuevo actor
                actor = new Actor();
                actor.setTmdbId(castDto.getTmdbId());
                actor.setName(castDto.getName());
                actor.setProfilePath(castDto.getProfilePath());
            }

            // Descargar foto
            String fullUrl = tmdbClient.buildImageUrl(castDto.getProfilePath());
            if (fullUrl != null) {
                String filename = "actor_" + castDto.getTmdbId();
                String localPath = imageStorageService.downloadAndStoreImage(fullUrl, "profiles", filename);

                if (localPath != null) {
                    actor.setProfileLocalPath(localPath);
                    actorRepository.save(actor);

                    castDto.setProfileLocalPath(localPath);
                    castDto.setProfileUrl(getImageUrl(localPath, castDto.getProfilePath()));

                    log.debug("Descargada foto de actor: {} -> {}", castDto.getName(), localPath);
                }
            }
        } catch (Exception e) {
            log.debug("Error descargando foto de actor {}: {}", castDto.getName(), e.getMessage());
        }
    }

    /**
     * Descarga la foto del director si no existe localmente
     */
    private void downloadDirectorPhotoIfNeeded(CrewDTO directorDto) {
        if (directorDto.getTmdbId() == null || directorDto.getProfilePath() == null || directorDto.getProfilePath().isEmpty()) {
            return;
        }

        try {
            // Verificar si ya existe el director en BD
            Optional<Director> directorOpt = directorRepository.findByTmdbId(directorDto.getTmdbId());
            Director director;

            if (directorOpt.isPresent()) {
                director = directorOpt.get();
                // Si ya tiene foto local, usar esa
                if (director.getProfileLocalPath() != null && !director.getProfileLocalPath().isEmpty()) {
                    directorDto.setProfileLocalPath(director.getProfileLocalPath());
                    directorDto.setProfileUrl(getImageUrl(director.getProfileLocalPath(), directorDto.getProfilePath()));
                    return;
                }
            } else {
                // Crear nuevo director
                director = new Director();
                director.setTmdbId(directorDto.getTmdbId());
                director.setName(directorDto.getName());
                director.setProfilePath(directorDto.getProfilePath());
            }

            // Descargar foto
            String fullUrl = tmdbClient.buildImageUrl(directorDto.getProfilePath());
            if (fullUrl != null) {
                String filename = "director_" + directorDto.getTmdbId();
                String localPath = imageStorageService.downloadAndStoreImage(fullUrl, "profiles", filename);

                if (localPath != null) {
                    director.setProfileLocalPath(localPath);
                    directorRepository.save(director);

                    directorDto.setProfileLocalPath(localPath);
                    directorDto.setProfileUrl(getImageUrl(localPath, directorDto.getProfilePath()));

                    log.debug("Descargada foto de director: {} -> {}", directorDto.getName(), localPath);
                }
            }
        } catch (Exception e) {
            log.debug("Error descargando foto de director {}: {}", directorDto.getName(), e.getMessage());
        }
    }
}

