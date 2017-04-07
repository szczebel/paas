package paas.desktop.gui.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    @Autowired
    private EventBus eventBus;
    @Value("${tiniestpaas.server.url}")
    private String initialServerUrl;

    private JFrame owner;
    private Long selfLastModified;

    public void initialize(JFrame owner) {
        this.owner = owner;
        this.selfLastModified = findSelfLastModified();
        eventBus.whenServerChanged(this::checkVersion);
        checkVersion(initialServerUrl);
    }

    private void checkVersion(String serverUrl) {
        BackgroundOperation.execute(
                () -> isNewerVersionAvailable(serverUrl),
                yes -> {
                    if (yes) {
                        tellUserAboutNewVersion(serverUrl);
                    } else {
                        System.out.println("No new version detected");
                    }
                },
                ex -> {
                    Throwable t = ex;
                    while (t.getCause() != null) t = t.getCause();
                    System.out.println("Checking version failed due to:");
                    t.printStackTrace();
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
            Desktop.getDesktop().browse(new URI(serverUrl + "/PaasDesktopClient.jar"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("WeakerAccess") //private won't let AOP ensure that this @MustBeInBackground
    @MustBeInBackground
    protected boolean isNewerVersionAvailable(String serverUrl) throws Exception {
        if(selfLastModified == null) throw new Exception("Self last modified not accessible");
        long fromServer = restGet(serverUrl + "/desktopClientLastModified", Long.class).execute();
        return fromServer > selfLastModified;
    }

    private Long findSelfLastModified() {
        try {
            URL jarLocation = VersionChecker.class.getProtectionDomain().getCodeSource().getLocation();
            String jarLocationString = jarLocation.toString();
            String jarFileName = jarLocationString.substring("jar:file:".length(), jarLocationString.indexOf("!"));
            File file = new File(jarFileName);
            if (!file.exists() || !file.isFile()) throw new Exception("Not a file: " + jarFileName);
            return file.lastModified();
        } catch (Exception e) {
            System.out.println("Checking self last modified failed:");
            e.printStackTrace();
            return null;
        }
    }
}
