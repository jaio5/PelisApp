package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.service.AuthService;
import alicanteweb.pelisapp.service.UserRegistrationService;
import alicanteweb.pelisapp.service.EmailConfirmationService;
import alicanteweb.pelisapp.service.UserRegistrationService.UserRegistrationRequest;
import alicanteweb.pelisapp.service.UserRegistrationService.UserRegistrationResult;
import alicanteweb.pelisapp.service.EmailConfirmationService.EmailConfirmationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controlador web para registro y confirmación de usuarios.
 * Refactorizado aplicando SRP - delega responsabilidades a servicios especializados.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RegisterController {

    private final UserRegistrationService registrationService;
    private final EmailConfirmationService emailConfirmationService;
    private final AuthService authService; // Solo para compatibilidad

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

        log.info("[DEBUG] Intentando registrar usuario: {}", user.getUsername());
        if (result.hasErrors()) {
            log.warn("[DEBUG] Errores de binding en el formulario: {}", result.getAllErrors());
            return "register";
        }

        // Verificar límite de tasa de registro
        if (!checkRegistrationRateLimit(request, model)) {
            log.warn("[DEBUG] Límite de tasa de registro alcanzado");
            return "register";
        }

        // Crear request object para el servicio
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            confirmPassword,
            user.getDisplayName()
        );

        log.info("[DEBUG] Llamando a registrationService.registerUser para: {}", user.getUsername());
        UserRegistrationResult registrationResult = registrationService.registerUser(registrationRequest);
        log.info("[DEBUG] Resultado del registro: success={}, mensaje={}", registrationResult.success(), registrationResult.message());

        if (!registrationResult.success()) {
            log.warn("[DEBUG] Registro fallido para {}: {}", user.getUsername(), registrationResult.message());
            model.addAttribute("error", registrationResult.message());
            return "register";
        }

        // Mensaje dinámico según configuración de email
        String successMessage = emailEnabled
            ? "✅ Cuenta creada exitosamente. Te hemos enviado un email de confirmación a " +
              user.getEmail() + ". Revisa tu bandeja de entrada y haz clic en el enlace para activar tu cuenta."
            : "✅ Cuenta creada exitosamente. En modo desarrollo, revisa la consola del servidor para el enlace de confirmación.";

        redirectAttributes.addFlashAttribute("success", successMessage);
        log.info("[DEBUG] Usuario registrado exitosamente: {}", user.getUsername());

        return "redirect:/login";
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

        // Validar fortaleza de contraseña básica
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            model.addAttribute("errorPasswordWeak", "La contraseña debe tener al menos 6 caracteres");
            hasError = true;
        }

        // Validar username: solo que no sea nulo o vacío (sin mínimo de caracteres)
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("errorUsername", "El nombre de usuario no puede estar vacío");
            hasError = true;
        }

        // Validar email básico
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().length() < 5) {
            model.addAttribute("errorEmail", "Formato de email inválido");
            hasError = true;
        }

        // Validar displayName: ahora realmente opcional y NO obligatorio ni mínimo
        if (user.getDisplayName() != null && user.getDisplayName().length() > 50) {
            model.addAttribute("errorDisplayName", "El nombre para mostrar debe tener como máximo 50 caracteres si se indica");
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

        EmailConfirmationResult result = emailConfirmationService.confirmAccount(token);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success",
                "🎉 " + result.getMessage() + " Ahora puedes iniciar sesión con seguridad.");
            log.info("Cuenta confirmada exitosamente con token válido");
            return "redirect:/login?confirmed=true";
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ " + result.getMessage());
            log.warn("Intento de confirmación fallido");
            return "redirect:/login?error=invalid_token";
        }
    }

    @GetMapping("/resend-confirmation")
    public String resendConfirmation(@RequestParam("email") String email,
                                   RedirectAttributes redirectAttributes) {

        EmailConfirmationResult result = emailConfirmationService.resendConfirmationEmail(email);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "📧 " + result.getMessage());
            log.info("Token de confirmación reenviado para: {}", email);
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ " + result.getMessage());
            log.warn("Error reenviando confirmación para: {}", email);
        }

        return "redirect:/register";
    }

    /**
     * Página para reenviar confirmación
     */
    @GetMapping("/request-confirmation")
    public String requestConfirmation() {
        return "request-confirmation";
    }


    /**
     * Implementa límite de tasa para registros por IP
     */
    private boolean checkRegistrationRateLimit(HttpServletRequest request, Model model) {
        // Lógica deshabilitada: siempre permite registrar
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

    @PostMapping("/register/validate")
    @ResponseBody
    public String validateUserInputApi(@RequestBody User user, @RequestParam String confirmPassword, Model model) {
        boolean valid = validateUserInput(user, confirmPassword, model);
        return valid ? "OK" : "ERROR";
    }
}
