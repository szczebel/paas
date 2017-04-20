package paas.rest.service.logging;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppsOutputForwarder {

    public void forward(long appId, String outputLine) {
        LoggerFactory.getLogger("HostedAppLogger.appId." + appId).info(outputLine);
    }
}
