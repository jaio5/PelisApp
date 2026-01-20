package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.tmdb.TmdbClient;
import alicanteweb.pelisapp.tmdb.TmdbMovieShort;
import alicanteweb.pelisapp.tmdb.TmdbSearchResult;
import org.springframework.stereotype.Service;

@Service
public class TmdbBatchImportService {

    private final TmdbClient tmdbClient;
    private final TmdbService tmdbService;

    public TmdbBatchImportService(TmdbClient tmdbClient, TmdbService tmdbService) {
        this.tmdbClient = tmdbClient;
        this.tmdbService = tmdbService;
    }

    /**
     * Importa las películas "popular" de TMDb por páginas. Cada página suele contener 20 items.
     * Este método es bloqueante y pensado para ejecución manual/administrativa.
     * @param pages número de páginas a procesar
     * @return número aproximado de películas importadas
     */
    public int importPopularPages(int pages) {
        int imported = 0;
        for (int page = 1; page <= pages; page++) {
            TmdbSearchResult res = tmdbClient.getPopular(page).block();
            if (res == null || res.getResults() == null) break;
            for (TmdbMovieShort m : res.getResults()) {
                try {
                    // llamar al servicio de importación (ya guarda películas, géneros y créditos)
                    tmdbService.importMovie(m.getId()).block();
                    imported++;
                } catch (Exception e) {
                    // registrar y continuar
                    System.err.println("Error importing movie " + m.getId() + ": " + e.getMessage());
                }
            }
        }
        return imported;
    }
}

