package alicanteweb.pelisapp.dto;

import alicanteweb.pelisapp.entity.Pelicula;

import java.util.List;

public record PeliculaDetailDTO(Pelicula pelicula, Double valoracionMedia, List<ResenaDTO> resenas) {
}
