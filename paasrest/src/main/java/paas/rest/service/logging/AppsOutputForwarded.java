package paas.rest.service.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppsOutputForwarded {

    private static final Logger log = LoggerFactory.getLogger(AppsOutputForwarded.class);

    public void forward(String outputLine) {
        log.debug("I want to forward this to ELK: " + outputLine);
    }
}
