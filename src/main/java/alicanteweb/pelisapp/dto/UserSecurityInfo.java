package alicanteweb.pelisapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * DTO para información de seguridad del usuario.
 * Contiene datos básicos del usuario y su estado de seguridad.
 */
@Data
@Builder
public class UserSecurityInfo {
    private String username;
    private String email;
    private boolean emailConfirmed;
    private Instant registeredAt;
    private List<String> roles;
}
