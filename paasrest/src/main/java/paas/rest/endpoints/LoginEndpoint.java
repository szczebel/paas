package paas.rest.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import paas.shared.Links;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

@RestController
public class LoginEndpoint {

    @GetMapping(Links.WHOAMI)
    @RolesAllowed({"USER", "ADMIN"})
    public Principal login(Principal principal) {
        return principal;
    }
}
