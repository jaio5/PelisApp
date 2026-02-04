package alicanteweb.pelisapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO para solicitudes de asignación de rol a usuario.
 */
@Data
public class AssignRoleRequest {

    @NotNull(message = "El ID del rol es obligatorio")
    @Positive(message = "El ID del rol debe ser positivo")
    private Long roleId;
}
