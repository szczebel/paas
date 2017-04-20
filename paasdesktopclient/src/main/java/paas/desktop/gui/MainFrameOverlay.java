package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.shared.Links;
import swingutils.RunnableProxy;
import swingutils.components.fade.FadingPanel;
import swingutils.layout.SnapToCorner;
import swingutils.mdi.MDI;
import swingutils.mdi.SelfCloseable;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static swingutils.components.ComponentFactory.decorate;
import static swingutils.components.ComponentFactory.hyperlinkButton;

@Component
public class MainFrameOverlay {

    @Autowired
    private MainFrame mainFrame;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private SelfCloseable loginForm;
    @Autowired
    private SelfCloseable registrationForm;
    @Autowired
    private LoginData loginData;


    private MDI overlayMDI;


    @PostConstruct
    void init() {
        mainFrame.getOverlay().setNonModalLayout(new SnapToCorner(8));
        overlayMDI = MDI.create(mainFrame.getOverlay());
        eventBus.when(PopupRequest.class, (request) -> request.visit(this));
    }

    void showRegistration() {
        overlayMDI.remove(loginForm);
        overlayMDI.add(null, registrationForm, SnapToCorner.TOP_RIGHT);
    }

    void showLogin() {
        overlayMDI.remove(registrationForm);
        overlayMDI.add(null, loginForm, SnapToCorner.TOP_RIGHT);
    }

    void tellUserAboutNewVersion() {
        RunnableProxy closeAction = new RunnableProxy();
        JButton downloadButton = hyperlinkButton("Download it!", this::download);
        FadingPanel downloadMessageBox = new FadingPanel(
                decorate(downloadButton)
                        .withEmptyBorder(16, 16, 24, 16)
                        .withGradientHeader("New version available", closeAction, null)
                        .opaque(true)
                        .get());
        overlayMDI.add(null, downloadMessageBox, closeAction::delegate, SnapToCorner.BOTTOM_RIGHT);
    }

    private void download() {
        try {
            Desktop.getDesktop().browse(new URI(loginData.getServerUrl() + Links.PAAS_DESKTOP_CLIENT_JAR));
        } catch (IOException | URISyntaxException e) {
            mainFrame.getOverlay().showAndLock(e.getMessage());
        }
    }

}
