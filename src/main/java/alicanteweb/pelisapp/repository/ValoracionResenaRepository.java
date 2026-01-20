package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.ValoracionResena;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface ValoracionResenaRepository extends JpaRepository<ValoracionResena, Integer> {

    @Query("select coalesce(avg(v.puntuacion), 0) from ValoracionResena v where v.resena.usuario.id = :usuarioId")
    double avgPuntuacionRecibidaByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // Añadido: consulta para comprobar si un usuario ya valoró una reseña
    @Query("select v from ValoracionResena v where v.resena.id = :resenaId and v.valorador.id = :valoradorId")
    Optional<ValoracionResena> findByResenaIdAndValoradorId(@Param("resenaId") Integer resenaId, @Param("valoradorId") Integer valoradorId);

    @Query("select count(v) from ValoracionResena v where v.resena.usuario.id = :usuarioId and v.puntuacion >= 4")
    long countPositiveByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
