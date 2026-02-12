package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    long countByUser_Id(Long userId);
    List<Review> findAllByUser_Id(Long userId);
    Page<Review> findAllByUser_Id(Long userId, Pageable pageable);
    List<Review> findByMovieId(Long movieId);
    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);
    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);
}
