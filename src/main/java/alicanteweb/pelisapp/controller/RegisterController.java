package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.service.AuthService;
import alicanteweb.pelisapp.service.IEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * Controlador web para registro y confirmación de usuarios
 * Maneja tanto email real como simulado según configuración
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RegisterController {

    private final AuthService authService;
    private final IEmailService emailService; // Se auto-configura según app.email.enabled

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;


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
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {

        // Validaciones adicionales de seguridad
        if (!validateUserInput(user, confirmPassword, model)) {
            return "register";
        }

        if (result.hasErrors()) {
            return "register";
        }

        // Verificar límite de tasa de registro
        if (!checkRegistrationRateLimit(request, model)) {
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

            // Mensaje dinámico según configuración de email
            String successMessage;
            if (emailEnabled) {
                successMessage = "✅ Cuenta creada exitosamente. Te hemos enviado un email de confirmación a " +
                               user.getEmail() + ". Revisa tu bandeja de entrada y haz clic en el enlace para activar tu cuenta.";
            } else {
                successMessage = "✅ Cuenta creada exitosamente. En modo desarrollo, revisa la consola del servidor para el enlace de confirmación.";
            }

            redirectAttributes.addFlashAttribute("success", successMessage);
            log.info("Usuario registrado: {} - Email {} enviado", user.getUsername(), emailEnabled ? "real" : "simulado");

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

        // Validar fortaleza de contraseña usando el servicio
        if (!authService.isPasswordStrong(user.getPassword())) {
            model.addAttribute("errorPasswordWeak",
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial");
            hasError = true;
        }

        // Validar username usando el servicio
        if (!authService.isUsernameValid(user.getUsername())) {
            model.addAttribute("errorUsername",
                "El nombre de usuario debe tener 3-20 caracteres, solo letras, números y guiones bajos, y no puede empezar con número o guión bajo");
            hasError = true;
        }

        // Validar email usando el servicio
        if (!authService.isEmailValid(user.getEmail())) {
            model.addAttribute("errorEmail", "Formato de email inválido");
            hasError = true;
        }

        // Validar displayName
        if (user.getDisplayName() != null && (user.getDisplayName().length() > 50 ||
            user.getDisplayName().trim().isEmpty())) {
            model.addAttribute("errorDisplayName",
                "El nombre para mostrar debe tener entre 1 y 50 caracteres");
            hasError = true;
        }

        // Validación adicional de seguridad: detectar intentos de inyección
        if (containsSuspiciousContent(user.getUsername()) ||
            containsSuspiciousContent(user.getEmail()) ||
            containsSuspiciousContent(user.getDisplayName())) {

            log.warn("Intento de registro con contenido sospechoso");
            model.addAttribute("error", "Datos de entrada inválidos");
            hasError = true;
        }

        return !hasError;
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

    /**
     * Implementa límite de tasa para registros por IP
     */
    private boolean checkRegistrationRateLimit(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String clientIp = getClientIpAddress(request);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRegistration = (LocalDateTime) session.getAttribute("lastRegistrationAttempt");

        if (lastRegistration != null) {
            long minutesSinceLastAttempt = java.time.Duration.between(lastRegistration, now).toMinutes();

            if (minutesSinceLastAttempt < 5) { // Mínimo 5 minutos entre registros
                model.addAttribute("error",
                    "Debes esperar al menos 5 minutos antes de crear otra cuenta.");
                log.warn("Intento de registro demasiado rápido desde IP: {}", clientIp);
                return false;
            }
        }

        session.setAttribute("lastRegistrationAttempt", now);
        return true;
    }

    /**
     * Verifica si hay contenido potencialmente malicioso
     */
    private boolean containsSuspiciousContent(String content) {
        if (content == null) return false;

        String lower = content.toLowerCase();
        String[] suspiciousPatterns = {
            "<script", "javascript:", "vbscript:", "onload=", "onerror=",
            "eval(", "document.", "window.", "alert(", "confirm(",
            "drop table", "delete from", "insert into", "update set",
            "union select", "' or '", "' and '", "--", "/*", "*/"
        };

        for (String pattern : suspiciousPatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

}
