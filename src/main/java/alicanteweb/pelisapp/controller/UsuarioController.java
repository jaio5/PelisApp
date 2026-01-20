package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ResenaDTO;
import alicanteweb.pelisapp.dto.UsuarioDetailDTO;
import alicanteweb.pelisapp.dto.UsuarioProfileDTO;
import alicanteweb.pelisapp.entity.Etiqueta;
import alicanteweb.pelisapp.entity.Resena;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.mapper.ResenaMapper;
import alicanteweb.pelisapp.repository.ResenaRepository;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ResenaRepository resenaRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, ResenaRepository resenaRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.resenaRepository = resenaRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> profile(@PathVariable Integer id) {
        Optional<Usuario> uOpt = usuarioService.getProfile(id);
        if (uOpt.isEmpty()) return ResponseEntity.notFound().build();
        Usuario u = uOpt.get();
        Set<String> roles = u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        Set<String> etiquetas = u.getEtiquetas().stream().map(Etiqueta::getClave).collect(Collectors.toSet());
        // obtener reseñas del usuario (fetch pelicula para evitar lazy)
        List<Resena> resenas = resenaRepository.findByUsuarioIdFetchPelicula(id);
        List<ResenaDTO> resenasDto = resenas.stream().map(ResenaMapper::toDto).collect(Collectors.toList());
        UsuarioProfileDTO dto = new UsuarioProfileDTO(u.getId(), u.getUsername(), u.getNivelCritico(), u.getFechaRegistro(), etiquetas, roles);
        UsuarioDetailDTO detail = new UsuarioDetailDTO(dto, resenasDto);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<?> follow(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");
        var opt = usuarioRepository.findByUsername(userDetails.getUsername());
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Usuario autentificado no encontrado");
        Usuario u = opt.get();
        usuarioService.followUser(u.getId(), id);
        return ResponseEntity.ok("Seguido");
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<?> unfollow(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");
        var opt = usuarioRepository.findByUsername(userDetails.getUsername());
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Usuario autentificado no encontrado");
        Usuario u = opt.get();
        usuarioService.unfollowUser(u.getId(), id);
        return ResponseEntity.ok("Dejado de seguir");
    }
}
