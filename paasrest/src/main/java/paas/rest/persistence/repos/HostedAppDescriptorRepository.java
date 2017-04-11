package paas.rest.persistence.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paas.rest.persistence.entities.HostedAppDescriptor;

@Repository
public interface HostedAppDescriptorRepository extends JpaRepository<HostedAppDescriptor, Long> {
}
