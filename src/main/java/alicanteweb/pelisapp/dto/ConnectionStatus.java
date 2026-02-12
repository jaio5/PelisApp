package alicanteweb.pelisapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * DTO para representar el estado de conexión de un servicio.
 * Extraído para evitar dependencias circulares entre servicios.
 */
@Data
@Builder
public class ConnectionStatus {
    private boolean connected;
    private String message;
    private long responseTimeMs;
    private Instant lastChecked;
    private String error;
    private Map<String, Object> details;
    private boolean warning;
}
