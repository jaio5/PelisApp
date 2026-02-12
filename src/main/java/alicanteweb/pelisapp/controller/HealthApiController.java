package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ConnectionStatus;
import alicanteweb.pelisapp.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HealthApiController {
    private final SystemHealthService systemHealthService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> systemHealth() {
        try {
            log.info("🔍 Iniciando verificación completa del estado del sistema...");
            Map<String, ConnectionStatus> healthStatus = systemHealthService.checkAllConnections();
            Map<String, Object> response = new HashMap<>();
            healthStatus.forEach((service, status) -> {
                Map<String, Object> serviceInfo = new HashMap<>();
                serviceInfo.put("connected", status.isConnected());
                serviceInfo.put("message", status.getMessage());
                serviceInfo.put("responseTimeMs", status.getResponseTimeMs());
                serviceInfo.put("lastChecked", status.getLastChecked());
                if (status.getError() != null) {
                    serviceInfo.put("error", status.getError());
                }
                if (status.getDetails() != null) {
                    serviceInfo.put("details", status.getDetails());
                }
                response.put(service, serviceInfo);
            });
            log.info("✅ Verificación del sistema completada - {} servicios verificados", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error verificando estado del sistema: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error verificando estado del sistema: " + e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }
}
