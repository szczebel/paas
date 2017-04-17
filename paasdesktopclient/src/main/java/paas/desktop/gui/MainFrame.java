package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.version.VersionChecker;
import paas.desktop.gui.views.AdminView;
import paas.shared.Links;
import swingutils.RunnableProxy;
import swingutils.components.IsComponent;
import swingutils.components.fade.FadingPanel;
import swingutils.frame.RichFrame;
import swingutils.layout.SnapToCorner;
import swingutils.layout.cards.CardMenuBuilders;
import swingutils.mdi.MDI;
import swingutils.mdi.SelfCloseable;
import swingutils.spring.application.SwingEntryPoint;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static paas.desktop.gui.infra.events.Events.LOGIN_CHANGED;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.hBox;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
public class MainFrame extends RichFrame implements SwingEntryPoint {

    private static final int MARGIN = 4;

    @Autowired
    private IsComponent hostedAppsListView;
    @Autowired
    private IsComponent deployView;
    @Autowired
    private IsComponent hostedAppDetailsViewsContainer;
    @Autowired
    private IsComponent selfLogView;
    @Autowired
    private AdminView adminView;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private VersionChecker versionChecker;
    @Autowired
    private SelfCloseable loginForm;
    @Autowired
    private SelfCloseable registrationForm;
    @Autowired
    private LoginData loginData;

    private MDI overlayMDI = MDI.create(getOverlay());

    public void startInEdt() {
        eventBus.when(LOGIN_CHANGED, () -> setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl()));
        eventBus.when(ViewRequest.class, (request) -> request.visit(this));

        setIconImage(new ImageIcon(getClass().getResource("/splash.png")).getImage());
        setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getOverlay().setNonModalLayout(new SnapToCorner(8));
        add(buildContent());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        showLogin();
        versionChecker.checkVersion();
    }

    private JComponent buildContent() {
        hostedAppsListView.getComponent().setPreferredSize(new Dimension(100, 200));
        JComponent north = borderLayout()
                .west(dec(deployView.getComponent(), "New deployment", 0, MARGIN, MARGIN / 2, MARGIN / 2))
                .center(dec(hostedAppsListView.getComponent(), "Hosted applications", 0, MARGIN / 2, MARGIN / 2, MARGIN))
                .build();
        JComponent container = hostedAppDetailsViewsContainer.getComponent();
        container.setPreferredSize(new Dimension(1300, 600));

        JComponent appsTab = borderLayout()
                .north(north)
                .center(container)
                .build();

        return cardLayout(CardMenuBuilders.BorderedOrange()
                .menuBarCustomizer(this::customizeMenuBar))
                .addTab("Applications", appsTab)
                .addTab("For admins", adminView.getComponent())
                .addTab("My error log", selfLogView.getComponent())
                .onCardChange((prevCard, newCard) -> {
                    if ("For admins".equals(newCard)) adminView.focus();
                })
                .build();
    }

    private JComponent customizeMenuBar(JComponent menu) {
        JLabel userInfo = label("");
        Runnable listener = () -> userInfo.setText(getUserInfoString());
        eventBus.when(LOGIN_CHANGED, listener);
        return borderLayout()
                .center(menu)
                .east(
                        hBox(8,
                                userInfo,
                                hyperlinkButton("Login", this::showLogin)
                        )
                )
                .build();
    }

    private String getUserInfoString() {
        return "Hello " + loginData.getUsername() + ", your role is: " + loginData.getRoles();
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
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)
                .withTitledSeparator(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
