package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.version.VersionChecker;
import paas.desktop.gui.views.AdminView;
import swingutils.components.IsComponent;
import swingutils.frame.RichFrame;
import swingutils.layout.SnapToCorner;
import swingutils.layout.cards.CardMenuBuilders;
import swingutils.spring.application.SwingEntryPoint;

import javax.swing.*;
import java.awt.*;

import static paas.desktop.gui.infra.events.Events.LOGIN_CHANGED;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.hBox;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
public class MainFrame extends RichFrame implements SwingEntryPoint {

    private static final int MARGIN = 4;

    @Autowired
    private IsComponent docsView;
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
    private VersionChecker versionChecker;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private LoginData loginData;

    public void startInEdt() {
        eventBus.when(LOGIN_CHANGED, () -> setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl()));

        setIconImage(new ImageIcon(getClass().getResource("/splash.png")).getImage());
        setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getOverlay().setNonModalLayout(new SnapToCorner(8));
        add(buildContent());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        showLogin();

        versionChecker.checkVersion();//todo: this does not belong here
    }

    private void showLogin() {
        eventBus.dispatch(ViewRequest.LOGIN);
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
                .addTab("Documentation", docsView.getComponent())
                .addTab("For admins", adminView.getComponent())
                .addTab("My error log", selfLogView.getComponent())
                .onCardChange((prevCard, newCard) -> {
                    if ("For admins".equals(newCard)) adminView.focus();
                })
                .build();
    }

    private JComponent customizeMenuBar(JComponent menu) {
        JLabel userInfo = label("");
        eventBus.when(LOGIN_CHANGED, () -> userInfo.setText(getUserInfoString()));
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

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)
                .withTitledSeparator(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
