package alicanteweb.pelisapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class TmdbImageDownloader {

    private final WebClient webClient;
    private final String storagePath;
    private final String serveBaseUrl;

    public TmdbImageDownloader(WebClient.Builder webClientBuilder,
                               @Value("${app.tmdb.image-base-url}") String imageBaseUrl,
                               @Value("${app.images.storage-path:./data/images}") String storagePath,
                               @Value("${app.images.serve-base:/images}") String serveBaseUrl) {
        // creamos un webClient con base url de TMDB images
        this.webClient = webClientBuilder.baseUrl(imageBaseUrl).build();
        this.storagePath = storagePath;
        this.serveBaseUrl = serveBaseUrl;
    }

    /**
     * Descarga la imagen desde TMDb (ruta relativa como /t/p/w500/xxx.jpg) y la guarda en
     * {storagePath}/{type}/{id}.{ext}. Devuelve la URL pública para acceder (serveBaseUrl/...)
     */
    // Versión sincrónica (compatibilidad)
    public String downloadAndStore(String relativePath, String type, Integer tmdbId) {
        return downloadAndStoreReactive(relativePath, type, tmdbId).block();
    }

    // Versión reactiva: devuelve Mono<String> con la URL pública donde se sirve la imagen
    public Mono<String> downloadAndStoreReactive(String relativePath, String type, Integer tmdbId) {
        if (relativePath == null || relativePath.isBlank()) return Mono.empty();
        String ext = extractExtension(relativePath);
        String filename = String.format("%d.%s", tmdbId, ext != null ? ext : "jpg");
        Path dir = Path.of(storagePath, type);
        Path file = dir.resolve(filename);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            return Mono.error(new RuntimeException("No se pudo crear directorio de imágenes: " + dir, e));
        }

        if (Files.exists(file)) {
            return Mono.just(serveBaseUrl + "/" + type + "/" + filename);
        }

        return webClient.get()
                .uri(relativePath)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(bytes -> Mono.fromCallable(() -> {
                    try {
                        Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    } catch (IOException e) {
                        throw new RuntimeException("Error escribiendo fichero de imagen: " + file, e);
                    }
                    return serveBaseUrl + "/" + type + "/" + filename;
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    private String extractExtension(String path) {
        String clean = StringUtils.getFilename(path);
        if (clean == null) return null;
        int idx = clean.lastIndexOf('.');
        if (idx == -1) return null;
        return clean.substring(idx + 1);
    }
}
