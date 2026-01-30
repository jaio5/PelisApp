package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Archivement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivementRepository extends JpaRepository<Archivement, Long> {
}
