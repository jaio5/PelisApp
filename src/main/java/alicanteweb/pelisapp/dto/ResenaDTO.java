package alicanteweb.pelisapp.dto;

import java.time.Instant;

public record ResenaDTO(Integer id, Integer puntuacion, String comentario, Instant fecha, Integer usuarioId, String usuarioUsername, Integer peliculaId, String peliculaTitulo) {
}

