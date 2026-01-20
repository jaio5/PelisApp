package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Etiqueta;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.EtiquetaRepository;
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
    private final EtiquetaRepository etiquetaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ValoracionResenaRepository valoracionResenaRepository, EtiquetaRepository etiquetaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.valoracionResenaRepository = valoracionResenaRepository;
        this.etiquetaRepository = etiquetaRepository;
    }

    @Transactional
    public void recalcularNivelCritico(Integer usuarioId) {
        // avg viene como double (COALESCE en la consulta), por tanto no es null
        double avg = valoracionResenaRepository.avgPuntuacionRecibidaByUsuarioId(usuarioId);

        int nuevoNivel = Math.max(1, Math.min(5, (int) Math.round(avg)));

        Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
        if (u != null) {
            if (!Integer.valueOf(nuevoNivel).equals(u.getNivelCritico())) {
                u.setNivelCritico(nuevoNivel);
            }

            // Asignar roles según average recibido
            Set<Role> newRoles = new HashSet<>();
            newRoles.add(Role.ROLE_USER);
            if (avg >= 4.5) {
                newRoles.add(Role.ROLE_EXPERTO);
            } else if (avg >= 3.5) {
                newRoles.add(Role.ROLE_CRITICO);
            }
            u.setRoles(newRoles);

            // Etiquetas como entidades según reglas
            Set<Etiqueta> etiquetas = new HashSet<>(u.getEtiquetas());

            // regla: reseñas publicadas
            // We don't have ResenaRepository injected here; rely on existing etiquetas rules elsewhere or expand constructor. For now use avg and nivel.
            updateEtiqueta(etiquetas, "media_alta", avg >= 4.5, "Crítico recomendado", "Tu media de puntuación es muy alta");
            updateEtiqueta(etiquetas, "nivel_experto", u.getNivelCritico() != null && u.getNivelCritico() >= 3, "Crítico experto", "Nivel de crítico alto");

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

    private void updateEtiqueta(Set<Etiqueta> etiquetas, String clave, boolean present, String nombre, String descripcion) {
        if (present) {
            Etiqueta e = findOrCreateEtiqueta(clave, nombre, descripcion);
            etiquetas.add(e);
        } else {
            etiquetas.removeIf(et -> clave.equals(et.getClave()));
        }
    }

    private Etiqueta findOrCreateEtiqueta(String clave, String nombre, String descripcion) {
        return etiquetaRepository.findByClave(clave).orElseGet(() -> {
            Etiqueta e = new Etiqueta();
            e.setClave(clave);
            e.setNombre(nombre != null ? nombre : clave);
            e.setDescripcion(descripcion);
            return etiquetaRepository.save(e);
        });
    }
}
