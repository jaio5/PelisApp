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
     * @param imageUrl URL completa de la imagen (por ejemplo, <a href="https://image.tmdb.org/t/p/w500/abc.jpg">https://image.tmdb.org/t/p/w500/abc.jpg</a>)
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
     * Fuerza la redescarga de una imagen, incluso si ya existe localmente.
     * @param imageUrl URL completa de la imagen
     * @param subfolder subcarpeta donde guardar
     * @param filename nombre del archivo sin extensión
     * @return ruta relativa donde se guardó la imagen, o null si falló
     */
    public String forceDownloadAndStoreImage(String imageUrl, String subfolder, String filename) {
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

            // Descargar imagen (forzando sobreescritura)
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .header("User-Agent", "PelisApp/1.0")
                .build();

            HttpResponse<InputStream> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                Files.copy(response.body(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                String relativePath = serveBasePath + "/" + subfolder + "/" + fullFilename;
                log.info("Force downloaded image: {} -> {}", imageUrl, relativePath);
                return relativePath;
            } else {
                log.warn("Failed to force download image {}: HTTP {}", imageUrl, response.statusCode());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error force downloading image {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    /**
     * Elimina archivos duplicados en la subcarpeta indicada (por hash de contenido).
     * Devuelve el número de archivos eliminados.
     */
    public int deleteDuplicates(String subfolder) {
        int deleted = 0;
        try {
            Path storageDir = Paths.get(storageBasePath, subfolder);
            if (!Files.exists(storageDir) || !Files.isDirectory(storageDir)) {
                log.warn("No existe la carpeta de imágenes: {}", storageDir);
                return 0;
            }
            java.util.Map<String, Path> hashToFile = new java.util.HashMap<>();
            java.util.Set<Path> duplicates = new java.util.HashSet<>();
            try (java.util.stream.Stream<Path> stream = Files.list(storageDir)) {
                for (Path file : stream.filter(Files::isRegularFile).toList()) {
                    try (InputStream in = Files.newInputStream(file)) {
                        byte[] content = in.readAllBytes();
                        String hash = java.util.Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(content));
                        if (hashToFile.containsKey(hash)) {
                            duplicates.add(file);
                        } else {
                            hashToFile.put(hash, file);
                        }
                    } catch (Exception e) {
                        log.warn("Error leyendo archivo {}: {}", file, e.getMessage());
                    }
                }
            }
            for (Path dup : duplicates) {
                try {
                    Files.delete(dup);
                    deleted++;
                    log.info("Archivo duplicado eliminado: {}", dup);
                } catch (Exception e) {
                    log.warn("No se pudo eliminar {}: {}", dup, e.getMessage());
                }
            }
            log.info("Eliminados {} archivos duplicados en {}", deleted, storageDir);
        } catch (Exception e) {
            log.error("Error eliminando duplicados en {}: {}", subfolder, e.getMessage());
        }
        return deleted;
    }
}
