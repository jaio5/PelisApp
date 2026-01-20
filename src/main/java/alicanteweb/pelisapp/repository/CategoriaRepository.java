package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Categoria;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria,Integer> {
    Optional<Categoria> findByNombre(String nombre);
}
