package alicanteweb.pelisapp.dto;

import java.util.List;

public record UsuarioDetailDTO(UsuarioProfileDTO profile, List<ResenaDTO> resenas) {
}
