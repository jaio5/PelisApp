package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.repository.ValoracionResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ValoracionResenaRepository valoracionResenaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ValoracionResenaRepository valoracionResenaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.valoracionResenaRepository = valoracionResenaRepository;
    }

    @Transactional
    public void recalcularNivelCritico(Integer usuarioId) {
        Double avg = valoracionResenaRepository.avgPuntuacionRecibidaByUsuarioId(usuarioId);
        int nuevoNivel;
        if (avg == null) {
            nuevoNivel = 1; // sin valoraciones recibidas
        } else {
            // avg está entre 1..5; mapear directamente a 1..5
            nuevoNivel = Math.max(1, Math.min(5, (int) Math.round(avg)));
        }
        Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
        if (u != null) {
            if (!Integer.valueOf(nuevoNivel).equals(u.getNivelCritico())) {
                u.setNivelCritico(nuevoNivel);
            }

            // Asignar roles según average recibido
            Set<Role> newRoles = new HashSet<>();
            newRoles.add(Role.ROLE_USER);
            if (avg != null) {
                if (avg >= 4.5) {
                    newRoles.add(Role.ROLE_EXPERTO);
                } else if (avg >= 3.5) {
                    newRoles.add(Role.ROLE_CRITICO);
                }
            }
            u.setRoles(newRoles);

            // Etiquetas simples según niveles/umbral (ejemplo)
            Set<String> etiquetas = new HashSet<>(u.getEtiquetas());
            // sustituimos if/else duplicados por helper updateEtiqueta
            updateEtiqueta(etiquetas, "TopCritic", avg != null && avg >= 4.5);
            updateEtiqueta(etiquetas, "ConfianzaAlta", u.getNivelCritico() != null && u.getNivelCritico() >= 4);

            u.setEtiquetas(etiquetas);

            usuarioRepository.save(u);
        }
    }

    @Transactional
    public void followUser(Integer userId, Integer targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo");
        }
        Usuario user = usuarioRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Usuario target = usuarioRepository.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException("Usuario objetivo no encontrado"));
        user.getSeguidos().add(target);
        usuarioRepository.save(user);
    }

    @Transactional
    public void unfollowUser(Integer userId, Integer targetUserId) {
        Usuario user = usuarioRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Usuario target = usuarioRepository.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException("Usuario objetivo no encontrado"));
        user.getSeguidos().remove(target);
        usuarioRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getProfile(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId);
    }

    // Helper para añadir o quitar una etiqueta en el conjunto según el flag 'present'
    private void updateEtiqueta(Set<String> etiquetas, String etiqueta, boolean present) {
        if (present) {
            etiquetas.add(etiqueta);
        } else {
            etiquetas.remove(etiqueta);
        }
    }
}
