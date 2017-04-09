package paas.rest.persistence.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paas.rest.persistence.entities.Procfile;

import java.util.Optional;

@Repository
public interface ProcfileRepository extends JpaRepository<Procfile, Long> {

    Optional<Procfile> findByJarFileName(String jarFileName);
}
