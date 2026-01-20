package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaRepository extends JpaRepository<Resena,Integer> {
    @Query("select avg(r.puntuacion) from Resena r where r.usuario.id = :usuarioId")
    Double avgPuntuacionByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // Añadido: promedio de puntuación por película
    @Query("select avg(r.puntuacion) from Resena r where r.pelicula.id = :peliculaId")
    Double avgPuntuacionByPeliculaId(@Param("peliculaId") Integer peliculaId);
}
