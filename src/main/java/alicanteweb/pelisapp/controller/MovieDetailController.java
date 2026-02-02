package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.MovieDetailsDTO;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.service.MovieService;
import alicanteweb.pelisapp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la página de detalles de película
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MovieDetailController {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;
    private final ReviewService reviewService;

    @GetMapping("/pelicula/{id}")
    public String movieDetail(@PathVariable Long id, Model model, Authentication auth) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Película no encontrada"));

            // Obtener reseñas ordenadas por fecha (más recientes primero)
            List<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movie.getId());

            // Calcular estadísticas de valoración
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

            // Añadir datos al modelo
            model.addAttribute("movie", movie);
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

            // Verificar que el usuario no tenga ya una reseña
            Optional<Review> existingReview = reviewRepository.findByUserIdAndMovieId(user.getId(), movie.getId());
            if (existingReview.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Ya has valorado esta película");
                return "redirect:/pelicula/" + id;
            }

            // Crear nueva reseña
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

    /**
     * Calcula estadísticas de valoración de una película
     */
    private MovieStats calculateMovieStats(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new MovieStats(0, 0.0, new int[5]);
        }

        double totalRating = 0;
        int[] starDistribution = new int[5]; // Índices 0-4 para estrellas 1-5

        for (Review review : reviews) {
            totalRating += review.getStars();
            starDistribution[review.getStars() - 1]++; // Convertir 1-5 a 0-4
        }

        double averageRating = totalRating / reviews.size();

        return new MovieStats(reviews.size(), averageRating, starDistribution);
    }

    /**
     * Clase para almacenar estadísticas de valoración
     */
    public static class MovieStats {
        private final int totalReviews;
        private final double averageRating;
        private final int[] starDistribution;

        public MovieStats(int totalReviews, double averageRating, int[] starDistribution) {
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.starDistribution = starDistribution;
        }

        public int getTotalReviews() { return totalReviews; }
        public double getAverageRating() { return averageRating; }
        public int[] getStarDistribution() { return starDistribution; }

        public String getAverageRatingFormatted() {
            return String.format("%.1f", averageRating);
        }

        public int getStarPercentage(int starLevel) {
            if (totalReviews == 0) return 0;
            return (starDistribution[starLevel - 1] * 100) / totalReviews;
        }
    }
}
