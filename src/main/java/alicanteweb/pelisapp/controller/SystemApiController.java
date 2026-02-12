package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ConnectionStatus;
import alicanteweb.pelisapp.service.SystemHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemApiController {
    private final SystemHealthService systemHealthService;

    public SystemApiController(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    // Health check general
    @GetMapping("/health")
    public ResponseEntity<Map<String, ConnectionStatus>> getSystemHealth() {
        try {
            Map<String, ConnectionStatus> healthStatus = systemHealthService.checkAllConnections();
            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", ConnectionStatus.builder()
                    .connected(false)
                    .message("Error verificando estado del sistema: " + e.getMessage())
                    .responseTimeMs(0L)
                    .lastChecked(java.time.Instant.now())
                    .build()
            ));
        }
    }

    // Health check de un servicio concreto
    @GetMapping("/health/{service}")
    public ResponseEntity<ConnectionStatus> getServiceHealth(@PathVariable String service) {
        try {
            ConnectionStatus status;
            switch (service.toLowerCase()) {
                case "database":
                    status = systemHealthService.isDatabaseHealthy()
                        ? ConnectionStatus.builder().connected(true).message("Base de datos conectada").build()
                        : ConnectionStatus.builder().connected(false).message("Error en base de datos").build();
                    break;
                case "tmdb":
                    status = systemHealthService.isTmdbHealthy()
                        ? ConnectionStatus.builder().connected(true).message("TMDB API conectada").build()
                        : ConnectionStatus.builder().connected(false).message("Error en TMDB API").build();
                    break;
                case "ollama":
                    status = systemHealthService.isOllamaHealthy()
                        ? ConnectionStatus.builder().connected(true).message("Ollama conectado").build()
                        : ConnectionStatus.builder().connected(false).message("Error en Ollama").build();
                    break;
                case "email":
                    status = ConnectionStatus.builder().connected(true).message("Configuración de email").build();
                    break;
                case "server":
                    status = ConnectionStatus.builder().connected(true).message("Servidor funcionando").build();
                    break;
                default:
                    return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            ConnectionStatus errorStatus = ConnectionStatus.builder()
                .connected(false)
                .message("Error verificando servicio: " + e.getMessage())
                .responseTimeMs(0L)
                .lastChecked(java.time.Instant.now())
                .build();
            return ResponseEntity.status(500).body(errorStatus);
        }
    }
}
