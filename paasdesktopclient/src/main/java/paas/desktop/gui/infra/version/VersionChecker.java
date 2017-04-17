package paas.desktop.gui.infra.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.MustNotBeInEDT;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.gui.infra.security.LoginData;
import paas.shared.Links;
import swingutils.background.BackgroundOperation;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.jar.Manifest;

import static paas.desktop.gui.ViewRequest.NEW_VERSION;
import static paas.shared.BuildTime.readBuildTime;
import static restcall.RestCall.restGet;

@Component
public class VersionChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventBus eventBus;
    @Autowired
    private LoginData loginData;

    private Long myBuildTimestamp;

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


    @SuppressWarnings("WeakerAccess") //private won't let AOP ensure that this @MustNotBeInEDT
    @MustNotBeInEDT
    protected boolean isNewerVersionAvailable() throws Exception {
        String serverUrl = loginData.getServerUrl();
        if (myBuildTimestamp == null) {
            logger.error("My build timestamp not available");
            return false;
        }
        long fromServer = restGet(serverUrl + Links.DESKTOP_CLIENT_BUILD_TIMESTAMP, Long.class).execute();
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
