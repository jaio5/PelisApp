package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.service.MovieService;
import alicanteweb.pelisapp.tmdb.TMDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiagnosticoRepartoController {

    private final TMDBClient tmdbClient;
    private final MovieService movieService;
    private final MovieRepository movieRepository;

    @GetMapping("/diagnostico/reparto/{movieId}")
    public String diagnosticarReparto(@PathVariable Long movieId) {
        StringBuilder diagnostico = new StringBuilder();
        diagnostico.append("🔍 DIAGNÓSTICO COMPLETO DEL REPARTO\n\n");

        try {
            // 1. Verificar película en BD
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                return "❌ ERROR: Película con ID " + movieId + " no encontrada en base de datos";
            }

            diagnostico.append("✅ PASO 1: Película encontrada\n");
            diagnostico.append("   - ID: ").append(movie.getId()).append("\n");
            diagnostico.append("   - Título: ").append(movie.getTitle()).append("\n");
            diagnostico.append("   - TMDB ID: ").append(movie.getTmdbId()).append("\n\n");

            // 2. Verificar TMDB ID
            if (movie.getTmdbId() == null) {
                diagnostico.append("❌ PROBLEMA: La película no tiene TMDB ID\n");
                diagnostico.append("   SOLUCIÓN: Cargar películas desde TMDB con /api/admin/load-movies\n");
                return diagnostico.toString();
            }

            // 3. Test directo TMDB
            diagnostico.append("✅ PASO 2: Probando TMDB directamente\n");
            JsonNode tmdbDetails = tmdbClient.getMovieDetails(movie.getTmdbId());
            
            if (tmdbDetails == null) {
                diagnostico.append("❌ PROBLEMA: TMDB devuelve NULL\n");
                diagnostico.append("   CAUSA: API key inválida o problemas de conectividad\n");
                diagnostico.append("   API KEY ACTUAL: ").append(getApiKeyStatus()).append("\n");
                return diagnostico.toString();
            }

            diagnostico.append("   - Título TMDB: ").append(tmdbDetails.path("title").asText("N/A")).append("\n");
            diagnostico.append("   - Tiene créditos: ").append(tmdbDetails.has("credits")).append("\n");

            if (!tmdbDetails.has("credits")) {
                diagnostico.append("❌ PROBLEMA: TMDB no devuelve créditos\n");
                diagnostico.append("   VERIFICAR: append_to_response=credits en la petición\n");
                return diagnostico.toString();
            }

            JsonNode credits = tmdbDetails.path("credits");
            JsonNode cast = credits.path("cast");
            JsonNode crew = credits.path("crew");

            diagnostico.append("   - Actores en TMDB: ").append(cast.size()).append("\n");
            diagnostico.append("   - Crew en TMDB: ").append(crew.size()).append("\n");

            // 4. Verificar directores
            int directores = 0;
            for (JsonNode member : crew) {
                if ("Director".equals(member.path("job").asText())) {
                    directores++;
                }
            }
            diagnostico.append("   - Directores encontrados: ").append(directores).append("\n\n");

            // 5. Test MovieService
            diagnostico.append("✅ PASO 3: Probando MovieService\n");
            MovieDetailsDTO movieDetails = movieService.getCombinedByMovieId(movieId);
            
            if (movieDetails == null) {
                diagnostico.append("❌ PROBLEMA: MovieService devuelve NULL\n");
                return diagnostico.toString();
            }

            diagnostico.append("   - MovieDetails existe: true\n");
            diagnostico.append("   - Cast miembros: ").append(
                movieDetails.getCastMembers() != null ? movieDetails.getCastMembers().size() : "NULL"
            ).append("\n");
            diagnostico.append("   - Directores: ").append(
                movieDetails.getDirectors() != null ? movieDetails.getDirectors().size() : "NULL"
            ).append("\n\n");

            // 6. Verificar columnas BD
            diagnostico.append("✅ PASO 4: Verificación de base de datos\n");
            diagnostico.append("   ⚠️ EJECUTA ESTOS COMANDOS EN MYSQL:\n");
            diagnostico.append("   ALTER TABLE actor ADD COLUMN profile_local_path VARCHAR(500) NULL;\n");
            diagnostico.append("   ALTER TABLE director ADD COLUMN profile_local_path VARCHAR(500) NULL;\n\n");

            // 7. Primer actor de ejemplo
            if (cast.size() > 0) {
                JsonNode primerActor = cast.get(0);
                diagnostico.append("✅ EJEMPLO - PRIMER ACTOR:\n");
                diagnostico.append("   - Nombre: ").append(primerActor.path("name").asText()).append("\n");
                diagnostico.append("   - Personaje: ").append(primerActor.path("character").asText()).append("\n");
                diagnostico.append("   - Foto: ").append(primerActor.path("profile_path").asText("sin foto")).append("\n\n");
            }

            // 8. Resumen final
            if (movieDetails.getCastMembers() != null && movieDetails.getCastMembers().size() > 0) {
                diagnostico.append("🎉 ÉXITO: El reparto se está cargando correctamente!\n");
                diagnostico.append("   Si no aparece en la web, verificar el template HTML\n");
            } else {
                diagnostico.append("❌ PROBLEMA: MovieService no está procesando el reparto\n");
                diagnostico.append("   REVISAR: Lógica en MovieService.getCombinedByMovieId()\n");
            }

        } catch (Exception e) {
            diagnostico.append("💥 ERROR EXCEPCIÓN: ").append(e.getMessage()).append("\n");
            diagnostico.append("   StackTrace: ").append(e.getClass().getSimpleName()).append("\n");
        }

        return diagnostico.toString().replace("\n", "<br>");
    }

    private String getApiKeyStatus() {
        // Intentar hacer una petición simple para verificar API key
        try {
            JsonNode test = tmdbClient.getMovieDetails(155L); // The Dark Knight
            return test != null ? "✅ VÁLIDA" : "❌ INVÁLIDA";
        } catch (Exception e) {
            return "❌ ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/diagnostico/tmdb-simple")
    public String testTMDBSimple() {
        try {
            JsonNode result = tmdbClient.getMovieDetails(155L);
            if (result != null) {
                return "✅ TMDB FUNCIONA<br>" +
                       "Película: " + result.path("title").asText() + "<br>" +
                       "Tiene créditos: " + result.has("credits") + "<br>" +
                       "Cast: " + (result.has("credits") ? result.path("credits").path("cast").size() : 0) + "<br>" +
                       "Crew: " + (result.has("credits") ? result.path("credits").path("crew").size() : 0);
            } else {
                return "❌ TMDB devuelve NULL - API key inválida";
            }
        } catch (Exception e) {
            return "❌ ERROR TMDB: " + e.getMessage();
        }
    }
}
