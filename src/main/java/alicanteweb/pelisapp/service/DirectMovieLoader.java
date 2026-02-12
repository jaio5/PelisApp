package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.constants.AppConstants;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Servicio para carga directa de películas desde TMDB
 * Responsabilidades:
 * - Obtener listas de películas populares de TMDB
 * - Delegar la carga detallada a TMDBMovieLoaderService
 * - Gestionar el flujo de paginación
 * Aplica principios SOLID:
 * - SRP: Una sola responsabilidad (coordinación de carga)
 * - DIP: Depende de abstracciones (repositorio y servicio)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMovieLoader {

    // URL template construida usando constantes
    private static final String TMDB_POPULAR_URL_TEMPLATE =
        AppConstants.TMDB_DEFAULT_BASE_URL + AppConstants.TMDB_POPULAR_ENDPOINT +
        "?language=" + AppConstants.TMDB_LANGUAGE_ES + "&page=%d";

    private final MovieRepository movieRepository;
    private final TMDBMovieLoaderService tmdbMovieLoaderService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.tmdb.bearer-token}")
    private String bearerToken;

    /**
     * Carga películas directamente desde TMDB con todos los detalles
     *
     * @param pages número de páginas a cargar (cada página contiene ~20 películas)
     * @return número de películas nuevas cargadas
     */
    @Transactional
    public int loadMoviesDirectly(int pages) {
        log.info("{} Iniciando carga directa de {} páginas desde TMDB",
                 AppConstants.LOG_FIRE_EMOJI, pages);

        validateInput(pages);

        int totalLoaded = 0;
        HttpEntity<String> httpEntity = createHttpEntity();

        for (int page = 1; page <= pages; page++) {
            try {
                int loadedInPage = processPage(page, httpEntity);
                totalLoaded += loadedInPage;

                delayBetweenPages();

            } catch (Exception e) {
                handlePageError(page, e);
            }
        }

        logCompletionSummary(totalLoaded);
        return totalLoaded;
    }

    /**
     * Obtiene el número total de películas en la base de datos
     */
    public long getMovieCount() {
        return movieRepository.count();
    }

    // Métodos privados que implementan el patrón Template Method

    private void validateInput(int pages) {
        if (pages <= 0) {
            throw new IllegalArgumentException("El número de páginas debe ser positivo");
        }
        if (pages > AppConstants.MAX_PAGES_LIMIT) {
            log.warn("{} Cargando {} páginas, esto puede tardar mucho tiempo",
                     AppConstants.LOG_WARNING_EMOJI, pages);
        }
    }

    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AppConstants.TMDB_AUTHORIZATION_HEADER,
                   AppConstants.TMDB_BEARER_PREFIX + bearerToken);
        return new HttpEntity<>(headers);
    }

    private int processPage(int page, HttpEntity<String> httpEntity) {
        log.info("{} Procesando página {} de TMDB", AppConstants.LOG_INFO_EMOJI, page);

        String url = String.format(TMDB_POPULAR_URL_TEMPLATE, page);
        ResponseEntity<Map<String, Object>> response = makeApiCall(url, httpEntity);

        return processApiResponse(response, page);
    }

    private ResponseEntity<Map<String, Object>> makeApiCall(String url, HttpEntity<String> httpEntity) {
        try {
            return restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
        } catch (RestClientException e) {
            log.error("{} Error llamando a TMDB API: {}", AppConstants.LOG_ERROR_EMOJI, e.getMessage());
            throw new RuntimeException("Error conectando con TMDB", e);
        }
    }

    private int processApiResponse(ResponseEntity<Map<String, Object>> response, int page) {
        Map<String, Object> data = response.getBody();

        if (data == null || !data.containsKey(AppConstants.TMDB_RESULTS_KEY)) {
            log.error("{} Respuesta inválida para página {}", AppConstants.LOG_ERROR_EMOJI, page);
            return 0;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get(AppConstants.TMDB_RESULTS_KEY);

        log.info("{} Página {} obtenida - {} películas encontradas",
                 AppConstants.LOG_SUCCESS_EMOJI, page, results.size());

        return processMovieResults(results);
    }

    private int processMovieResults(List<Map<String, Object>> results) {
        int loadedCount = 0;

        for (Map<String, Object> movieData : results) {
            try {
                if (processMovieData(movieData)) {
                    loadedCount++;
                }
            } catch (Exception e) {
                log.warn("{} Error procesando película individual: {}",
                         AppConstants.LOG_WARNING_EMOJI, e.getMessage());
            }
        }

        return loadedCount;
    }

    private boolean processMovieData(Map<String, Object> movieData) {
        Long tmdbId = extractTmdbId(movieData);

        if (tmdbId == null) {
            return false;
        }

        if (movieAlreadyExists(tmdbId)) {
            return false;
        }

        return loadMovieDetails(tmdbId);
    }

    private Long extractTmdbId(Map<String, Object> movieData) {
        Object idObj = movieData.get(AppConstants.TMDB_ID_KEY);
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        log.warn("{} ID de película inválido: {}", AppConstants.LOG_WARNING_EMOJI, idObj);
        return null;
    }

    private boolean movieAlreadyExists(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId).isPresent();
    }

    private boolean loadMovieDetails(Long tmdbId) {
        Movie movie = tmdbMovieLoaderService.loadMovieByTmdbId(tmdbId);

        if (movie != null) {
            log.debug("{} Cargada: {} (ID: {})", AppConstants.LOG_SUCCESS_EMOJI, movie.getTitle(), tmdbId);
            return true;
        }

        log.warn("{} No se pudo cargar película con ID: {}", AppConstants.LOG_WARNING_EMOJI, tmdbId);
        return false;
    }

    private void delayBetweenPages() {
        try {
            Thread.sleep(AppConstants.DELAY_BETWEEN_PAGES_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Carga interrumpida", e);
        }
    }

    private void handlePageError(int page, Exception e) {
        log.error("{} Error procesando página {}: {}",
                  AppConstants.LOG_ERROR_EMOJI, page, e.getMessage());
        // Continuamos con la siguiente página en lugar de fallar completamente
    }

    private void logCompletionSummary(int totalLoaded) {
        log.info("{} Carga directa completada - {} películas cargadas con reparto y directores",
                 AppConstants.LOG_FIRE_EMOJI, totalLoaded);
    }
}
