package afpa.mra.repositories;

import afpa.mra.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SessionRepository extends JpaRepository<Session,Long> {

}

