package alicanteweb.pelisapp.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TMDBClient {

    private final WebClient webClient;

    @Value("${app.tmdb.api-key}")
    private String apiKey;

    public TMDBClient(WebClient tmdbWebClient) {
        this.webClient = tmdbWebClient;
    }

    public JsonNode getPopular(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("page", page)
                        .queryParam("language", "es-ES")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getMovieDetails(long tmdbId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}")
                        .queryParam("language", "es-ES")
                        .queryParam("append_to_response", "credits")
                        .queryParam("api_key", apiKey)
                        .build(tmdbId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
