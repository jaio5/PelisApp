package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.LoginResponse;
import alicanteweb.pelisapp.dto.LoginRequest;
import alicanteweb.pelisapp.dto.RegisterRequest;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.security.JwtTokenProvider;
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
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email already exists");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setDisplayName(req.getDisplayName());
        u.setRegisteredAt(Instant.now());

        Role defaultRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Default role");
            return roleRepository.save(r);
        });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        u.setRoles(roles);

        userRepository.save(u);

        String access = jwtTokenProvider.createAccessToken(u.getUsername(), Collections.singleton("ROLE_USER"));
        String refresh = jwtTokenProvider.createRefreshToken(u.getUsername());
        long expires = jwtTokenProvider.getExpiryMillis(access);
        return new LoginResponse(access, expires, refresh);
    }

    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        Set<String> roles = principal.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        String access = jwtTokenProvider.createAccessToken(principal.getUsername(), roles);
        String refresh = jwtTokenProvider.createRefreshToken(principal.getUsername());
        long expires = jwtTokenProvider.getExpiryMillis(access);
        return new LoginResponse(access, expires, refresh);
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String username = jwtTokenProvider.getUsername(refreshToken);
        User u = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<String> roles = u.getRoles() == null ? Collections.emptySet() : u.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
        String access = jwtTokenProvider.createAccessToken(username, roles);
        String refresh = jwtTokenProvider.createRefreshToken(username);
        long expires = jwtTokenProvider.getExpiryMillis(access);
        return new LoginResponse(access, expires, refresh);
    }

    /**
     * Registro web con confirmación de email
     */
    @Transactional
    public User registerWebUser(User user) {
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegisteredAt(java.time.Instant.now());
        user.setEmailConfirmed(false); // Por defecto no confirmado

        // Asignar rol USER por defecto
        Role defaultRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            r.setDescription("Usuario estándar");
            return roleRepository.save(r);
        });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     * Generar token de confirmación con mayor seguridad
     */
    public String generateConfirmationToken(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar que el usuario no esté ya confirmado
        if (user.isEmailConfirmed()) {
            throw new IllegalStateException("La cuenta ya está confirmada");
        }

        // Crear token JWT con expiración de 24 horas y datos adicionales de seguridad
        return jwtTokenProvider.createConfirmationToken(
            user.getUsername(),
            24 * 60 * 60 * 1000L, // 24 horas
            user.getEmail()
        );
    }

    /**
     * Confirmar cuenta con token y validaciones adicionales de seguridad
     */
    @Transactional
    public boolean confirmAccount(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Token de confirmación inválido: {}", token.substring(0, Math.min(10, token.length())));
                return false;
            }

            String username = jwtTokenProvider.getUsername(token);
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                log.warn("Usuario no encontrado para confirmación: {}", username);
                return false;
            }

            User user = userOpt.get();

            // Verificar que el email del token coincide con el del usuario
            String tokenEmail = jwtTokenProvider.getEmailFromToken(token);
            if (!user.getEmail().equals(tokenEmail)) {
                log.warn("Email del token no coincide para usuario: {}", username);
                return false;
            }

            // Verificar que no esté ya confirmado
            if (user.isEmailConfirmed()) {
                log.info("Usuario ya confirmado: {}", username);
                return true; // Ya está confirmado, considerar como éxito
            }

            user.setEmailConfirmed(true);
            userRepository.save(user);

            log.info("Cuenta confirmada exitosamente para usuario: {}", username);
            return true;

        } catch (Exception e) {
            log.error("Error confirmando cuenta: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generar nuevo token de confirmación para reenvío
     */
    @Transactional
    public String resendConfirmationToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No existe usuario con ese email"));

        if (user.isEmailConfirmed()) {
            throw new IllegalStateException("La cuenta ya está confirmada");
        }

        return generateConfirmationToken(user.getId());
    }

    /**
     * Validar fortaleza de contraseña
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Validar si el username es seguro
     */
    public boolean isUsernameValid(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            return false;
        }

        // Solo permitir letras, números y guiones bajos
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return false;
        }

        // No permitir usernames que empiecen con números o guiones bajos
        if (Character.isDigit(username.charAt(0)) || username.charAt(0) == '_') {
            return false;
        }

        // Lista de nombres de usuario prohibidos
        String[] prohibitedUsernames = {
            "admin", "administrator", "root", "user", "test", "guest", "api", "support",
            "info", "contact", "help", "service", "mail", "email", "www", "ftp"
        };

        String usernameLower = username.toLowerCase();
        for (String prohibited : prohibitedUsernames) {
            if (usernameLower.equals(prohibited)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validar formato de email
     */
    public boolean isEmailValid(String email) {
        if (email == null || email.length() < 5 || email.length() > 100) {
            return false;
        }

        // Regex básico pero efectivo para email
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Buscar usuario por email
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Verificar si un usuario está habilitado para login
     */
    public boolean isUserEnabledForLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        // Un usuario puede hacer login si está confirmado por email
        return user.isEmailConfirmed();
    }

    /**
     * Obtener información de seguridad del usuario
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

    /**
     * DTO para información de seguridad del usuario
     */
    public static class UserSecurityInfo {
        private String username;
        private String email;
        private boolean emailConfirmed;
        private java.time.Instant registeredAt;
        private java.util.List<String> roles;

        public static UserSecurityInfoBuilder builder() {
            return new UserSecurityInfoBuilder();
        }

        public static class UserSecurityInfoBuilder {
            private String username;
            private String email;
            private boolean emailConfirmed;
            private java.time.Instant registeredAt;
            private java.util.List<String> roles;

            public UserSecurityInfoBuilder username(String username) {
                this.username = username;
                return this;
            }

            public UserSecurityInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public UserSecurityInfoBuilder emailConfirmed(boolean emailConfirmed) {
                this.emailConfirmed = emailConfirmed;
                return this;
            }

            public UserSecurityInfoBuilder registeredAt(java.time.Instant registeredAt) {
                this.registeredAt = registeredAt;
                return this;
            }

            public UserSecurityInfoBuilder roles(java.util.List<String> roles) {
                this.roles = roles;
                return this;
            }

            public UserSecurityInfo build() {
                UserSecurityInfo info = new UserSecurityInfo();
                info.username = this.username;
                info.email = this.email;
                info.emailConfirmed = this.emailConfirmed;
                info.registeredAt = this.registeredAt;
                info.roles = this.roles;
                return info;
            }
        }

        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public boolean isEmailConfirmed() { return emailConfirmed; }
        public java.time.Instant getRegisteredAt() { return registeredAt; }
        public java.util.List<String> getRoles() { return roles; }
    }

}
