package alicanteweb.pelisapp.repository;

import alicanteweb.pelisapp.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository  extends JpaRepository<Country,Long> {
}
