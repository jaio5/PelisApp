package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.repository.ValoracionResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
            if (avg != null && avg >= 4.5) {
                etiquetas.add("TopCritic");
            } else {
                etiquetas.remove("TopCritic");
            }
            // Si tiene nivelCritico alto añadir etiqueta de confianza
            if (u.getNivelCritico() != null && u.getNivelCritico() >= 4) {
                etiquetas.add("ConfianzaAlta");
            } else {
                etiquetas.remove("ConfianzaAlta");
            }
            u.setEtiquetas(etiquetas);

            usuarioRepository.save(u);
        }
    }
}
