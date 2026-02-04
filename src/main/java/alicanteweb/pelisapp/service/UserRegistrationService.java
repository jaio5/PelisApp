package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.constants.RoleConstants;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.service.UserValidationService.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio especializado en registro de usuarios.
 * Aplica el principio SRP separando esta responsabilidad del AuthService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService validationService;
    private final EmailConfirmationService emailConfirmationService;

    /**
     * Registra un nuevo usuario web con validaciones completas.
     */
    @Transactional
    public UserRegistrationResult registerUser(UserRegistrationRequest request) {
        try {
            // Validar datos de entrada
            ValidationResult validationResult = validateRegistrationData(request);
            if (!validationResult.isValid()) {
                return UserRegistrationResult.failure(validationResult.getErrorMessage());
            }

            // Verificar duplicados
            if (userRepository.existsByUsername(request.username())) {
                return UserRegistrationResult.failure("El usuario ya existe");
            }
            if (userRepository.existsByEmail(request.email())) {
                return UserRegistrationResult.failure("El email ya está en uso");
            }

            // Crear usuario
            User user = createUser(request);
            User savedUser = userRepository.save(user);

            // Enviar email de confirmación
            sendConfirmationEmail(savedUser);

            log.info("Usuario registrado exitosamente: {}", request.username());
            return UserRegistrationResult.success(savedUser, "Usuario registrado exitosamente");

        } catch (Exception e) {
            log.error("Error registrando usuario {}: {}", request.username(), e.getMessage());
            return UserRegistrationResult.failure("Error al crear la cuenta: " + e.getMessage());
        }
    }

    /**
     * Valida todos los datos de registro usando UserValidationService.
     */
    private ValidationResult validateRegistrationData(UserRegistrationRequest request) {
        // Validar contraseña
        ValidationResult passwordResult = validationService.validatePassword(request.password());
        if (!passwordResult.isValid()) {
            return passwordResult;
        }

        // Validar coincidencia de contraseñas
        ValidationResult passwordMatchResult = validationService
            .validatePasswordMatch(request.password(), request.confirmPassword());
        if (!passwordMatchResult.isValid()) {
            return passwordMatchResult;
        }

        // Validar username
        ValidationResult usernameResult = validationService.validateUsername(request.username());
        if (!usernameResult.isValid()) {
            return usernameResult;
        }

        // Validar email
        ValidationResult emailResult = validationService.validateEmail(request.email());
        if (!emailResult.isValid()) {
            return emailResult;
        }

        // Validar display name
        ValidationResult displayNameResult = validationService.validateDisplayName(request.displayName());
        log.info("[DEBUG] Validación de displayName '{}': valid={}, mensaje={}",
                 request.displayName(), displayNameResult.isValid(), displayNameResult.getErrorMessage());
        if (!displayNameResult.isValid()) {
            return displayNameResult;
        }

        // Validar contenido sospechoso
        ValidationResult suspiciousContentResult = validationService
            .validateSuspiciousContent(request.username());
        if (!suspiciousContentResult.isValid()) {
            return suspiciousContentResult;
        }

        return ValidationResult.success();
    }

    /**
     * Crea entidad User a partir de los datos de registro.
     */
    private User createUser(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRegisteredAt(Instant.now());
        user.setEmailConfirmed(false);

        // Asignar rol USER por defecto
        Role defaultRole = getDefaultUserRole();
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        return user;
    }

    /**
     * Obtiene o crea el rol USER por defecto.
     */
    private Role getDefaultUserRole() {
        return roleRepository.findByName(RoleConstants.USER).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleConstants.USER);
            role.setDescription("Usuario estándar");
            return roleRepository.save(role);
        });
    }

    /**
     * Envía email de confirmación al usuario recién registrado.
     */
    private void sendConfirmationEmail(User user) {
        try {
            String token = emailConfirmationService.generateConfirmationToken(user);
            emailConfirmationService.sendConfirmationEmail(user, token);
        } catch (Exception e) {
            log.error("Error enviando email de confirmación para {}: {}", user.getUsername(), e.getMessage());
            // No fallar el registro por problemas de email
        }
    }

    /**
     * DTO inmutable para datos de registro.
     *
     * @param username Getters
     */
        public record UserRegistrationRequest(String username, String email, String password, String confirmPassword,
                                              String displayName) {

    }

    /**
         * Resultado inmutable de un registro de usuario.
         */
        public record UserRegistrationResult(boolean success, User user, String message) {

        public static UserRegistrationResult success(User user, String message) {
                return new UserRegistrationResult(true, user, message);
            }

            public static UserRegistrationResult failure(String message) {
                return new UserRegistrationResult(false, null, message);
            }

        }
}
