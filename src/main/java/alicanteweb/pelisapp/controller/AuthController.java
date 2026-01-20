package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.RefreshToken;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Usuario;
import alicanteweb.pelisapp.repository.UsuarioRepository;
import alicanteweb.pelisapp.security.JwtUtil;
import alicanteweb.pelisapp.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (usuarioRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username ya existe");
        }
        Usuario u = new Usuario();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        u.setRoles(roles);
        usuarioRepository.save(u);
        return ResponseEntity.ok("Usuario creado");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        // Si no lanza excepción, autenticación OK
        var user = usuarioRepository.findByUsername(req.username()).orElseThrow();
        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        String accessToken = jwtUtil.generateToken(user.getUsername(), roles);
        RefreshToken refresh = refreshTokenService.createTokenForUser(user);
        return ResponseEntity.ok(new AuthResponse(accessToken, refresh.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        var opt = refreshTokenService.findByToken(req.refreshToken());
        if (opt.isEmpty()) return ResponseEntity.status(401).body("Refresh token inválido");
        RefreshToken rt = opt.get();
        if (rt.getExpiryDate().isBefore(java.time.Instant.now())) {
            refreshTokenService.deleteByUsuario(rt.getUsuario());
            return ResponseEntity.status(401).body("Refresh token expirado");
        }
        var user = rt.getUsuario();
        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        String accessToken = jwtUtil.generateToken(user.getUsername(), roles);
        return ResponseEntity.ok(new AuthResponse(accessToken, rt.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest req) {
        var opt = refreshTokenService.findByToken(req.refreshToken());
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Token no encontrado");
        refreshTokenService.deleteByUsuario(opt.get().getUsuario());
        return ResponseEntity.ok("Desconectado");
    }

    // DTOs
    public record RegisterRequest(String username, String password){}
    public record AuthRequest(String username, String password){}
    public record RefreshRequest(String refreshToken){}
    public record AuthResponse(String accessToken, String refreshToken){}
}
