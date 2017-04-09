package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import paas.host.Shell;
import paas.procman.DatedMessage;

import java.io.IOException;
import java.util.List;

@RestController
public class ShellEndpoint {

    @Autowired
    private Shell shell;

    @PostMapping("/executeShellCommand")
    public String executeHostCommand(@RequestParam String command) throws IOException, InterruptedException {
        shell.execute(command);
        return "Executed " + command;
    }

    @GetMapping("/getShellOutput")
    public List<DatedMessage> getShellOutput(@RequestParam(required = false) long timestamp) {
        return shell.getOutputNewerThan(timestamp);
    }

}
