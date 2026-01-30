package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.ComentaryModeration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComentaryModerationRepository extends JpaRepository<ComentaryModeration,Long> {

}
