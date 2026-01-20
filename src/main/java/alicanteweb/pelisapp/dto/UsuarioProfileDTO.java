package alicanteweb.pelisapp.dto;

import java.time.Instant;
import java.util.Set;

public record UsuarioProfileDTO(Integer id, String username, Integer nivelCritico, Instant fechaRegistro, Set<String> etiquetas, Set<String> roles) {
}

