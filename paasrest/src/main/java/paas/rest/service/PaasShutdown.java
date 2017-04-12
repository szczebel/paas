package paas.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.procman.JavaProcessManager;

import javax.annotation.PreDestroy;

@Component
public class PaasShutdown {

    @Autowired private JavaProcessManager processManager;

    @PreDestroy
    void shutdown() {
        Logger log = LoggerFactory.getLogger(getClass());
        log.warn("Commencing paas shutdown sequence");
        processManager.shutdown();
    }
}
