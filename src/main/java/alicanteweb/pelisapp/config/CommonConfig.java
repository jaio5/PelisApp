package alicanteweb.pelisapp.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CommonConfig {

    // Proveer WebClient.Builder si alguna clase lo inyecta directamente
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // ObjectMapper personalizado para evitar problemas con fechas y JavaTime
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    // RestTemplate para ModerationService y otras integraciones HTTP
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
