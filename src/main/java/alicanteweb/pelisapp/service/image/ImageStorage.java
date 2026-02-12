package alicanteweb.pelisapp.service.image;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Componente especializado para almacenamiento local de imágenes.
 * Aplica principio de responsabilidad única (SRP).
 */
@Component
@Slf4j
@Getter
public class ImageStorage {

    private final Path storagePath;
    private final String serveBase;

    public ImageStorage(@Value("${app.images.storage-path:./data/images}") String storagePath,
                        @Value("${app.images.serve-base:/images}") String serveBase) throws IOException {
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        this.serveBase = serveBase.endsWith("/") ? serveBase.substring(0, serveBase.length()-1) : serveBase;

        // Crear directorios si no existen
        Files.createDirectories(this.storagePath);

        log.info("📂 ImageStorage configurado:");
        log.info("  📍 Ruta de almacenamiento: {}", this.storagePath);
        log.info("  🌐 Base URL para servir: {}", this.serveBase);
    }

    /**
     * Guarda un InputStream como archivo en el almacenamiento local.
     *
     * @param inputStream stream de datos de la imagen
     * @param filename nombre del archivo a guardar
     * @param subfolder subcarpeta donde guardar (ej: "posters", "profiles")
     * @return ruta relativa del archivo guardado
     * @throws IOException si hay error guardando el archivo
     */
    public String saveImage(InputStream inputStream, String filename, String subfolder) throws IOException {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nombre de archivo no puede estar vacío");
        }

        // Crear subcarpeta si no existe
        Path subfolderPath = storagePath.resolve(subfolder);
        Files.createDirectories(subfolderPath);

        // Ruta completa del archivo

        // Sanitizar nombre de archivo
        String sanitizedFilename = sanitizeFilename(filename);
        Path sanitizedPath = subfolderPath.resolve(sanitizedFilename);

        log.debug("💾 Guardando imagen: {}/{}", subfolder, sanitizedFilename);

        try {
            Files.copy(inputStream, sanitizedPath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = subfolder + "/" + sanitizedFilename;
            log.debug("✅ Imagen guardada: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("❌ Error guardando imagen {}/{}: {}", subfolder, sanitizedFilename, e.getMessage());
            throw new IOException("Error guardando imagen: " + e.getMessage(), e);
        }
    }

    // Métodos públicos no utilizados eliminados para evitar warnings: buildPublicUrl, fileExists, deleteImage

    /**
     * Sanitiza el nombre de archivo eliminando caracteres peligrosos.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed";
        }

        // Reemplazar caracteres peligrosos
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_"); // Evitar múltiples guiones bajos consecutivos
    }
}
