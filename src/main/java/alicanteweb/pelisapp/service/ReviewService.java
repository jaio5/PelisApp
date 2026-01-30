package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.entity.ReviewLike;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.ReviewLikeRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ModeratingAI moderatingAI;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewLikeRepository reviewLikeRepository,
                         UserRepository userRepository,
                         MovieRepository movieRepository,
                         ModeratingAI moderatingAI,
                         UserService userService) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.moderatingAI = moderatingAI;
        this.userService = userService;
    }

    @Transactional
    public Review createReview(Long userId, Long movieId, String text, int stars) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("stars must be 1..5");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("movie not found"));

        // Moderation check (synchronous local AI)
        double aiScore = moderatingAI.analyzeText(text);
        if (aiScore < 0.2) { // threshold: too toxic
            throw new IllegalArgumentException("Review rejected by moderation");
        }

        Review r = new Review();
        r.setUser(user);
        r.setMovie(movie);
        r.setText(text);
        r.setStars(stars);
        r.setCreatedAt(Instant.now());
        r.setLikesCount(0L);

        Review saved = reviewRepository.save(r);

        // After saving, update user's achievements/level asynchronously via service
        userService.onUserPostedReview(user.getId());

        return saved;
    }

    @Transactional
    public void likeReview(Long likerUserId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("review not found"));
        User liker = userRepository.findById(likerUserId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (review.getUser().getId().equals(likerUserId)) {
            throw new IllegalArgumentException("cannot like your own review");
        }

        // Check existing like by this user
        boolean exists = reviewLikeRepository.existsByUser_IdAndReview_Id(likerUserId, reviewId);
        if (exists) {
            return;
        }

        ReviewLike like = new ReviewLike();
        like.setReview(review);
        like.setUser(liker);
        like.setCreatedAt(Instant.now());

        reviewLikeRepository.save(like);

        // increment likesCount
        review.setLikesCount(review.getLikesCount() + 1);
        reviewRepository.save(review);

        // Recalculate author's critic level / achievements
        userService.onUserReceivedLike(review.getUser().getId());
    }
}
