package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike,Long> {
    boolean existsByUser_IdAndReview_Id(Long userId, Long reviewId);
    long countByReview_User_Id(Long userId);
    long countByReview_IdIn(List<Long> reviewIds);
    List<ReviewLike> findAllByReview_IdIn(List<Long> reviewIds);
}
