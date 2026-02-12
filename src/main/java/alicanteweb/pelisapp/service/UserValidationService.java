package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.constants.ValidationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio especializado en validaciones de usuario.
 * Aplica el principio SRP (Single Responsibility Principle).
 */
@Service
@Slf4j
public class UserValidationService {

    /**
     * Valida la fortaleza de una contraseña según criterios de seguridad.
     */
    public ValidationResult validatePassword(String password) {
        if (password == null || password.length() < ValidationConstants.MIN_PASSWORD_LENGTH) {
            return ValidationResult.failure("La contraseña debe tener al menos " +
                ValidationConstants.MIN_PASSWORD_LENGTH + " caracteres");
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars()
            .anyMatch(c -> ValidationConstants.SPECIAL_CHARACTERS.indexOf(c) >= 0);

        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            return ValidationResult.failure(
                "La contraseña debe contener: mayúscula, minúscula, número y carácter especial");
        }

        return ValidationResult.success();
    }

    /**
     * Valida que un username sea seguro y permitido.
     */
    public ValidationResult validateUsername(String username) {
        if (username == null || username.length() < ValidationConstants.MIN_USERNAME_LENGTH
            || username.length() > ValidationConstants.MAX_USERNAME_LENGTH) {
            return ValidationResult.failure(
                String.format("El username debe tener entre %d y %d caracteres",
                    ValidationConstants.MIN_USERNAME_LENGTH, ValidationConstants.MAX_USERNAME_LENGTH));
        }

        if (!username.matches(ValidationConstants.USERNAME_PATTERN)) {
            return ValidationResult.failure(
                "El username solo puede contener letras, números y guiones bajos");
        }

        if (Character.isDigit(username.charAt(0)) || username.charAt(0) == '_') {
            return ValidationResult.failure(
                "El username no puede empezar con número o guión bajo");
        }

        // Verificar usernames prohibidos
        String usernameLower = username.toLowerCase();
        for (String prohibited : ValidationConstants.PROHIBITED_USERNAMES) {
            if (usernameLower.equals(prohibited)) {
                return ValidationResult.failure("Este username no está permitido");
            }
        }

        return ValidationResult.success();
    }

    /**
     * Valida formato de email.
     */
    public ValidationResult validateEmail(String email) {
        if (email == null || email.length() < ValidationConstants.MIN_EMAIL_LENGTH
            || email.length() > ValidationConstants.MAX_EMAIL_LENGTH) {
            return ValidationResult.failure(
                String.format("El email debe tener entre %d y %d caracteres",
                    ValidationConstants.MIN_EMAIL_LENGTH, ValidationConstants.MAX_EMAIL_LENGTH));
        }

        if (!email.matches(ValidationConstants.EMAIL_PATTERN)) {
            return ValidationResult.failure("Formato de email inválido");
        }

        return ValidationResult.success();
    }

    /**
     * Valida nombre para mostrar.
     */
    public ValidationResult validateDisplayName(String displayName) {
        // DisplayName es completamente opcional: solo validar si tiene contenido y supera el máximo
        if (displayName != null && displayName.length() > ValidationConstants.MAX_DISPLAY_NAME_LENGTH) {
            return ValidationResult.failure(
                String.format("El nombre debe tener como máximo %d caracteres",
                    ValidationConstants.MAX_DISPLAY_NAME_LENGTH));
        }
        return ValidationResult.success();
    }

    /**
     * Valida que las contraseñas coincidan.
     */
    public ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return ValidationResult.failure("Las contraseñas no coinciden");
        }
        return ValidationResult.success();
    }

    /**
     * Valida contenido sospechoso para prevenir inyecciones.
     */
    public ValidationResult validateSuspiciousContent(String content) {
        if (content == null) {
            return ValidationResult.success();
        }

        String lower = content.toLowerCase();
        for (String pattern : ValidationConstants.SUSPICIOUS_PATTERNS) {
            if (lower.contains(pattern)) {
                log.warn("Contenido sospechoso detectado: {}", pattern);
                return ValidationResult.failure("Contenido no permitido detectado");
            }
        }

        return ValidationResult.success();
    }

    /**
         * Resultado inmutable de una validación.
         */
        public record ValidationResult(boolean valid, String errorMessage) {

        public static ValidationResult success() {
                return new ValidationResult(true, null);
            }

            public static ValidationResult failure(String errorMessage) {
                return new ValidationResult(false, errorMessage);
            }

        }
}
