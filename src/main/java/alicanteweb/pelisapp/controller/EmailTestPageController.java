package alicanteweb.pelisapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para página de test de email
 */
@Controller
public class EmailTestPageController {

    @GetMapping("/email-test")
    public String emailTestPage() {
        return "email-test";
    }
}
