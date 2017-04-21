package paas.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PaasStartup implements ApplicationRunner{

    @Autowired private HostingService hostingService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Logger log = LoggerFactory.getLogger(getClass());
        log.info("Commencing paas startup sequence");
        hostingService.redeployFromProcfiles();
    }
}
