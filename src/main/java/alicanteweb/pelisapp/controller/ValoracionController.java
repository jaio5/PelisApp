package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ValoracionDTO;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.service.ValoracionResenaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/resenas/{resenaId}/valoraciones")
public class ValoracionController {

    private final ValoracionResenaService valoracionResenaService;
    private final UsuarioRepository usuarioRepository;

    public ValoracionController(ValoracionResenaService valoracionResenaService, UsuarioRepository usuarioRepository) {
        this.valoracionResenaService = valoracionResenaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> valorar(@PathVariable Integer resenaId, @RequestBody ValoracionDTO dto, @AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        Optional<Usuario> uOpt = usuarioRepository.findAll().stream().filter(u->u.getUsername().equals(user.getUsername())).findFirst();
        if (uOpt.isEmpty()) return ResponseEntity.status(404).body("Usuario no encontrado");
        Usuario u = uOpt.get();

        // Llamar servicio para crear o actualizar
        var saved = valoracionResenaService.crearOActualizarValoracion(resenaId, u.getId(), dto.puntuacion(), dto.comentario());
        return ResponseEntity.ok(saved);
    }
}
