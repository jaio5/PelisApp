package alicanteweb.pelisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import alicanteweb.pelisapp.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
}
