package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio especializado en confirmación de email.
 * Aplica el principio SRP separando esta responsabilidad del AuthService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final IEmailService emailService;

    private static final long TOKEN_VALIDITY_24_HOURS = 24 * 60 * 60 * 1000L;

    /**
     * Genera un token de confirmación seguro para el usuario.
     */
    public String generateConfirmationToken(User user) {
        if (user.isEmailConfirmed()) {
            throw new IllegalStateException("La cuenta ya está confirmada");
        }

        return jwtTokenProvider.createConfirmationToken(
            user.getUsername(),
            TOKEN_VALIDITY_24_HOURS,
            user.getEmail()
        );
    }

    /**
     * Envía email de confirmación al usuario.
     */
    public void sendConfirmationEmail(User user, String token) {
        try {
            emailService.sendConfirmationEmail(
                user.getEmail(),
                user.getUsername(),
                token
            );
            log.info("Email de confirmación enviado a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error enviando email de confirmación a {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Error enviando email de confirmación", e);
        }
    }

    /**
     * Confirma una cuenta usando un token JWT.
     */
    @Transactional
    public EmailConfirmationResult confirmAccount(String token) {
        try {
            // Validar token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Token de confirmación inválido");
                return EmailConfirmationResult.failure("Token de confirmación inválido o expirado");
            }

            // Extraer información del token
            String username = jwtTokenProvider.getUsername(token);
            String tokenEmail = jwtTokenProvider.getEmailFromToken(token);

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("Usuario no encontrado para confirmación: {}", username);
                return EmailConfirmationResult.failure("Usuario no encontrado");
            }

            User user = userOpt.get();

            // Verificar que el email del token coincide
            if (tokenEmail != null && !user.getEmail().equals(tokenEmail)) {
                log.warn("Email del token no coincide para usuario: {}", username);
                return EmailConfirmationResult.failure("Token inválido para este usuario");
            }

            // Verificar si ya está confirmado
            if (user.isEmailConfirmed()) {
                log.info("Usuario ya confirmado: {}", username);
                return EmailConfirmationResult.success("La cuenta ya estaba confirmada");
            }

            // Confirmar cuenta
            user.setEmailConfirmed(true);
            userRepository.save(user);

            log.info("Cuenta confirmada exitosamente para usuario: {}", username);
            return EmailConfirmationResult.success("Cuenta confirmada exitosamente");

        } catch (Exception e) {
            log.error("Error confirmando cuenta: {}", e.getMessage());
            return EmailConfirmationResult.failure("Error técnico al confirmar la cuenta");
        }
    }

    /**
     * Reenvía email de confirmación para un email dado.
     */
    @Transactional
    public EmailConfirmationResult resendConfirmationEmail(String email) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return EmailConfirmationResult.failure("No existe usuario con ese email");
            }

            if (user.isEmailConfirmed()) {
                return EmailConfirmationResult.failure("La cuenta ya está confirmada");
            }

            String token = generateConfirmationToken(user);
            sendConfirmationEmail(user, token);

            return EmailConfirmationResult.success("Email de confirmación reenviado");

        } catch (Exception e) {
            log.error("Error reenviando confirmación para {}: {}", email, e.getMessage());
            return EmailConfirmationResult.failure("Error reenviando email de confirmación");
        }
    }

    /**
         * Resultado inmutable de una operación de confirmación por email.
         */
        public record EmailConfirmationResult(boolean success, String message) {

        public static EmailConfirmationResult success(String message) {
                return new EmailConfirmationResult(true, message);
            }

            public static EmailConfirmationResult failure(String message) {
                return new EmailConfirmationResult(false, message);
            }
        }
}
