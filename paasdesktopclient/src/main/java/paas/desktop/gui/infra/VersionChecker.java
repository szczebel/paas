package paas.desktop.gui.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.security.LoginData;
import paas.shared.Links;
import swingutils.background.BackgroundOperation;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static paas.desktop.remoting.RestCall.restGet;
import static swingutils.components.ComponentFactory.hyperlinkButton;
import static swingutils.components.ComponentFactory.label;
import static swingutils.layout.LayoutBuilders.borderLayout;

@Component
public class VersionChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventBus eventBus;
    @Autowired
    private LoginData loginData;

    private JFrame owner;
    private Long selfLastModified;

    //todo: break dependency on JFrame
    public void initialize(JFrame owner) {
        this.owner = owner;
        this.selfLastModified = findSelfLastModified();
        eventBus.whenLoginChanged(this::checkVersion);
        checkVersion();
    }

    private void checkVersion() {
        String serverUrl = loginData.getServerUrl();
        BackgroundOperation.execute(
                () -> isNewerVersionAvailable(serverUrl),
                yes -> {
                    if (yes) {
                        tellUserAboutNewVersion(serverUrl);
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

    private void tellUserAboutNewVersion(String serverUrl) {
        JOptionPane.showMessageDialog(owner,
                borderLayout()
                        .center(label("Newer version is available @ " + serverUrl))
                        .south(hyperlinkButton("Download it now!", () -> openBrowser(serverUrl)))
                        .build(),
                "Newer version available",
                JOptionPane.INFORMATION_MESSAGE
                );
    }

    private void openBrowser(String serverUrl) {
        try {
            Desktop.getDesktop().browse(new URI(serverUrl + Links.PAAS_DESKTOP_CLIENT_JAR));
        } catch (IOException | URISyntaxException e) {
            logger.warn("Could not open browser", e);
        }
    }

    @SuppressWarnings("WeakerAccess") //private won't let AOP ensure that this @MustBeInBackground
    @MustBeInBackground
    protected boolean isNewerVersionAvailable(String serverUrl) throws Exception {
        if(selfLastModified == null) throw new Exception("Self last modified not available");
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
