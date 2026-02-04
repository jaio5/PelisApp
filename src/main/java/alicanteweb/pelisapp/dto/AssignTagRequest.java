package alicanteweb.pelisapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO para solicitudes de asignación de etiqueta a usuario.
 */
@Data
public class AssignTagRequest {

    @NotNull(message = "El ID de la etiqueta es obligatorio")
    @Positive(message = "El ID de la etiqueta debe ser positivo")
    private Long tagId;
}
