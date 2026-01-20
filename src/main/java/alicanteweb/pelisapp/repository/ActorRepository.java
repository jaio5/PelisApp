package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Actor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface ActorRepository extends JpaRepository <Actor,Integer> {
    Optional<Actor> findByNombre(String nombre);
    Optional<Actor> findByTmdbId(Integer tmdbId);
}
