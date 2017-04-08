package paas.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.host.Shell;
import paas.procman.HostedAppManager;

import javax.annotation.PreDestroy;

@Component
public class Shutdown {

    @Autowired private HostedAppManager hostedAppManager;
    @Autowired private Shell shell;

    @PreDestroy
    void shutdown() {
        hostedAppManager.shutdown();
        shell.killShellProcess();
    }
}
