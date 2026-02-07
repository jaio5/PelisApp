package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users-management")
@RequiredArgsConstructor
public class AdminUsersViewController {

    private final UserRepository userRepository;

    @GetMapping("/list")
    public String listUsers(Model model) {
        try {
            // Obtener todos los usuarios sin paginación para simplificar
            List<User> allUsers = userRepository.findAll();

            // Crear una página simulada
            model.addAttribute("users", allUsers);
            model.addAttribute("search", "");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalUsers", allUsers.size());

            return "admin/users-management";
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando usuarios: " + e.getMessage());
            model.addAttribute("users", java.util.Collections.emptyList());
            model.addAttribute("search", "");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalUsers", 0);
            return "admin/users-management";
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        try {
            long userCount = userRepository.count();
            return "Users controller working. Total users: " + userCount;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
