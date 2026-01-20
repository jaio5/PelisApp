package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Director;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface DirectorRepository extends JpaRepository<Director,Integer> {
    Optional<Director> findByNombre(String nombre);
    Optional<Director> findByTmdbId(Integer tmdbId);
}
