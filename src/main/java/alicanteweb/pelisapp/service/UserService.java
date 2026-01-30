package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Tag;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.ReviewLikeRepository;
import alicanteweb.pelisapp.repository.ReviewRepository;
import alicanteweb.pelisapp.repository.TagRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final RoleRepository roleRepository;
    private final TagRepository tagRepository;

    public UserService(UserRepository userRepository,
                       ReviewRepository reviewRepository,
                       ReviewLikeRepository reviewLikeRepository,
                       RoleRepository roleRepository,
                       TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.roleRepository = roleRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Called after a user posts a review. Grants badges for number of reviews posted.
     */
    @Transactional
    public void onUserPostedReview(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        long reviewCount = reviewRepository.countByUser_Id(userId);

        // Example rule: 10 reviews -> tag FIRST_10_REVIEWS
        if (reviewCount >= 10) {
            grantTagIfNotPresent(user, "FIRST_10_REVIEWS", "First 10 reviews");
        }

        userRepository.save(user);
    }

    /**
     * Called when one of the user's reviews receives a like.
     * Recalculates average likes per review and assigns roles based on thresholds.
     */
    @Transactional
    public void onUserReceivedLike(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));

        List<Long> reviewIds = reviewRepository.findAllByUser_Id(userId).stream().map(Review::getId).toList();

        if (reviewIds.isEmpty()) return;

        long totalLikes = reviewLikeRepository.countByReview_IdIn(reviewIds);

        double avgLikesPerReview = (double) totalLikes / reviewIds.size();

        int criticLevel = (int) Math.floor(avgLikesPerReview);
        user.setCriticLevel(criticLevel);

        // Example thresholds
        Optional<Role> criticRole = roleRepository.findByName("ROLE_CRITIC");
        if (avgLikesPerReview >= 3.0 && criticRole.isPresent()) {
            user.addRole(criticRole.get());
        }

        Optional<Role> topCritic = roleRepository.findByName("ROLE_TOP_CRITIC");
        if (avgLikesPerReview >= 5.0 && topCritic.isPresent()) {
            user.addRole(topCritic.get());
        }

        // Example badge for very liked users
        if (avgLikesPerReview >= 3.0) {
            grantTagIfNotPresent(user, "WELL_LIKED_CRITIC", "Well liked critic");
        }

        userRepository.save(user);
    }

    private void grantTagIfNotPresent(User user, String code, String name) {
        Tag tag = tagRepository.findByCode(code).orElseGet(() -> {
            Tag t = new Tag();
            t.setCode(code);
            t.setName(name);
            t.setCreatedAt(Instant.now());
            return tagRepository.save(t);
        });

        // Usa helper addTag en lugar de manipular la colección expuesta
        boolean present = user.getTags().stream().anyMatch(t -> t.getCode().equals(code));
        if (!present) {
            user.addTag(tag);
        }
    }
}
