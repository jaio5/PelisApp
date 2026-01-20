package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Etiqueta;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.EtiquetaRepository;
import alicanteweb.pelisapp.repository.ResenaRepository;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.repository.ValoracionResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResenaRepository resenaRepository;
    private final ValoracionResenaRepository valoracionResenaRepository;

    public EtiquetaService(EtiquetaRepository etiquetaRepository, UsuarioRepository usuarioRepository, ResenaRepository resenaRepository, ValoracionResenaRepository valoracionResenaRepository) {
        this.etiquetaRepository = etiquetaRepository;
        this.usuarioRepository = usuarioRepository;
        this.resenaRepository = resenaRepository;
        this.valoracionResenaRepository = valoracionResenaRepository;
    }

    @Transactional
    public void assignBadgesToUser(Integer usuarioId) {
        Optional<Usuario> uOpt = usuarioRepository.findById(usuarioId);
        if (uOpt.isEmpty()) return;
        Usuario u = uOpt.get();

        Set<Etiqueta> nuevos = new HashSet<>();

        // regla 1: reseñas publicadas
        long totalResenas = resenaRepository.countByUsuarioId(usuarioId);
        if (totalResenas >= 50) {
            addBadgeIf(nuevos, "resenas_50","Prolífico crítico","Has publicado 50 reseñas");
        } else if (totalResenas >= 10) {
            addBadgeIf(nuevos, "resenas_10","Crítico activo","Has publicado 10 reseñas");
        }

        // regla 2: media de puntuaciones de sus reseñas >= 4.5
        // la consulta de ResenaRepository utiliza COALESCE(..., 0) por lo que nunca devuelve null
        double avg = resenaRepository.avgPuntuacionByUsuarioId(usuarioId);
        if (avg >= 4.5) {
            addBadgeIf(nuevos, "media_alta","Crítico recomendado","Tu media de puntuación es muy alta");
        }

        // regla 3: nivelCritico > 3 => experto
        if (u.getNivelCritico() != null && u.getNivelCritico() >= 3) {
            addBadgeIf(nuevos, "nivel_experto","Crítico experto","Nivel de crítico alto");
        }

        // regla 4: cantidad de valoraciones positivas recibidas (puntuacion >=4)
        long positive = valoracionResenaRepository.countPositiveByUsuarioId(usuarioId);
        if (positive >= 50) {
            addBadgeIf(nuevos, "likes_50","Muy valorado","Has recibido 50 valoraciones positivas");
        } else if (positive >= 10) {
            addBadgeIf(nuevos, "likes_10","Valorado","Has recibido 10 valoraciones positivas");
        }

        // regla 5: número de seguidores
        long seguidores = usuarioRepository.countSeguidoresByUsuarioId(usuarioId);
        if (seguidores >= 100) {
            addBadgeIf(nuevos, "popular","Popular","Tienes más de 100 seguidores");
        } else if (seguidores >= 20) {
            addBadgeIf(nuevos, "conocido","Conocido","Tienes más de 20 seguidores");
        }

        // fusionar con las etiquetas existentes
        u.getEtiquetas().addAll(nuevos);
        usuarioRepository.save(u);
    }

    /**
     * Añade una etiqueta al conjunto si la condición se cumple; se busca o crea la etiqueta.
     */
    private void addBadgeIf(Set<Etiqueta> set, String clave, String nombre, String descripcion) {
        Etiqueta e = etiquetaRepository.findByClave(clave).orElseGet(() -> {
            Etiqueta ne = new Etiqueta();
            ne.setClave(clave);
            ne.setNombre(nombre);
            ne.setDescripcion(descripcion);
            return etiquetaRepository.save(ne);
        });
        set.add(e);
    }
}
