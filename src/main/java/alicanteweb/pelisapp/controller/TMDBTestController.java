package alicanteweb.pelisapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class TMDBTestController {

    @GetMapping("/test-tmdb-simple")
    public String testTMDBDirect() {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.themoviedb.org/3")
                    .build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/155")
                            .queryParam("api_key", "8265bd1679663a7ea12ac168da84d2e8")
                            .queryParam("language", "es-ES")
                            .queryParam("append_to_response", "credits")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            if (response != null && response.contains("Dark Knight")) {
                // Extraer información básica del reparto
                int castStart = response.indexOf("\"cast\":");
                int crewStart = response.indexOf("\"crew\":");

                boolean hasCast = castStart > 0 && response.indexOf("Christian Bale", castStart) > 0;
                boolean hasCrew = crewStart > 0 && response.indexOf("Christopher Nolan", crewStart) > 0;

                return "✅ TMDB FUNCIONA CORRECTAMENTE!\n\n" +
                       "🎬 Película: The Dark Knight\n" +
                       "👥 Cast detectado: " + (hasCast ? "✅ SÍ (Christian Bale encontrado)" : "❌ NO") + "\n" +
                       "🎭 Crew detectado: " + (hasCrew ? "✅ SÍ (Christopher Nolan encontrado)" : "❌ NO") + "\n\n" +
                       "📊 Tamaño respuesta: " + response.length() + " caracteres\n\n" +
                       "🔧 SIGUIENTE PASO: Ir a http://localhost:9090/pelicula/1 y verificar reparto";
            } else {
                return "❌ TMDB responde pero sin datos esperados\n" +
                       "📊 Respuesta recibida: " + (response != null ? response.substring(0, Math.min(500, response.length())) + "..." : "null");
            }
        } catch (Exception e) {
            return "❌ ERROR conectando con TMDB:\n" +
                   "Mensaje: " + e.getMessage() + "\n\n" +
                   "🔍 Posibles causas:\n" +
                   "- API key inválida\n" +
                   "- Problema de conectividad\n" +
                   "- Límite de peticiones alcanzado";
        }
    }
}
