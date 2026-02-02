package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controller responsable de la gestión de perfiles de usuario.
 * Sigue el principio de responsabilidad única delegando la lógica de negocio al servicio.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/mi-perfil")
    public String myProfile(Model model, Principal principal, Authentication authentication) {
        if (principal == null) {
            log.debug("Usuario no autenticado intentó acceder al perfil");
            return "redirect:/login";
        }

        try {
            userService.loadUserProfileData(principal.getName(), authentication, model);
            return "usuario/profile";
        } catch (Exception e) {
            log.error("Error cargando perfil de usuario: {}", e.getMessage());
            return "redirect:/login";
        }
    }
}
