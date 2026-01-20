package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Etiqueta;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta,Integer> {
    Optional<Etiqueta> findByClave(String clave);
}

