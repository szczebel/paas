package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.LoginPresenter;
import paas.desktop.gui.infra.security.RegistrationPresenter;
import paas.desktop.gui.infra.version.NewVersionNotifier;
import paas.desktop.gui.infra.version.VersionChecker;
import paas.desktop.gui.views.AdminView;
import paas.shared.Links;
import swingutils.RunnableProxy;
import swingutils.components.IsComponent;
import swingutils.components.fade.FadingPanel;
import swingutils.frame.RichFrame;
import swingutils.layout.SnapToCorner;
import swingutils.layout.cards.CardMenuBuilders;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.hBox;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
@Qualifier("owner")
public class MainFrame extends RichFrame implements LoginPresenter, NewVersionNotifier, RegistrationPresenter, ComponentOwner {

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
    private IsComponent loginForm;
    @Autowired
    private IsComponent registrationForm;
    @Autowired
    private LoginData loginData;

    public void buildAndShow() {
        setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl());
        eventBus.whenLoginChanged(() -> setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl()));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getOverlay().setNonModalLayout(new SnapToCorner(8));
        add(buildContent());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        showLogin();
        versionChecker.checkVersion();
    }

    @Override
    public void close(IsComponent c) {
        getOverlay().removeNonmodal(c.getComponent());
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
                    if ("Server shell console".equals(newCard)) adminView.focus();
                })
                .build();
    }

    private JComponent customizeMenuBar(JComponent menu) {
        JLabel userInfo = label("");
        eventBus.whenLoginChanged(() -> userInfo.setText(getUserInfoString()));
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

    @Override
    public void showRegistration() {
        close(loginForm);
        getOverlay().addNonmodal(registrationForm.getComponent(), SnapToCorner.TOP_RIGHT);
    }

    @Override
    public void showLogin() {
        getOverlay().addNonmodal(loginForm.getComponent(), SnapToCorner.TOP_RIGHT);
    }

    @Override //todo: can look better
    public void tellUserAboutNewVersion() {
        RunnableProxy closeAction = new RunnableProxy();
        JButton downloadButton = hyperlinkButton("Download it!", this::download);
        FadingPanel downloadMessageBox = new FadingPanel(
                decorate(downloadButton)
                        .withEmptyBorder(16, 16, 24, 16)
                        .withGradientHeader("New version available", closeAction, null)
                        .opaque(true)
                        .get());
        closeAction.delegate(() -> downloadMessageBox.fadeOut(() -> getOverlay().removeNonmodal(downloadMessageBox)));
        getOverlay().addNonmodal(downloadMessageBox, SnapToCorner.BOTTOM_RIGHT);
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
