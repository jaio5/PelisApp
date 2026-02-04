package alicanteweb.pelisapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para vistas adicionales del panel de administración
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminViewController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDashboard(Model model) {
        log.info("Loading admin dashboard");

        // Basic data for dashboard
        model.addAttribute("movieCount", 0); // TODO: Get from repository
        model.addAttribute("userCount", 0);  // TODO: Get from repository

        return "admin/dashboard";
    }

    @GetMapping("/system-config")
    @PreAuthorize("hasRole('ADMIN')")
    public String showSystemConfig(Model model) {
        log.info("Loading system configuration page");

        // Add system configuration data
        model.addAttribute("appName", "PelisApp");
        model.addAttribute("appVersion", "1.0.0");
        model.addAttribute("environment", "Development");

        return "admin/system-config";
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String showReports(Model model) {
        log.info("Loading reports and analytics page");

        // Add basic analytics data
        model.addAttribute("totalUsers", 1247);
        model.addAttribute("activeUsers", 892);
        model.addAttribute("totalMovies", 3456);
        model.addAttribute("totalReviews", 8923);

        return "admin/reports";
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public String showModeration(Model model) {
        log.info("Loading moderation panel");

        // Add moderation statistics
        model.addAttribute("pendingReviews", 12);
        model.addAttribute("flaggedContent", 3);
        model.addAttribute("approvedToday", 8);

        return "admin/moderation";
    }

    @GetMapping("/bulk-loader")
    @PreAuthorize("hasRole('ADMIN')")
    public String showBulkLoader(Model model) {
        log.info("Loading bulk loader page");

        // Add current movie count for display
        model.addAttribute("currentMovieCount", 0); // TODO: Get from repository

        return "admin/bulk-loader";
    }

    @GetMapping("/email-config")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEmailConfig(Model model) {
        log.info("Loading email configuration page");

        // Add email configuration status
        model.addAttribute("emailEnabled", true);
        model.addAttribute("smtpHost", "smtp.gmail.com");
        model.addAttribute("smtpPort", 587);

        return "admin/email-config";
    }
}
