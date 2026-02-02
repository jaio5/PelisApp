package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.service.AuthService;
import alicanteweb.pelisapp.service.MockEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controlador web para registro y confirmación de usuarios
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RegisterController {

    private final AuthService authService;
    private final MockEmailService emailService; // Usar MockEmailService en desarrollo

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               @RequestParam String confirmPassword,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Validaciones adicionales de seguridad
        if (!validateUserInput(user, confirmPassword, model)) {
            return "register";
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Registrar el usuario con estado pendiente de confirmación
            User registeredUser = authService.registerWebUser(user);

            // Enviar email de confirmación
            String confirmationToken = authService.generateConfirmationToken(registeredUser.getId());
            emailService.sendConfirmationEmail(registeredUser.getEmail(),
                                               registeredUser.getUsername(),
                                               confirmationToken);

            redirectAttributes.addFlashAttribute("success",
                "✅ Cuenta creada exitosamente. En modo desarrollo, revisa la consola del servidor para el enlace de confirmación.");

            log.info("Usuario registrado: {} - Email de confirmación simulado enviado", user.getUsername());

            return "redirect:/login";

        } catch (Exception e) {
            log.error("Error registrando usuario: {}", e.getMessage());
            model.addAttribute("error", "Error al crear la cuenta: " + e.getMessage());
            return "register";
        }
    }

    /**
     * Valida los datos de entrada del usuario con criterios de seguridad estrictos
     */
    private boolean validateUserInput(User user, String confirmPassword, Model model) {
        boolean hasError = false;

        // Validar que las contraseñas coincidan
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            hasError = true;
        }

        // Validar fortaleza de contraseña
        if (!isPasswordStrong(user.getPassword())) {
            model.addAttribute("errorPasswordWeak",
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial");
            hasError = true;
        }

        // Validar username (sin espacios, caracteres especiales peligrosos)
        if (!isUsernameValid(user.getUsername())) {
            model.addAttribute("errorUsername",
                "El nombre de usuario solo puede contener letras, números y guiones bajos");
            hasError = true;
        }

        // Validar email formato
        if (!isEmailValid(user.getEmail())) {
            model.addAttribute("errorEmail", "Formato de email inválido");
            hasError = true;
        }

        return !hasError;
    }

    /**
     * Verifica que la contraseña sea lo suficientemente fuerte
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Verifica que el username sea válido y seguro
     */
    private boolean isUsernameValid(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) return false;
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Verifica formato básico de email
     */
    private boolean isEmailValid(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @GetMapping("/confirm-account")
    public String confirmAccount(@RequestParam("token") String token,
                                RedirectAttributes redirectAttributes) {
        try {
            boolean confirmed = authService.confirmAccount(token);

            if (confirmed) {
                redirectAttributes.addFlashAttribute("success",
                    "🎉 ¡Cuenta confirmada exitosamente! Ahora puedes iniciar sesión con seguridad.");
                log.info("Cuenta confirmada exitosamente con token válido");
                return "redirect:/login?confirmed=true";
            } else {
                redirectAttributes.addFlashAttribute("error",
                    "❌ Token de confirmación inválido o expirado. Solicita uno nuevo.");
                log.warn("Intento de confirmación con token inválido");
                return "redirect:/login?error=invalid_token";
            }

        } catch (Exception e) {
            log.error("Error confirmando cuenta: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                "❌ Error técnico al confirmar la cuenta. Inténtalo de nuevo más tarde.");
            return "redirect:/login?error=technical";
        }
    }

    @GetMapping("/resend-confirmation")
    public String resendConfirmation(@RequestParam("email") String email,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = authService.findUserByEmail(email);
            if (user != null && !user.isEmailConfirmed()) {
                String newToken = authService.generateConfirmationToken(user.getId());
                emailService.sendConfirmationEmail(user.getEmail(), user.getUsername(), newToken);

                redirectAttributes.addFlashAttribute("success",
                    "Email de confirmación reenviado. Revisa la consola en modo desarrollo.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                    "Email no encontrado o ya está confirmado.");
            }

        } catch (Exception e) {
            log.error("Error reenviando confirmación: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                "Error al reenviar email de confirmación.");
        }

        return "redirect:/login";
    }
}
