package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.views.AdminView;
import swingutils.components.IsComponent;
import swingutils.frame.RichFrame;
import swingutils.layout.cards.CardMenuBuilders;

import javax.swing.*;
import java.awt.*;

import static paas.desktop.gui.infra.events.Events.LOGIN_CHANGED;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.hBox;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
public class MainFrame extends RichFrame {

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
    private EventBus eventBus;
    @Autowired
    private LoginData loginData;

    public void buildAndShow() {
        eventBus.when(LOGIN_CHANGED, () -> setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl()));

        setIconImage(new ImageIcon(getClass().getResource("/splash.png")).getImage());
        setTitle("Tiniest PaaS desktop client - " + loginData.getServerUrl());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(buildContent());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        showLogin();
    }

    private void showLogin() {
        eventBus.dispatch(PopupRequest.LOGIN);
    }

    private JComponent buildContent() {
        hostedAppsListView.getComponent().setPreferredSize(new Dimension(800, 200));//to secure some width
        JComponent north = borderLayout()
                .west(dec(deployView.getComponent(), "New deployment", 0, MARGIN, MARGIN / 2, MARGIN / 2))
                .center(dec(hostedAppsListView.getComponent(), "Hosted applications", 0, MARGIN / 2, MARGIN / 2, MARGIN))
                .build();
        JComponent container = hostedAppDetailsViewsContainer.getComponent();

        JComponent appsTab = borderLayout()
                .north(north)
                .center(container)
                .build();

        JComponent docsViewComponent = docsView.getComponent();
        //this guys stretches the window too tall, so will reduce it
        docsViewComponent.setPreferredSize(new Dimension(1000, 800));

        return cardLayout(CardMenuBuilders.BorderedOrange()
                .menuBarCustomizer(this::customizeMenuBar))
                .addTab("Applications", appsTab)
                .addTab("Documentation", docsViewComponent)
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
