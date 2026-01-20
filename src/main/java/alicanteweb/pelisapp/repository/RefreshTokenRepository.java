package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.RefreshToken;
import alicanteweb.pelisapp.entity.Usuario;
import jakarta.validation.constraints.Null;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}

