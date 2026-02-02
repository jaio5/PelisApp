package alicanteweb.pelisapp.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Getter
@Service
public class ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final Path storagePath;
    private final String serveBase;

    public ImageService(@Value("${app.images.storage-path:./data/images}") String storagePath,
                        @Value("${app.images.serve-base:/images}") String serveBase) throws IOException {
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        this.serveBase = serveBase.endsWith("/") ? serveBase.substring(0, serveBase.length()-1) : serveBase;
        Files.createDirectories(this.storagePath);
    }

    private String extractExtension(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) return "jpg";
        try {
            String path = new URL(imageUrl).getPath();
            int idx = path.lastIndexOf('.');
            if (idx > 0 && idx + 1 < path.length()) {
                String ext = path.substring(idx + 1);
                // sanitize
                ext = ext.replaceAll("[^a-zA-Z0-9]", "");
                if (!ext.isBlank()) return ext;
            }
        } catch (Exception ignored) {}
        return "jpg";
    }

    /**
     * Descarga una imagen desde una URL remota y la guarda localmente.
     * Ahora retorna únicamente el nombre de archivo (por ejemplo "uuid_123.jpg").
     * La plantilla y el ImageController deben servirlo en /images/{filename}.
     */
    public String downloadAndStore(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) return null;
        try (InputStream in = new URL(imageUrl).openStream()) {
            String ext = extractExtension(imageUrl);
            String filename = UUID.randomUUID() + "_" + Instant.now().getEpochSecond() + "." + ext;
            Path target = storagePath.resolve(filename);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Image saved to {} (public path: {}/{} )", target, serveBase, filename);
            // Retornamos sólo el filename para almacenarlo en la entidad y usar en la plantilla como /images/{file}
            return filename;
        } catch (IOException e) {
            log.warn("Failed to download image {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }

}
