package paas.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PaasStartup {

    @Autowired private HostingService hostingService;

    @PostConstruct
    void startup() {
        Logger log = LoggerFactory.getLogger(getClass());
        log.info("Commencing paas startup sequence");
        hostingService.redeployFromProcfiles();
    }
}
