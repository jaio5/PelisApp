package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoríaRepository extends JpaRepository <Categoria,Integer> {
}
