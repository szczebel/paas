package paas.rest.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginEndpoint {

    @GetMapping("/login")
    public String login() {
        return "OK";//this is just a 'ping' service, security filter handles the actual credential check
    }
}
