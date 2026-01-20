package alicanteweb.pelisapp.mapper;

import alicanteweb.pelisapp.dto.ResenaDTO;
import alicanteweb.pelisapp.entity.Resena;

public class ResenaMapper {
    public static ResenaDTO toDto(Resena r) {
        if (r == null) return null;
        Integer usuarioId = r.getUsuario() != null ? r.getUsuario().getId() : null;
        String usuarioUsername = r.getUsuario() != null ? r.getUsuario().getUsername() : null;
        Integer peliculaId = r.getPelicula() != null ? r.getPelicula().getId() : null;
        String peliculaTitulo = r.getPelicula() != null ? r.getPelicula().getTitulo() : null;
        return new ResenaDTO(r.getId(), r.getPuntuacion(), r.getComentario(), r.getFecha(), usuarioId, usuarioUsername, peliculaId, peliculaTitulo);
    }
}

