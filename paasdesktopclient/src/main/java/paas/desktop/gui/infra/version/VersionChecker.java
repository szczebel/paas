package paas.desktop.gui.infra.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.remoting.PaasRestClient;
import swingutils.background.BackgroundOperation;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.jar.Manifest;

import static paas.desktop.gui.ViewRequest.NEW_VERSION;
import static paas.shared.BuildTime.readBuildTime;

@Component
public class VersionChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventBus eventBus;
    @Autowired
    private PaasRestClient paasRestClient;

    private Long myBuildTimestamp;

    @SuppressWarnings("unused")
    @PostConstruct
    void initialize() {
        this.myBuildTimestamp = getMyBuildTimestamp();
        logger.info("My build timestamp : " + myBuildTimestamp);
        eventBus.when(Events.LOGIN_CHANGED, this::checkVersion);
    }

    public void checkVersion() {
        BackgroundOperation.execute(
                this::isNewerVersionAvailable,
                yes -> {
                    if (yes) {
                        eventBus.dispatch(NEW_VERSION);
                    } else {
                        logger.info("No new version detected");
                    }
                },
                ex -> {
                    Throwable t = ex;
                    while (t.getCause() != null) t = t.getCause();
                    logger.error("Checking version failed due to : ", t);
                }
        );
    }

    private boolean isNewerVersionAvailable() throws Exception {
        if (myBuildTimestamp == null) {
            logger.error("My build timestamp not available");
            return false;
        }
        long fromServer = paasRestClient.getDesktopClientBuildTime();
        return fromServer > myBuildTimestamp;
    }

    private Long getMyBuildTimestamp() {
        try (InputStream manifestStream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF")) {
            return readBuildTime(new Manifest(manifestStream));
        } catch (Exception e) {
            logger.error("Checking self last modified failed : ", e);
            return null;
        }
    }
}
