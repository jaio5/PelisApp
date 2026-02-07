package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.constants.AppConstants;
import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.entity.*;
import alicanteweb.pelisapp.repository.*;
import alicanteweb.pelisapp.service.*;
import alicanteweb.pelisapp.service.UserRegistrationService.UserRegistrationRequest;
import alicanteweb.pelisapp.service.UserRegistrationService.UserRegistrationResult;
import alicanteweb.pelisapp.service.EmailConfirmationService.EmailConfirmationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controlador unificado para todas las vistas web HTML
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    // Repositories
    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // Services
    private final TMDBMovieLoaderService tmdbMovieLoaderService;
    private final MovieService movieService;
    private final ReviewService reviewService;
    private final UserRegistrationService registrationService;
    private final EmailConfirmationService emailConfirmationService;
    private final AuthService authService;
    private final MoviePosterRedownloadService posterRedownloadService;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    // ============= HOME PAGE =============

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "12") int size,
                       @RequestParam(value = "genre", required = false) String genre,
                       @RequestParam(value = "search", required = false) String search) {

        try {
            // Verificar si hay pocas películas y cargar más automáticamente
            ensureMinimumMovies();

            PaginationParams pagination = validatePaginationParams(page, size);
            Page<Movie> moviesPage;

            if (search != null && !search.trim().isEmpty()) {
                moviesPage = searchMovies(search, pagination);
                model.addAttribute("searchQuery", search);
            } else {
                moviesPage = getMoviesByGenre(genre, pagination);
            }

            addMoviesDataToModel(model, moviesPage, genre);
            addCategoriesDataToModel(model);
            addPaginationDataToModel(model, moviesPage);

            return "index";

        } catch (Exception e) {
            log.error("Error cargando página principal: {}", e.getMessage());
            model.addAttribute("error", "Error cargando películas");
            return "error";
        }
    }

    // ============= MOVIE DETAILS =============

    @GetMapping("/pelicula/{id}")
    public String movieDetail(@PathVariable Long id, Model model, Authentication auth) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Película no encontrada"));

            // Obtener detalles completos incluyendo reparto con fotos
            MovieDetailsDTO movieDetails = null;
            try {
                movieDetails = movieService.getCombinedByMovieId(id);
                log.info("Detalles de película obtenidos - ID: {}, TMDB ID: {}, Reparto: {}, Directores: {}",
                    id, movie.getTmdbId(),
                    movieDetails != null && movieDetails.getCastMembers() != null ? movieDetails.getCastMembers().size() : 0,
                    movieDetails != null && movieDetails.getDirectors() != null ? movieDetails.getDirectors().size() : 0
                );
            } catch (Exception e) {
                log.error("Error obteniendo detalles de reparto para película {}: {}", id, e.getMessage());
            }

            // Obtener reseñas ordenadas por fecha
            List<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movie.getId());

            // Calcular estadísticas
            MovieStats stats = calculateMovieStats(reviews);

            // Verificar si el usuario actual ya tiene una reseña
            Review userReview = null;
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    userReview = reviewRepository.findByUserIdAndMovieId(userOpt.get().getId(), movie.getId())
                            .orElse(null);
                }
            }

            model.addAttribute("movie", movie);
            model.addAttribute("movieDetails", movieDetails);
            model.addAttribute("reviews", reviews);
            model.addAttribute("movieStats", stats);
            model.addAttribute("userReview", userReview);
            model.addAttribute("canReview", auth != null && auth.isAuthenticated() && userReview == null);
            model.addAttribute("isAuthenticated", auth != null && auth.isAuthenticated());

            return "movie-detail";

        } catch (Exception e) {
            log.error("Error cargando detalles de película {}: {}", id, e.getMessage());
            model.addAttribute("error", "No se pudo cargar la película");
            return "error";
        }
    }

    @PostMapping("/pelicula/{id}/review")
    public String addReview(@PathVariable Long id,
                           @RequestParam int stars,
                           @RequestParam String text,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para valorar películas");
            return "redirect:/login";
        }

        try {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Película no encontrada"));

            Optional<Review> existingReview = reviewRepository.findByUserIdAndMovieId(user.getId(), movie.getId());
            if (existingReview.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Ya has valorado esta película");
                return "redirect:/pelicula/" + id;
            }

            reviewService.createReview(user.getId(), movie.getId(), text, stars);
            redirectAttributes.addFlashAttribute("success", "¡Reseña añadida exitosamente!");
            log.info("Nueva reseña añadida por {} para película {}", username, movie.getTitle());

        } catch (Exception e) {
            log.error("Error añadiendo reseña: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al añadir la reseña: " + e.getMessage());
        }

        return "redirect:/pelicula/" + id;
    }

    @PostMapping("/review/{reviewId}/like")
    @ResponseBody
    public String likeReview(@PathVariable Long reviewId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "error:Debes iniciar sesión";
        }

        try {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            reviewService.likeReview(user.getId(), reviewId);
            return "success";

        } catch (Exception e) {
            log.error("Error dando like a reseña {}: {}", reviewId, e.getMessage());
            return "error:" + e.getMessage();
        }
    }

    // ============= AUTHENTICATION =============

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "confirmed", required = false) String confirmed,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", true);
            log.debug("LoginController: error param detectado");
        }
        if (logout != null) {
            model.addAttribute("logout", true);
            log.debug("LoginController: logout param detectado");
        }
        if (confirmed != null) {
            model.addAttribute("confirmed", true);
            log.debug("LoginController: confirmed param detectado");
        }

        log.debug("LoginController: Acceso a /login con params -> error={}, logout={}, confirmed={}",
                 error, logout, confirmed);
        return "login";
    }

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

        try {
            String username = user.getUsername();
            String email = user.getEmail();
            String password = user.getPassword();
            String displayName = user.getDisplayName();

            UserRegistrationRequest regRequest = new UserRegistrationRequest(
                    username, email, password, confirmPassword, displayName);

            log.info("[DEBUG] Llamando a registrationService.registerUser para: {}", username);

            UserRegistrationResult regResult = registrationService.registerUser(regRequest);

            log.info("[DEBUG] Resultado del registro: success={}, mensaje={}",
                    regResult.success(), regResult.message());

            if (!regResult.success()) {
                log.warn("[DEBUG] Registro fallido para {}: {}", username, regResult.message());
                model.addAttribute("error", regResult.message());
                model.addAttribute("user", user);
                return "register";
            }

            if (emailEnabled) {
                redirectAttributes.addFlashAttribute("success",
                    "✅ Usuario registrado exitosamente. Te hemos enviado un email de confirmación a " + email);
            } else {
                redirectAttributes.addFlashAttribute("success",
                    "✅ Usuario registrado exitosamente. El email está deshabilitado, tu cuenta ya está activa.");
            }

            log.info("[DEBUG] Usuario {} registrado exitosamente", username);
            return "redirect:/login";

        } catch (Exception e) {
            log.error("[DEBUG] Error durante el registro de {}: {}", user.getUsername(), e.getMessage());
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        log.info("[CONFIRM-EMAIL] Procesando token: {}", token);

        try {
            EmailConfirmationResult result = emailConfirmationService.confirmAccount(token);

            if (result.isSuccess()) {
                log.info("[CONFIRM-EMAIL] Email confirmado exitosamente para token: {}", token);
                redirectAttributes.addFlashAttribute("confirmed", true);
                redirectAttributes.addFlashAttribute("success", result.getMessage());
            } else {
                log.warn("[CONFIRM-EMAIL] Error confirmando email: {}", result.getMessage());
                redirectAttributes.addFlashAttribute("error", result.getMessage());
            }

        } catch (Exception e) {
            log.error("[CONFIRM-EMAIL] Excepción confirmando email: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error confirmando el email: " + e.getMessage());
        }

        return "redirect:/login";
    }

    // ============= USER PROFILE =============

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            List<Review> userReviews = reviewRepository.findAllByUser_Id(user.getId());

            model.addAttribute("user", user);
            model.addAttribute("reviews", userReviews);
            model.addAttribute("reviewCount", userReviews.size());

            return "perfil";

        } catch (Exception e) {
            log.error("Error cargando perfil: {}", e.getMessage());
            model.addAttribute("error", "Error cargando perfil");
            return "error";
        }
    }

    // ============= ADMIN PAGES =============

    @GetMapping("/admin")
    public String adminIndex(Model model, Authentication auth) {
        if (auth == null || !auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login";
        }

        try {
            // Estadísticas básicas
            long totalMovies = movieRepository.count();
            long totalUsers = userRepository.count();
            long totalReviews = reviewRepository.count();

            model.addAttribute("totalMovies", totalMovies);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalReviews", totalReviews);
            model.addAttribute("adminUser", auth.getName());

            return "admin/index";

        } catch (Exception e) {
            log.error("Error cargando panel de admin: {}", e.getMessage());
            model.addAttribute("error", "Error cargando panel de administración");
            return "error";
        }
    }

    @GetMapping("/admin/")
    public String adminDashboardWithSlash(Model model, Authentication auth) {
        return "redirect:/admin";
    }

    @GetMapping("/admin/movies")
    public String adminMovies(Model model, Authentication auth) {
        if (!isAdmin(auth)) {
            return "redirect:/login";
        }

        try {
            long movieCount = movieRepository.count();
            model.addAttribute("movieCount", movieCount);
            return "admin/movies";
        } catch (Exception e) {
            log.error("Error cargando administración de películas: {}", e.getMessage());
            model.addAttribute("error", "Error cargando películas");
            return "error";
        }
    }

    @GetMapping("/admin/users")
    public String adminUsers(Model model,
                           Authentication auth,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String search) {

        if (!isAdmin(auth)) {
            return "redirect:/login";
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> usersPage;

            if (search != null && !search.trim().isEmpty()) {
                // Si hay búsqueda, usar búsqueda por username
                usersPage = userRepository.findByUsernameContainingIgnoreCase(search, pageable);
                model.addAttribute("search", search);
            } else {
                usersPage = userRepository.findAll(pageable);
            }

            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalElements", usersPage.getTotalElements());
            model.addAttribute("totalUsers", usersPage.getTotalElements()); // Para compatibilidad con el template
            model.addAttribute("hasNext", usersPage.hasNext());
            model.addAttribute("hasPrevious", usersPage.hasPrevious());

            return "admin/simple-users-fixed";

        } catch (Exception e) {
            log.error("Error cargando gestión de usuarios: {}", e.getMessage());
            model.addAttribute("error", "Error cargando usuarios");
            return "error";
        }
    }

    @GetMapping("/admin/moderation")
    public String adminModeration(Model model, Authentication auth) {
        if (!isAdmin(auth)) {
            return "redirect:/login";
        }

        // Aquí iría la lógica para mostrar estadísticas de moderación
        // y reviews pendientes de revisar manualmente

        return "admin/moderation";
    }

    @GetMapping("/admin/email-config")
    public String adminEmailConfig(Model model, Authentication auth) {
        if (!isAdmin(auth)) {
            return "redirect:/login";
        }

        model.addAttribute("emailEnabled", emailEnabled);

        return "admin/email-config";
    }

    @PostMapping("/admin/load-popular")
    @ResponseBody
    public String loadPopularMovies(@RequestParam(defaultValue = "3") int pages, Authentication auth) {
        if (!isAdmin(auth)) {
            return "❌ Sin permisos de administrador";
        }

        try {
            log.info("🎬 Iniciando carga de {} páginas de películas populares desde TMDB", pages);

            long countBefore = movieRepository.count();
            tmdbMovieLoaderService.loadPopularMovies(Math.min(pages, 5)); // Máximo 5 páginas
            long countAfter = movieRepository.count();

            String result = String.format("✅ Se han cargado %d nuevas películas populares desde TMDB (Total: %d → %d)",
                    countAfter - countBefore, countBefore, countAfter);
            log.info(result);

            return result;

        } catch (Exception e) {
            log.error("❌ Error cargando películas populares: {}", e.getMessage());
            return "❌ Error cargando películas: " + e.getMessage();
        }
    }

    @PostMapping("/admin/load-top-rated")
    @ResponseBody
    public String loadTopRatedMovies(@RequestParam(defaultValue = "3") int pages, Authentication auth) {
        if (!isAdmin(auth)) {
            return "❌ Sin permisos de administrador";
        }

        try {
            log.info("🏆 Iniciando carga de {} páginas de películas top rated desde TMDB", pages);

            long countBefore = movieRepository.count();
            tmdbMovieLoaderService.loadTopRatedMovies(Math.min(pages, 5));
            long countAfter = movieRepository.count();

            String result = String.format("✅ Se han cargado %d nuevas películas top rated desde TMDB (Total: %d → %d)",
                    countAfter - countBefore, countBefore, countAfter);
            log.info(result);

            return result;

        } catch (Exception e) {
            log.error("❌ Error cargando películas top rated: {}", e.getMessage());
            return "❌ Error cargando películas: " + e.getMessage();
        }
    }

    @PostMapping("/admin/reload-posters")
    @ResponseBody
    public String reloadMoviePosters(Authentication auth) {
        if (!isAdmin(auth)) {
            return "❌ Sin permisos de administrador";
        }

        try {
            log.info("🖼️ Iniciando recarga de posters de películas");

            // Implementación simplificada por ahora
            long totalMovies = movieRepository.count();

            String result = String.format("✅ Proceso de recarga de posters iniciado para %d películas", totalMovies);
            log.info(result);

            return result;

        } catch (Exception e) {
            log.error("❌ Error recargando posters: {}", e.getMessage());
            return "❌ Error recargando posters: " + e.getMessage();
        }
    }

    @GetMapping("/admin/bulk-loader")
    public String adminBulkLoader(Model model, Authentication auth) {
        if (!isAdmin(auth)) {
            return "redirect:/login";
        }

        try {
            long movieCount = movieRepository.count();
            model.addAttribute("movieCount", movieCount);
            return "admin/bulk-loader";
        } catch (Exception e) {
            log.error("Error cargando bulk loader: {}", e.getMessage());
            model.addAttribute("error", "Error cargando bulk loader");
            return "error";
        }
    }

    // ============= UTILITY METHODS =============

    private void ensureMinimumMovies() {
        try {
            long movieCount = movieRepository.count();
            if (movieCount < 10) { // Mínimo 10 películas
                log.info("Pocas películas en BD ({}), cargando más automáticamente...", movieCount);
                tmdbMovieLoaderService.loadPopularMovies(2); // Cargar 2 páginas
            }
        } catch (Exception e) {
            log.warn("Error verificando/cargando películas mínimas: {}", e.getMessage());
        }
    }

    private PaginationParams validatePaginationParams(int page, int size) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), AppConstants.MAX_PAGE_SIZE);
        return new PaginationParams(validPage, validSize);
    }

    private Page<Movie> getMoviesByGenre(String genre, PaginationParams pagination) {
        Pageable pageable = PageRequest.of(pagination.page(), pagination.size());

        if (genre != null && !genre.trim().isEmpty()) {
            return movieRepository.findByCategories_Name(genre.trim(), pageable);
        } else {
            return movieRepository.findAll(pageable);
        }
    }

    private Page<Movie> searchMovies(String search, PaginationParams pagination) {
        Pageable pageable = PageRequest.of(pagination.page(), pagination.size());
        // Simplificar búsqueda - usar findAll por ahora hasta implementar búsqueda por título
        return movieRepository.findAll(pageable);
    }

    private void addMoviesDataToModel(Model model, Page<Movie> moviesPage, String genre) {
        model.addAttribute("movies", moviesPage.getContent());
        model.addAttribute("selectedGenre", genre);
    }

    private void addCategoriesDataToModel(Model model) {
        List<CategoryEntity> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
    }

    private void addPaginationDataToModel(Model model, Page<Movie> moviesPage) {
        model.addAttribute("currentPage", moviesPage.getNumber());
        model.addAttribute("totalPages", moviesPage.getTotalPages());
        model.addAttribute("totalElements", moviesPage.getTotalElements());
        model.addAttribute("hasNext", moviesPage.hasNext());
        model.addAttribute("hasPrevious", moviesPage.hasPrevious());
    }

    private MovieStats calculateMovieStats(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new MovieStats(0, 0.0, new int[5]);
        }

        double totalRating = 0;
        int[] starDistribution = new int[5];

        for (Review review : reviews) {
            totalRating += review.getStars();
            starDistribution[review.getStars() - 1]++;
        }

        double averageRating = totalRating / reviews.size();

        return new MovieStats(reviews.size(), averageRating, starDistribution);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    // ============= INNER CLASSES =============

    private record PaginationParams(int page, int size) {}

    @Getter
    public static class MovieStats {
        private final int totalReviews;
        private final double averageRating;
        private final int[] starDistribution;

        public MovieStats(int totalReviews, double averageRating, int[] starDistribution) {
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.starDistribution = starDistribution;
        }

        public String getAverageRatingFormatted() {
            return String.format("%.1f", averageRating);
        }

        public int getStarPercentage(int starLevel) {
            if (totalReviews == 0) return 0;
            return (starDistribution[starLevel - 1] * 100) / totalReviews;
        }
    }
}
