package paas.rest.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.rest.persistence.repos.HostedAppDescriptorRepository;

import java.security.Principal;

@Component
public class OwnershipChecker {

    @Autowired
    private HostedAppDescriptorRepository hostedAppDescriptorRepository;

    public boolean isPrincipalOwnerOfAppId(Principal principal, long appId) {
        return hostedAppDescriptorRepository
                .findOne(appId)
                .getOwner()
                .equals(principal.getName());
    }
}
