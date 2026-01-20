package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.RefreshToken;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioService usuarioService;
    private final long refreshTtlSeconds;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UsuarioService usuarioService,
                               @Value("${app.jwt.refresh-ttl-sec:604800}") long refreshTtlSeconds) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioService = usuarioService;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public RefreshToken createTokenForUser(Usuario u) {
        RefreshToken t = new RefreshToken();
        t.setToken(UUID.randomUUID().toString());
        t.setUsuario(u);
        t.setExpiryDate(Instant.now().plusSeconds(refreshTtlSeconds));
        return refreshTokenRepository.save(t);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUsuario(Usuario u) {
        refreshTokenRepository.deleteByUsuario(u);
    }
}

