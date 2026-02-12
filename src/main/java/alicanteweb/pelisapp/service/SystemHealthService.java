package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.ConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio principal para verificar el estado de las conexiones del sistema.
 * Refactorizado usando ConnectionHealthService para cumplir SRP.
 * Actúa como fachada para el servicio de verificación de conexiones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemHealthService {

    private final ConnectionHealthService connectionHealthService;

    /**
     * Verifica el estado de todas las conexiones del sistema.
     *
     * @return Map con el estado de cada conexión del sistema
     */
    public Map<String, ConnectionStatus> checkAllConnections() {
        log.info("🔍 Iniciando verificación completa de conexiones del sistema");

        Map<String, ConnectionStatus> statuses = new HashMap<>();

        // Verificar base de datos
        statuses.put("database", connectionHealthService.checkDatabaseConnection());

        // Verificar TMDB API
        statuses.put("tmdb", connectionHealthService.checkTmdbConnection());

        // Verificar Ollama (IA de moderación)
        statuses.put("ollama", connectionHealthService.checkOllamaConnection());

        // Verificar configuración de email
        statuses.put("email", connectionHealthService.checkEmailConfiguration());

        // Estado general del servidor
        statuses.put("server", connectionHealthService.checkServerHealth());

        logConnectionSummary(statuses);
        return statuses;
    }

    /**
     * Registra un resumen del estado de las conexiones.
     */
    private void logConnectionSummary(Map<String, ConnectionStatus> statuses) {
        long connectedCount = statuses.values().stream()
                .mapToLong(status -> status.isConnected() ? 1 : 0)
                .sum();

        long totalCount = statuses.size();

        if (connectedCount == totalCount) {
            log.info("✅ Verificación completada: {}/{} servicios conectados correctamente",
                    connectedCount, totalCount);
        } else {
            log.warn("⚠️ Verificación completada: {}/{} servicios conectados - {} servicios con problemas",
                    connectedCount, totalCount, totalCount - connectedCount);
        }
    }

    /**
     * Verifica solo la conexión a la base de datos.
     * Método de conveniencia para verificaciones rápidas.
     */
    public boolean isDatabaseHealthy() {
        ConnectionStatus status = connectionHealthService.checkDatabaseConnection();
        return status.isConnected();
    }

    /**
     * Verifica solo la conexión a TMDB API.
     * Método de conveniencia para verificaciones rápidas.
     */
    public boolean isTmdbHealthy() {
        ConnectionStatus status = connectionHealthService.checkTmdbConnection();
        return status.isConnected();
    }

    /**
     * Verifica solo la conexión a Ollama.
     * Método de conveniencia para verificaciones rápidas.
     */
    public boolean isOllamaHealthy() {
        ConnectionStatus status = connectionHealthService.checkOllamaConnection();
        return status.isConnected();
    }
}
