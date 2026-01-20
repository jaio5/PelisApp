package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Pelicula;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula,Integer> {
    Optional<Pelicula> findByTmdbId(Integer tmdbId);
}
