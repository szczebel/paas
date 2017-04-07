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

    public void initialize(JFrame owner) {
        this.owner = owner;
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
                        System.err.println("No new version detected");
                    }
                },
                ex -> {
                    ex.printStackTrace();
                    System.err.println("Checking version failed");
                }
        );
    }

    private void tellUserAboutNewVersion(String serverUrl) {
        JOptionPane.showMessageDialog(owner,
                borderLayout()
                        .center(label("Newer version available @ " + serverUrl))
                        .south(hyperlinkButton("Get it here!", () -> openBrowser(serverUrl)))
                        .build());
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
        URL jarLocation = VersionChecker.class.getProtectionDomain().getCodeSource().getLocation();
        String jarLocationString = jarLocation.toString();
        String jarFileName = jarLocationString.substring("jar:file:".length(), jarLocationString.indexOf("!"));
        //todo this did not get printed in redirected console through System.out
        System.err.println("Checking 'last modified' of " + jarFileName);
        File file = new File(jarFileName);
        if(!file.exists() || !file.isFile()) throw new Exception("Not a jar");
        long local = file.lastModified();
        long fromServer = restGet(serverUrl + "/desktopClientLastModified", Long.class).execute();
        return fromServer > local;
    }
}
