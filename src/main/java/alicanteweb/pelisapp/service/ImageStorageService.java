package alicanteweb.pelisapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URI;

/**
 * Servicio para almacenar imágenes localmente desde TMDB.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    @Value("${app.images.storage-path:./data/images}")
    private String storageBasePath;

    @Value("${app.images.serve-base:/images}")
    private String serveBasePath;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Descarga una imagen desde una URL y la guarda localmente.
     * @param imageUrl URL completa de la imagen (ej: https://image.tmdb.org/t/p/w500/abc.jpg)
     * @param subfolder subcarpeta donde guardar (ej: "posters", "backdrops")
     * @param filename nombre del archivo sin extensión
     * @return ruta relativa donde se guardó la imagen, o null si falló
     */
    public String downloadAndStoreImage(String imageUrl, String subfolder, String filename) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            // Crear directorios si no existen
            Path storageDir = Paths.get(storageBasePath, subfolder);
            Files.createDirectories(storageDir);

            // Determinar extensión de la imagen
            String extension = getImageExtension(imageUrl);
            String fullFilename = filename + extension;

            Path targetPath = storageDir.resolve(fullFilename);

            // Si ya existe, no descargar de nuevo
            if (Files.exists(targetPath)) {
                String relativePath = serveBasePath + "/" + subfolder + "/" + fullFilename;
                log.debug("Image already exists: {}", relativePath);
                return relativePath;
            }

            // Descargar imagen
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .header("User-Agent", "PelisApp/1.0")
                .build();

            HttpResponse<InputStream> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                Files.copy(response.body(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                String relativePath = serveBasePath + "/" + subfolder + "/" + fullFilename;
                log.info("Downloaded image: {} -> {}", imageUrl, relativePath);
                return relativePath;
            } else {
                log.warn("Failed to download image {}: HTTP {}", imageUrl, response.statusCode());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error downloading image {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    private String getImageExtension(String url) {
        if (url.contains(".jpg") || url.contains(".jpeg")) return ".jpg";
        if (url.contains(".png")) return ".png";
        if (url.contains(".webp")) return ".webp";
        return ".jpg"; // default
    }

    /**
     * Descarga poster de una película desde TMDB.
     */
    public String downloadMoviePoster(String tmdbPosterPath, Long movieId) {
        if (tmdbPosterPath == null || movieId == null) return null;

        String fullUrl = "https://image.tmdb.org/t/p/w500" +
            (tmdbPosterPath.startsWith("/") ? tmdbPosterPath : "/" + tmdbPosterPath);

        return downloadAndStoreImage(fullUrl, "posters", "movie_" + movieId);
    }

    /**
     * Descarga backdrop de una película desde TMDB.
     */
    public String downloadMovieBackdrop(String tmdbBackdropPath, Long movieId) {
        if (tmdbBackdropPath == null || movieId == null) return null;

        String fullUrl = "https://image.tmdb.org/t/p/w780" +
            (tmdbBackdropPath.startsWith("/") ? tmdbBackdropPath : "/" + tmdbBackdropPath);

        return downloadAndStoreImage(fullUrl, "backdrops", "movie_" + movieId);
    }

    /**
     * Elimina una imagen almacenada localmente.
     */
    public boolean deleteStoredImage(String relativePath) {
        if (relativePath == null || !relativePath.startsWith(serveBasePath)) {
            return false;
        }

        try {
            // Convertir ruta relativa a absoluta
            String pathWithoutBase = relativePath.substring(serveBasePath.length());
            if (pathWithoutBase.startsWith("/")) pathWithoutBase = pathWithoutBase.substring(1);

            Path targetPath = Paths.get(storageBasePath, pathWithoutBase);

            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                log.info("Deleted image: {}", relativePath);
                return true;
            }
        } catch (IOException e) {
            log.error("Error deleting image {}: {}", relativePath, e.getMessage());
        }

        return false;
    }
}
