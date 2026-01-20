package alicanteweb.pelisapp.tmdb;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@NullMarked
@Component
public class TmdbClient {

    private final WebClient webClient;
    private final String apiKey;

    public TmdbClient(WebClient tmdbWebClient,
                      @Value("${app.tmdb.api-key}") String apiKey) {
        this.webClient = tmdbWebClient;
        this.apiKey = apiKey;
    }

    public Mono<TmdbSearchResult> searchMovies(String query, int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResult.class);
    }

    public Mono<TmdbMovieDetail> getMovieDetail(Integer tmdbId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/{id}")
                        .queryParam("api_key", apiKey)
                        .build(tmdbId))
                .retrieve()
                .bodyToMono(TmdbMovieDetail.class);
    }

    public Mono<TmdbSearchResult> getPopular(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResult.class);
    }

    public Mono<TmdbCredits> getCredits(Integer tmdbId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/{id}/credits")
                        .queryParam("api_key", apiKey)
                        .build(tmdbId))
                .retrieve()
                .bodyToMono(TmdbCredits.class);
    }
}
