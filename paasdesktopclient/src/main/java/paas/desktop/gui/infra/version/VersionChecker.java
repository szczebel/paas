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
import java.io.File;
import java.net.URL;

import static paas.desktop.gui.ViewRequest.NEW_VERSION;
import static paas.desktop.remoting.RestCall.restGet;

@Component
public class VersionChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventBus eventBus;
    @Autowired
    private LoginData loginData;

    private Long selfLastModified;

    @PostConstruct
    void initialize() {
        this.selfLastModified = findSelfLastModified();
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
        if (selfLastModified == null) throw new Exception("Self last modified not available");
        long fromServer = restGet(serverUrl + Links.DESKTOP_CLIENT_LAST_MODIFIED, Long.class).execute();
        return fromServer > selfLastModified;
    }
    //todo: try to base it on buildNumber

    private Long findSelfLastModified() {
        try {
            URL jarLocation = VersionChecker.class.getProtectionDomain().getCodeSource().getLocation();
            String jarLocationString = jarLocation.toString();
            String jarFileName = jarLocationString.substring("jar:file:".length(), jarLocationString.indexOf("!"));
            File file = new File(jarFileName);
            if (!file.exists() || !file.isFile()) throw new Exception("Not a file: " + jarFileName);
            return file.lastModified();
        } catch (Exception e) {
            logger.error("Checking self last modified failed : ", e);
            return null;
        }
    }
}
