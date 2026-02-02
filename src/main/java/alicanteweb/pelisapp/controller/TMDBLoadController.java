package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.TMDBMovieLoaderService;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para carga manual de películas desde TMDB con reparto completo
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class TMDBLoadController {

    private final TMDBMovieLoaderService tmdbMovieLoaderService;
    private final TMDBClient tmdbClient;

    @GetMapping("/test-tmdb")
    public String testTMDB() {
        try {
            log.info("Probando conexión con TMDB...");

            // Probar obtener una película conocida (The Dark Knight tiene ID 155)
            JsonNode movieDetails = tmdbClient.getMovieDetails(155L);

            if (movieDetails != null) {
                String title = movieDetails.path("title").asText("Sin título");
                boolean hasCredits = movieDetails.has("credits");
                int castSize = hasCredits ? movieDetails.path("credits").path("cast").size() : 0;
                int crewSize = hasCredits ? movieDetails.path("credits").path("crew").size() : 0;

                return String.format("✅ TMDB conectado correctamente!\n" +
                    "Película de prueba: %s\n" +
                    "Tiene créditos: %s\n" +
                    "Actores: %d\n" +
                    "Crew: %d\n" +
                    "Respuesta completa: %s",
                    title, hasCredits, castSize, crewSize,
                    movieDetails.toString().length() > 200 ?
                        movieDetails.toString().substring(0, 200) + "..." :
                        movieDetails.toString()
                );
            } else {
                return "❌ Error: TMDB devolvió null. Verifica tu API key/bearer token.";
            }
        } catch (Exception e) {
            log.error("Error probando TMDB: {}", e.getMessage());
            return "❌ Error conectando con TMDB: " + e.getMessage();
        }
    }

    @GetMapping("/test-movie/{id}")
    public String testSpecificMovie(@PathVariable Long id) {
        try {
            JsonNode movieDetails = tmdbClient.getMovieDetails(id);

            if (movieDetails != null) {
                String title = movieDetails.path("title").asText("Sin título");
                boolean hasCredits = movieDetails.has("credits");

                StringBuilder result = new StringBuilder();
                result.append("🎬 Película: ").append(title).append("\n");
                result.append("ID TMDB: ").append(id).append("\n");
                result.append("Tiene créditos: ").append(hasCredits).append("\n");

                if (hasCredits) {
                    JsonNode credits = movieDetails.path("credits");
                    JsonNode cast = credits.path("cast");
                    JsonNode crew = credits.path("crew");

                    result.append("Actores: ").append(cast.size()).append("\n");
                    result.append("Crew: ").append(crew.size()).append("\n");

                    // Mostrar primeros 3 actores
                    result.append("\nPrimeros actores:\n");
                    for (int i = 0; i < Math.min(3, cast.size()); i++) {
                        JsonNode actor = cast.get(i);
                        result.append("  - ").append(actor.path("name").asText())
                              .append(" como ").append(actor.path("character").asText())
                              .append(" (foto: ").append(actor.path("profile_path").asText("sin foto"))
                              .append(")\n");
                    }

                    // Mostrar directores
                    result.append("\nDirectores:\n");
                    for (JsonNode member : crew) {
                        if ("Director".equals(member.path("job").asText())) {
                            result.append("  - ").append(member.path("name").asText())
                                  .append(" (foto: ").append(member.path("profile_path").asText("sin foto"))
                                  .append(")\n");
                        }
                    }
                }

                return result.toString();
            } else {
                return "❌ No se encontró la película con ID: " + id;
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/load-movies")
    public String loadMovies() {
        try {
            log.info("Iniciando carga manual de películas desde TMDB...");
            tmdbMovieLoaderService.loadPopularMovies(3); // Cargar 3 páginas = ~60 películas
            return "✅ Carga completada. Películas populares cargadas con reparto y fotos.";
        } catch (Exception e) {
            log.error("Error cargando películas: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/load-trending")
    public String loadTrending() {
        try {
            log.info("Iniciando carga de películas trending...");
            // Aquí podrías añadir lógica para cargar trending si el servicio lo soporta
            tmdbMovieLoaderService.loadPopularMovies(2);
            return "✅ Carga de trending completada.";
        } catch (Exception e) {
            log.error("Error cargando trending: {}", e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }
}
