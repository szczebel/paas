package paas.rest.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import paas.rest.persistence.repos.HostedAppDescriptorRepository;

@Component
public class OwnershipChecker {

    @Autowired
    private HostedAppDescriptorRepository hostedAppDescriptorRepository;

    public boolean isPrincipalOwnerOfAppId(Authentication principal, long appId) {
        return hostedAppDescriptorRepository
                .findOne(appId)
                .getOwner()
                .equals(principal.getName());
    }
}
