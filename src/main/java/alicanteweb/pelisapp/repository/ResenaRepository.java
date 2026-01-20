package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena,Integer> {
    @Query("select avg(r.puntuacion) from Resena r where r.usuario.id = :usuarioId")
    Double avgPuntuacionByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // promedio de puntuación por película
    @Query("select avg(r.puntuacion) from Resena r where r.pelicula.id = :peliculaId")
    Double avgPuntuacionByPeliculaId(@Param("peliculaId") Integer peliculaId);

    // obtener reseñas de un usuario y fetch pelicula para evitar lazy
    @Query("select r from Resena r join fetch r.pelicula p where r.usuario.id = :usuarioId order by r.fecha desc")
    List<Resena> findByUsuarioIdFetchPelicula(@Param("usuarioId") Integer usuarioId);

    // obtener reseñas de una pelicula y fetch usuario
    @Query("select r from Resena r join fetch r.usuario u where r.pelicula.id = :peliculaId order by r.fecha desc")
    List<Resena> findByPeliculaIdFetchUsuario(@Param("peliculaId") Integer peliculaId);
}
