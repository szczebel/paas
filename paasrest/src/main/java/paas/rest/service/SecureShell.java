package paas.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.host.Shell;
import paas.procman.DatedMessage;

import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.List;

import static paas.rest.service.security.Role.ADMIN;

@Component
public class SecureShell {

    private Shell shell;

    public SecureShell(@Autowired FileSystemStorageService fileSystemStorageService) {
        shell = new Shell(System.getProperty("os.name").startsWith("Windows") ? "cmd" : "bash", fileSystemStorageService.getStorageRoot());
    }

    @RolesAllowed(ADMIN)
    public void execute(String command) throws IOException, InterruptedException {
        shell.execute(command);
    }

    @RolesAllowed(ADMIN)
    public List<DatedMessage> getOutputNewerThan(long timestamp) {
        return shell.getOutputNewerThan(timestamp);
    }

    @SuppressWarnings("unused")
    @PreDestroy
    public void cleanup() {
        Logger log = LoggerFactory.getLogger(getClass());
        shell.killShellProcess(log::info);
    }
}
