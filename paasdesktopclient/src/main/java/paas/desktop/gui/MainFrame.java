package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.VersionChecker;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.views.AdminView;
import paas.desktop.gui.views.LoginComponent;
import swingutils.components.IsComponent;
import swingutils.frame.RichFrame;
import swingutils.layout.SnapToCorner;
import swingutils.layout.cards.CardMenuBuilders;

import javax.swing.*;
import java.awt.*;

import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.hBox;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
public class MainFrame extends RichFrame implements paas.desktop.gui.infra.security.LoginPresenter {

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
    private LoginComponent loginForm;
    @Autowired
    private LoginData loginData;

    public void buildAndShow() {
        setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl());
        eventBus.whenLoginChanged(() -> setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl()));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getOverlay().setNonModalLayout(new SnapToCorner());
        loginForm.setCloseAction(this::closeLogin);
        add(buildContent());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        versionChecker.initialize(this);
        showLogin();
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
    public void showLogin() {
        getOverlay().addNonmodal(loginForm.getComponent(), SnapToCorner.TOP_RIGHT);
    }

    private void closeLogin() {
        getOverlay().removeNonmodal(loginForm.getComponent());
    }

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)
                .withTitledSeparator(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
