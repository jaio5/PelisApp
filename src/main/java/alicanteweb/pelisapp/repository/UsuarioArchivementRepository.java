package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.UsuarioArchivement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioArchivementRepository extends JpaRepository<UsuarioArchivement,Long> {

}
