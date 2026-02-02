package alicanteweb.pelisapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para gestionar la configuración de emails
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class EmailConfigController {

    private final Environment environment;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:}")
    private String emailUsername;

    @Value("${spring.mail.host:}")
    private String emailHost;

    @GetMapping("/email-config")
    public String showEmailConfig(Model model) {
        model.addAttribute("emailEnabled", emailEnabled);
        model.addAttribute("emailUsername", emailUsername);
        model.addAttribute("emailHost", emailHost);
        model.addAttribute("emailConfigured", !emailUsername.isEmpty() && !emailUsername.equals("tu-email@gmail.com"));

        return "admin/email-config";
    }
}
