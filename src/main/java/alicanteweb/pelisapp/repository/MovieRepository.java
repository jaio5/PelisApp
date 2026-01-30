package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie,Long> {
    Optional<Movie> findByTmdbId(Long tmdbId);
    Page<Movie> findAll(Pageable pageable);
    Page<Movie> findByCategories_Name(String name, Pageable pageable);
}
