package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula,Integer> {
}
