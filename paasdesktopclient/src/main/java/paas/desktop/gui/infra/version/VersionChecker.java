package paas.desktop.gui.infra.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.MustBeInBackground;
import paas.desktop.gui.infra.security.LoginData;
import paas.shared.Links;
import swingutils.background.BackgroundOperation;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.jar.Manifest;

import static paas.desktop.gui.ViewRequest.NEW_VERSION;
import static paas.desktop.remoting.RestCall.restGet;
import static paas.shared.BuildTime.readBuildTime;

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
        eventBus.whenLoginChanged(this::checkVersion);
    }

    public void checkVersion() {
        BackgroundOperation.execute(
                this::isNewerVersionAvailable,
                yes -> {
                    if (yes) {
                        eventBus.requestView(NEW_VERSION);
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


    @SuppressWarnings("WeakerAccess") //private won't let AOP ensure that this @MustBeInBackground
    @MustBeInBackground
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
