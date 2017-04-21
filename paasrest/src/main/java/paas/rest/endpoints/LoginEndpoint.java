package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import paas.rest.service.security.UserService;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

import static paas.rest.service.security.Role.ADMIN;
import static paas.rest.service.security.Role.USER;
import static paas.shared.Links.REGISTER;
import static paas.shared.Links.WHOAMI;

@RestController
public class LoginEndpoint {

    @GetMapping(WHOAMI)
    @RolesAllowed({USER, ADMIN})
    public Principal whoamI(Principal principal) {
        return principal;
    }

    @PostMapping(REGISTER)
    public void register(@RequestParam String username, @RequestParam String password) {
        userService.create(username, passwordEncoder.encode(password));
    }

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
}
