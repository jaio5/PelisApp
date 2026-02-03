package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie,Long> {
    Optional<Movie> findByTmdbId(Long tmdbId);
    Optional<Movie> findByTitle(String title);
    Page<Movie> findAll(Pageable pageable);
    Page<Movie> findByCategories_Name(String name, Pageable pageable);

    /**
     * Encuentra una película por ID cargando explícitamente actores y directores
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
           "LEFT JOIN FETCH m.actors " +
           "LEFT JOIN FETCH m.directors " +
           "LEFT JOIN FETCH m.categories " +
           "WHERE m.id = :id")
    Optional<Movie> findByIdWithCastAndDirectors(@Param("id") Long id);
}
