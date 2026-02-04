package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.LoginResponse;
import alicanteweb.pelisapp.dto.LoginRequest;
import alicanteweb.pelisapp.dto.RegisterRequest;
import alicanteweb.pelisapp.dto.UserSecurityInfo;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio principal de autenticación.
 * Refactorizado aplicando SRP - se enfoca únicamente en autenticación.
 *
 * Responsabilidades movidas:
 * - Validaciones → UserValidationService
 * - Registro → UserRegistrationService
 * - Confirmación email → EmailConfirmationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Registro API (mantener compatibilidad).
     * NOTA: Para web, usar UserRegistrationService directamente.
     * Se agregan logs detallados para debug.
     */
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        log.info("[DEBUG] Intentando registrar usuario por API: {}", req.getUsername());
        if (userRepository.existsByUsername(req.getUsername())) {
            log.warn("[DEBUG] El username ya existe: {}", req.getUsername());
            throw new IllegalArgumentException("username already exists");
        }
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            log.warn("[DEBUG] El email ya existe: {}", req.getEmail());
            throw new IllegalArgumentException("email already exists");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setDisplayName(req.getDisplayName());
        u.setRegisteredAt(Instant.now());
        u.setEmailConfirmed(true); // API registration auto-confirmed

        log.info("[DEBUG] Asignando rol por defecto a usuario: {}", req.getUsername());
        Role defaultRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            log.info("[DEBUG] Rol ROLE_USER no existe, creando...");
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Default role");
            return roleRepository.save(r);
        });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        u.setRoles(roles);

        log.info("[DEBUG] Guardando usuario en base de datos: {}", req.getUsername());
        userRepository.save(u);
        log.info("[DEBUG] Usuario guardado correctamente: {}", req.getUsername());

        String access = jwtTokenProvider.createAccessToken(u.getUsername(), Collections.singleton("ROLE_USER"));
        String refresh = jwtTokenProvider.createRefreshToken(u.getUsername());
        long expires = jwtTokenProvider.getExpiryMillis(access);
        log.info("[DEBUG] Tokens generados para usuario: {}", req.getUsername());
        return new LoginResponse(access, expires, refresh);
    }

    /**
     * Autenticación principal del sistema.
     * Se agregan logs para debug.
     */
    public LoginResponse login(LoginRequest req) {
        log.info("[DEBUG] Intentando login para usuario: {}", req.getUsername());
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();

            Set<String> roles = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(java.util.stream.Collectors.toSet());

            String access = jwtTokenProvider.createAccessToken(principal.getUsername(), roles);
            String refresh = jwtTokenProvider.createRefreshToken(principal.getUsername());
            long expires = jwtTokenProvider.getExpiryMillis(access);

            log.info("[DEBUG] Login exitoso para usuario: {}", req.getUsername());
            return new LoginResponse(access, expires, refresh);
        } catch (Exception e) {
            log.error("[DEBUG] Error en login para usuario {}: {}", req.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * Renovación de token.
     */
    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<String> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(java.util.stream.Collectors.toSet());

        String access = jwtTokenProvider.createAccessToken(username, roles);
        long expires = jwtTokenProvider.getExpiryMillis(access);

        return new LoginResponse(access, expires, refreshToken);
    }

    /**
     * Buscar usuario por email (mantenido para compatibilidad).
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Verificar si un usuario está habilitado para login.
     */
    public boolean isUserEnabledForLogin(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Obtener información de seguridad del usuario.
     */
    public UserSecurityInfo getUserSecurityInfo(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return UserSecurityInfo.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .emailConfirmed(user.isEmailConfirmed())
            .registeredAt(user.getRegisteredAt())
            .roles(user.getRoles().stream().map(Role::getName).toList())
            .build();
    }
}

