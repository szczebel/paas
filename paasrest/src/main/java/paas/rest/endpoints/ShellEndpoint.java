package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import paas.procman.DatedMessage;
import paas.rest.service.SecureShell;
import paas.shared.Links;

import java.io.IOException;
import java.util.List;

@RestController
public class ShellEndpoint {

    @Autowired
    private SecureShell secureShell;

    @PostMapping(Links.ADMIN_EXECUTE_SHELL_COMMAND)
    public String executeHostCommand(@RequestParam String command) throws IOException, InterruptedException {
        secureShell.execute(command);
        return "Executed " + command;
    }

    @GetMapping(Links.ADMIN_GET_SHELL_OUTPUT)
    public List<DatedMessage> getShellOutput(@RequestParam(required = false) long timestamp) {
        return secureShell.getOutputNewerThan(timestamp);
    }

}
