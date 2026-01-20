package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Usuario;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByUsername(String username);

    @Query("select count(usr) from Usuario usr join usr.seguidos s where s.id = :usuarioId")
    long countSeguidoresByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
