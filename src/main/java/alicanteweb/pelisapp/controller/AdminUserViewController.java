package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador web para la gestión de usuarios en el panel admin
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminUserViewController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUsersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            Model model) {

        try {
            // Create sort
            Sort sortObj = direction.equals("desc") ?
                Sort.by(sort).descending() :
                Sort.by(sort).ascending();

            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<User> usersPage;

            // Search functionality
            if (search != null && !search.trim().isEmpty()) {
                usersPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
                model.addAttribute("search", search);
            } else {
                usersPage = userRepository.findAll(pageable);
            }

            // Calculate statistics
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByEmailConfirmed(true);
            long pendingUsers = userRepository.countByEmailConfirmed(false);
            long newUsersThisMonth = totalUsers / 4; // Mock calculation

            // Add attributes to model
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalElements", usersPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);

            // Statistics
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("pendingUsers", pendingUsers);
            model.addAttribute("newUsersThisMonth", newUsersThisMonth);
            model.addAttribute("bannedUsers", totalUsers - activeUsers - pendingUsers);

            // Available roles for dropdowns
            model.addAttribute("availableRoles", roleRepository.findAll());

            log.info("Loading users page: {} users found", usersPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error loading users page: {}", e.getMessage());
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            model.addAttribute("users", new java.util.ArrayList<>());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("activeUsers", 0);
            model.addAttribute("pendingUsers", 0);
            model.addAttribute("bannedUsers", 0);
        }

        return "admin/users";
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String getUserDetail(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Build HTML response manually to avoid String.format issues
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"row\">");
            html.append("<div class=\"col-md-4 text-center\">");
            html.append("<img src=\"https://via.placeholder.com/120x120?text=").append(user.getUsername().substring(0,1).toUpperCase()).append("\" class=\"rounded-circle mb-3\" width=\"120\" height=\"120\" alt=\"Avatar\">");
            html.append("<h5>").append(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()).append("</h5>");
            html.append("<p class=\"text-muted\">@").append(user.getUsername()).append("</p>");
            html.append("</div>");
            html.append("<div class=\"col-md-8\">");
            html.append("<table class=\"table table-sm\">");
            html.append("<tr><td><strong>ID:</strong></td><td>").append(user.getId()).append("</td></tr>");
            html.append("<tr><td><strong>Email:</strong></td><td>").append(user.getEmail()).append("</td></tr>");
            html.append("<tr><td><strong>Email Confirmado:</strong></td><td>");
            html.append(user.isEmailConfirmed() ? "<span class='badge bg-success'>Sí</span>" : "<span class='badge bg-warning'>No</span>");
            html.append("</td></tr>");
            html.append("<tr><td><strong>Registrado:</strong></td><td>");
            html.append(user.getRegisteredAt() != null ? user.getRegisteredAt().toString() : "N/A");
            html.append("</td></tr>");
            html.append("<tr><td><strong>Roles:</strong></td><td>");
            html.append(user.getRoles().stream().map(role -> role.getName()).reduce((a, b) -> a + ", " + b).orElse("Sin roles"));
            html.append("</td></tr>");
            html.append("</table>");
            html.append("</div>");
            html.append("</div>");

            return html.toString();

        } catch (Exception e) {
            return "<div class='alert alert-danger'>Error: " + e.getMessage() + "</div>";
        }
    }
}
