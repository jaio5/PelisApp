package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ResenaDTO;
import alicanteweb.pelisapp.entity.Pelicula;
import alicanteweb.pelisapp.entity.Resena;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.PeliculaRepository;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.service.ResenaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/peliculas/{peliculaId}/resenas")
public class ResenaController {

    private final ResenaService resenaService;
    private final UsuarioRepository usuarioRepository;
    private final PeliculaRepository peliculaRepository;

    public ResenaController(ResenaService resenaService, UsuarioRepository usuarioRepository, PeliculaRepository peliculaRepository) {
        this.resenaService = resenaService;
        this.usuarioRepository = usuarioRepository;
        this.peliculaRepository = peliculaRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearResena(@PathVariable Integer peliculaId,
                                         @RequestBody ResenaDTO dto,
                                         @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Optional<Usuario> uOpt = usuarioRepository.findByUsername(user.getUsername());
        if (uOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        Usuario u = uOpt.get();

        Optional<Pelicula> pOpt = peliculaRepository.findById(peliculaId);
        if (pOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Pelicula no encontrada");
        }
        Pelicula p = pOpt.get();

        if (dto.puntuacion() == null || dto.puntuacion() < 1 || dto.puntuacion() > 5) {
            return ResponseEntity.badRequest().body("Puntuacion debe ser entre 1 y 5");
        }

        Resena r = new Resena();
        r.setComentario(dto.comentario());
        r.setPuntuacion(dto.puntuacion());
        r.setUsuario(u);
        r.setPelicula(p);

        Resena saved = resenaService.saveResena(r);
        return ResponseEntity.ok(saved);
    }
}