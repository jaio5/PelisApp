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

    @Query("select avg(v.puntuacion) from ValoracionResena v where v.resena.usuario.id = :usuarioId")
    Double avgPuntuacionRecibidaByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // Añadido: consulta para comprobar si un usuario ya valoró una reseña
    @Query("select v from ValoracionResena v where v.resena.id = :resenaId and v.valorador.id = :valoradorId")
    Optional<ValoracionResena> findByResenaIdAndValoradorId(@Param("resenaId") Integer resenaId, @Param("valoradorId") Integer valoradorId);
}
