package paas.rest.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

import static paas.shared.Links.REGISTER;
import static paas.shared.Links.WHOAMI;

@RestController
public class LoginEndpoint {

    @GetMapping(WHOAMI)
    @RolesAllowed({"USER", "ADMIN"})
    public Principal login(Principal principal) {
        return principal;
    }

    @PostMapping(REGISTER)
    public void register(@RequestParam String username, @RequestParam String password) {
        throw new RuntimeException("Not implemented yet, use guest account for now");
    }
}
