package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.constants.AppConstants;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.entity.ReviewLike;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.ReviewLikeRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Servicio para gestión de reseñas de películas.
 * Implementa principios de código limpio y usa constantes centralizadas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ModeratingAI moderatingAI;
    private final UserService userService;

    @Transactional
    public Review createReview(Long userId, Long movieId, String text, int stars) {
        validateReviewInput(stars, text);

        User user = findUserById(userId);
        Movie movie = findMovieById(movieId);

        // Validación de moderación
        double aiScore = moderatingAI.analyzeText(text);
        if (aiScore < AppConstants.AI_REJECTION_THRESHOLD) {
            log.warn("Review rechazada por moderación para usuario ID: {} - Score: {}", userId, aiScore);
            throw new IllegalArgumentException(AppConstants.ERROR_REVIEW_TOO_TOXIC);
        }

        Review review = buildReview(user, movie, text, stars);
        Review savedReview = reviewRepository.save(review);

        // Actualizar logros del usuario de forma asíncrona
        userService.onUserPostedReview(user.getId());

        log.info("Nueva reseña creada - Usuario: {}, Película: {}, Estrellas: {}",
                user.getUsername(), movie.getTitle(), stars);

        return savedReview;
    }

    @Transactional
    public void likeReview(Long likerUserId, Long reviewId) {
        Review review = findReviewById(reviewId);
        User liker = findUserById(likerUserId);

        validateLikeOperation(liker, review);

        if (reviewLikeRepository.existsByUser_IdAndReview_Id(likerUserId, reviewId)) {
            log.debug("Usuario {} ya había dado like a la reseña {}", likerUserId, reviewId);
            return;
        }

        createReviewLike(liker, review);
        incrementLikesCount(review);

        // Actualizar logros del autor de la reseña
        userService.onUserReceivedLike(review.getUser().getId());

        log.info("Like añadido - Usuario: {}, Reseña: {}, Total likes: {}",
                liker.getUsername(), reviewId, review.getLikesCount());
    }

    /**
     * Valida la entrada de una reseña.
     */
    private void validateReviewInput(int stars, String text) {
        if (stars < AppConstants.MIN_STARS_RATING || stars > AppConstants.MAX_STARS_RATING) {
            throw new IllegalArgumentException(AppConstants.ERROR_INVALID_RATING);
        }

        if (text == null || text.trim().length() < AppConstants.MIN_REVIEW_TEXT_LENGTH) {
            throw new IllegalArgumentException("El texto de la reseña es demasiado corto");
        }

        if (text.length() > AppConstants.MAX_REVIEW_TEXT_LENGTH) {
            throw new IllegalArgumentException("El texto de la reseña es demasiado largo");
        }
    }

    /**
     * Busca un usuario por ID o lanza excepción.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", userId);
                    return new IllegalArgumentException(AppConstants.ERROR_USER_NOT_FOUND + ": " + userId);
                });
    }

    /**
     * Busca una película por ID o lanza excepción.
     */
    private Movie findMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.error("Película no encontrada: {}", movieId);
                    return new IllegalArgumentException(AppConstants.ERROR_MOVIE_NOT_FOUND + ": " + movieId);
                });
    }

    /**
     * Busca una reseña por ID o lanza excepción.
     */
    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("Reseña no encontrada: {}", reviewId);
                    return new IllegalArgumentException(AppConstants.ERROR_REVIEW_NOT_FOUND + ": " + reviewId);
                });
    }

    /**
     * Construye una nueva reseña.
     */
    private Review buildReview(User user, Movie movie, String text, int stars) {
        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setText(text);
        review.setStars(stars);
        review.setCreatedAt(Instant.now());
        review.setLikesCount(0L);
        return review;
    }

    /**
     * Valida que se pueda dar like a la reseña.
     */
    private void validateLikeOperation(User liker, Review review) {
        if (review.getUser().getId().equals(liker.getId())) {
            log.warn("Usuario {} intentó dar like a su propia reseña {}", liker.getId(), review.getId());
            throw new IllegalArgumentException("No puedes dar like a tu propia reseña");
        }
    }

    /**
     * Crea un nuevo like para la reseña.
     */
    private void createReviewLike(User liker, Review review) {
        ReviewLike like = new ReviewLike();
        like.setReview(review);
        like.setUser(liker);
        like.setCreatedAt(Instant.now());
        reviewLikeRepository.save(like);
    }

    /**
     * Incrementa el contador de likes de la reseña.
     */
    private void incrementLikesCount(Review review) {
        review.setLikesCount(review.getLikesCount() + 1);
        reviewRepository.save(review);
    }
}
