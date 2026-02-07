package alicanteweb.pelisapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "confirmed", required = false) String confirmed,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", true);
            System.out.println("[DEBUG] LoginController: error param detectado");
        }
        if (logout != null) {
            model.addAttribute("logout", true);
            System.out.println("[DEBUG] LoginController: logout param detectado");
        }
        if (confirmed != null) {
            model.addAttribute("confirmed", true);
            System.out.println("[DEBUG] LoginController: confirmed param detectado");
        }
        System.out.println("[DEBUG] LoginController: Acceso a /login con params -> error=" + error + ", logout=" + logout + ", confirmed=" + confirmed);
        return "login";
    }

}
