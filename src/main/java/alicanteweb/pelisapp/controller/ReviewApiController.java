package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ReviewCreateRequest;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {
    private final ReviewService reviewService;

    @PostMapping("")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewCreateRequest req) {
        Review review = reviewService.createReview(req.getUserId(), req.getMovieId(), req.getText(), req.getStars());
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeReview(@PathVariable("id") Long reviewId, @RequestParam("userId") Long userId) {
        reviewService.likeReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }
}
