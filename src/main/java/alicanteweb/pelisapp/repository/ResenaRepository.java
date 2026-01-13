package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaRepository extends JpaRepository<Resena,Integer> {
}
