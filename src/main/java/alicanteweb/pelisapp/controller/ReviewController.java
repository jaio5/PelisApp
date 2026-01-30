package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ReviewCreateRequest;
import alicanteweb.pelisapp.entity.Review;
import alicanteweb.pelisapp.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> create(@Valid @RequestBody ReviewCreateRequest req) {
        Review r = reviewService.createReview(req.getUserId(), req.getMovieId(), req.getText(), req.getStars());
        return ResponseEntity.ok(r);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable("id") Long reviewId, @RequestParam("userId") Long userId) {
        reviewService.likeReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }
}
