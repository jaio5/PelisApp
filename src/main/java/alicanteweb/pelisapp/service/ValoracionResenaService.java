package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Resena;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.entity.ValoracionResena;
import alicanteweb.pelisapp.repository.ResenaRepository;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.repository.ValoracionResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ValoracionResenaService {

    private final ValoracionResenaRepository valoracionResenaRepository;
    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public ValoracionResenaService(ValoracionResenaRepository valoracionResenaRepository,
                                    ResenaRepository resenaRepository,
                                    UsuarioRepository usuarioRepository,
                                    UsuarioService usuarioService) {
        this.valoracionResenaRepository = valoracionResenaRepository;
        this.resenaRepository = resenaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public ValoracionResena crearOActualizarValoracion(Integer resenaId, Integer valoradorId, Integer puntuacion, String comentario) {
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5 estrellas");
        }

        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));
        Usuario valorador = usuarioRepository.findById(valoradorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario valorador no encontrado"));

        if (resena.getUsuario().getId().equals(valorador.getId())) {
            throw new IllegalArgumentException("No se puede valorar la propia reseña");
        }

        Optional<ValoracionResena> existing = valoracionResenaRepository.findByResenaIdAndValoradorId(resenaId, valoradorId);
        ValoracionResena v;
        if (existing.isPresent()) {
            // actualizar voto existente
            v = existing.get();
            v.setPuntuacion(puntuacion);
            v.setComentario(comentario);
        } else {
            v = new ValoracionResena();
            v.setResena(resena);
            v.setValorador(valorador);
            v.setPuntuacion(puntuacion);
            v.setComentario(comentario);
        }

        ValoracionResena saved = valoracionResenaRepository.save(v);

        // Recalcula el nivel crítico del autor de la reseña
        usuarioService.recalcularNivelCritico(resena.getUsuario().getId());

        return saved;
    }

    @Transactional
    public void eliminarValoracion(Integer valoracionId) {
        ValoracionResena v = valoracionResenaRepository.findById(valoracionId)
                .orElseThrow(() -> new IllegalArgumentException("Valoración no encontrada"));
        Integer autorId = v.getResena().getUsuario().getId();
        valoracionResenaRepository.delete(v);
        usuarioService.recalcularNivelCritico(autorId);
    }
}
