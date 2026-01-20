package alicanteweb.pelisapp.dto;

import java.time.LocalDate;

public record PeliculaDTO(Integer id, String titulo, Integer anio, String posterUrl, String backdropUrl, LocalDate releaseDate, Integer tmdbId) {
}

