package paas.rest.service.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppsOutputForwarder {

    private static final Logger log = LoggerFactory.getLogger(AppsOutputForwarder.class);

    public void forward(long appId, String outputLine) {
        log.debug("I want to forward this to ELK: {AppID:"+appId+"} " + outputLine);

        //todo implement me
    }
}
