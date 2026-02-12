package alicanteweb.pelisapp.service.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

/**
 * Componente especializado para descargar imágenes desde URLs remotas.
 * Aplica principio de responsabilidad única (SRP).
 */
@Component
@Slf4j
public class ImageDownloader {

    /**
     * Descarga una imagen desde una URL remota.
     *
     * @param imageUrl URL de la imagen a descargar
     * @return InputStream de la imagen descargada
     * @throws IOException si hay error en la descarga
     */
    public InputStream downloadImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("URL de imagen no puede estar vacía");
        }

        log.debug("📥 Descargando imagen desde: {}", imageUrl);

        try {
            URL url = new URL(imageUrl);
            InputStream stream = url.openStream();

            log.debug("✅ Imagen descargada exitosamente: {}", imageUrl);
            return stream;

        } catch (IOException e) {
            log.error("❌ Error descargando imagen desde {}: {}", imageUrl, e.getMessage());
            throw new IOException("Error descargando imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Extrae la extensión del archivo desde una URL.
     *
     * @param imageUrl URL de la imagen
     * @return extensión del archivo (por defecto "jpg")
     */
    public String extractExtension(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "jpg";
        }

        try {
            String path = new URL(imageUrl).getPath();
            int lastDotIndex = path.lastIndexOf('.');

            if (lastDotIndex > 0 && lastDotIndex + 1 < path.length()) {
                String extension = path.substring(lastDotIndex + 1);
                // Sanitizar extensión (solo caracteres alfanuméricos)
                extension = extension.replaceAll("[^a-zA-Z0-9]", "");

                if (!extension.isEmpty() && extension.length() <= 4) {
                    return extension.toLowerCase();
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer extensión de {}, usando 'jpg' por defecto", imageUrl);
        }

        return "jpg";
    }

    /**
     * Genera un nombre único para el archivo basado en el prefijo y la URL.
     *
     * @param prefix prefijo para el nombre del archivo (ej: "poster", "profile")
     * @param imageUrl URL original de la imagen
     * @return nombre único para el archivo
     */
    public String generateUniqueFilename(String prefix, String imageUrl) {
        String extension = extractExtension(imageUrl);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", prefix, uniqueId, extension);
    }
}
