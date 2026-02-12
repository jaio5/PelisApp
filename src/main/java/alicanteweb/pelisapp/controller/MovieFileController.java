package alicanteweb.pelisapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador para servir archivos de películas con soporte de streaming
 */
@Controller
@RequestMapping("/movies")
@Slf4j
public class MovieFileController {

    private final Path moviesStoragePath;

    public MovieFileController(@Value("${app.movies.storage-path:./data/movies}") String storagePath) {
        this.moviesStoragePath = Paths.get(storagePath).toAbsolutePath().normalize();
        log.info("🎬 Configurado directorio de películas: {}", this.moviesStoragePath);

        // Crear directorio si no existe
        try {
            Files.createDirectories(this.moviesStoragePath);
            log.info("✅ Directorio de películas listo: {}", this.moviesStoragePath);
        } catch (IOException e) {
            log.error("❌ Error creando directorio de películas: {}", e.getMessage());
        }
    }

    @GetMapping("/download/{movieId}/{fileName:.+}")
    public ResponseEntity<Resource> downloadMovie(
            @PathVariable Long movieId,
            @PathVariable String fileName,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            log.info("🎬 Solicitada descarga: Movie ID={}, File={}, Range={}", movieId, fileName, rangeHeader);

            // Construir ruta del archivo
            Path moviePath = moviesStoragePath.resolve(movieId.toString()).resolve(fileName).normalize();

            // Verificar que el archivo esté dentro del directorio permitido
            if (!moviePath.startsWith(moviesStoragePath)) {
                log.warn("⚠️ Intento de acceso fuera del directorio permitido: {}", moviePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que el archivo existe
            if (!Files.exists(moviePath)) {
                log.warn("❌ Archivo no encontrado: {}", moviePath);
                return ResponseEntity.notFound().build();
            }

            // Crear recurso
            Resource resource = new UrlResource(moviePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.error("❌ Archivo no legible: {}", moviePath);
                return ResponseEntity.notFound().build();
            }

            // Determinar tipo de contenido
            String contentType = determineContentType(fileName);
            long fileSize = Files.size(moviePath);

            // Si es una solicitud con Range (streaming), manejar parcial
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(resource, rangeHeader, fileSize, contentType, fileName);
            }

            // Respuesta completa
            log.info("✅ Sirviendo archivo completo: {} ({} bytes)", fileName, fileSize);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(fileSize)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Error sirviendo archivo de película: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stream/{movieId}/{fileName:.+}")
    public ResponseEntity<Resource> streamMovie(
            @PathVariable Long movieId,
            @PathVariable String fileName,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            log.info("🎬 Solicitado streaming: Movie ID={}, File={}, Range={}", movieId, fileName, rangeHeader);

            // Construir ruta del archivo
            Path moviePath = moviesStoragePath.resolve(movieId.toString()).resolve(fileName).normalize();

            // Verificaciones de seguridad
            if (!moviePath.startsWith(moviesStoragePath)) {
                log.warn("⚠️ Intento de acceso fuera del directorio permitido: {}", moviePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!Files.exists(moviePath)) {
                log.warn("❌ Archivo no encontrado: {}", moviePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(moviePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.error("❌ Archivo no legible: {}", moviePath);
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(fileName);
            long fileSize = Files.size(moviePath);

            // Para streaming, siempre manejar rangos
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(resource, rangeHeader, fileSize, contentType, fileName);
            }

            // Sin range header, devolver respuesta básica para streaming
            log.info("✅ Iniciando streaming: {} ({} bytes)", fileName, fileSize);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Error en streaming de película: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> handleRangeRequest(Resource resource, String rangeHeader,
                                                       long fileSize, String contentType, String fileName) {
        try {
            // Parsear header Range: bytes=start-end
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty() ?
                      Long.parseLong(ranges[1]) : fileSize - 1;

            // Validar rangos
            if (start >= fileSize || end >= fileSize || start > end) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            long contentLength = end - start + 1;

            log.info("📊 Sirviendo rango: bytes {}-{}/{} ({} bytes)", start, end, fileSize, contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(contentLength)
                    .header(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, fileSize))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Error procesando solicitud de rango: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private String determineContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFileName.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (lowerFileName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lowerFileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFileName.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerFileName.endsWith(".flv")) {
            return "video/x-flv";
        } else {
            return "application/octet-stream";
        }
    }
}
