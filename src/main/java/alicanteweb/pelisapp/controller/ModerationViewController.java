package alicanteweb.pelisapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para mostrar la página de administración de moderación
 */
@Controller
@RequestMapping("/admin")
public class ModerationViewController {

    @GetMapping("/moderation")
    public String showModerationPanel() {
        return "admin/moderation";
    }
}
