package paas.rest.persistence.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paas.rest.persistence.entities.PaasUser;

@Repository
public interface PaasUserRepository extends JpaRepository<PaasUser, String> {
}
