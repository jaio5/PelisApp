package alicanteweb.pelisapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TmdbConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean(name = "tmdbWebClient")
    public WebClient tmdbWebClient(WebClient.Builder builder, @Value("${app.tmdb.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}

