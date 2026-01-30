package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    long countByUser_Id(Long userId);
    List<Review> findAllByUser_Id(Long userId);
}
