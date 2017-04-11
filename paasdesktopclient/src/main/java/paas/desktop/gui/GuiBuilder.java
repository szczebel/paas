package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.LoginManager;
import paas.desktop.gui.infra.VersionChecker;
import paas.desktop.gui.views.LoginForm;
import paas.desktop.gui.views.ServerShellConsole;
import swingutils.components.ComponentFactory;
import swingutils.components.IsComponent;
import swingutils.frame.RichFrame;
import swingutils.layout.cards.CardMenuBuilders;

import javax.swing.*;
import java.awt.*;

import static swingutils.components.ComponentFactory.decorate;
import static swingutils.components.ComponentFactory.hyperlinkButton;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
public class GuiBuilder {

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
    private ServerShellConsole serverShellConsole;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private VersionChecker versionChecker;
    @Autowired
    private LoginManager loginManager;

    public void showGui() {
        ComponentFactory.initLAF();
        RichFrame f = new RichFrame();
        f.setTitle("Tiniest PaaS desktop client - " + loginManager.getServerUrl());
        eventBus.whenServerChanged(url -> f.setTitle("Tiniest PaaS desktop client - " + url));
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.add(buildContent(f));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        versionChecker.initialize(f);
        //showLogin(f); when security implemented
    }


    private JComponent buildContent(RichFrame f) {
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
                .menuBarCustomizer(menu -> borderLayout().center(menu).east(buildLoginButton(f)).build()))
                .addTab("Applications", appsTab)
                .addTab("Server shell console", serverShellConsole.getComponent())
                .addTab("My error log", selfLogView.getComponent())
                .onCardChange((prevCard, newCard) -> {
                    if ("Server shell console".equals(newCard)) serverShellConsole.focus();
                })
                .build();
    }

    private JComponent buildLoginButton(RichFrame parent) {
        return decorate(hyperlinkButton("Login", () -> showLogin(parent)))
                .withEmptyBorder(0, 0, 0, 8)
                .get();
    }

    private void showLogin(RichFrame parent) {
        //todo: don't do it modal
        LoginForm form = new LoginForm(loginManager, () -> parent.getOverlay().hideAndUnlock());
        parent.getOverlay().showAndLock(
                decorate(form.getComponent())
                        .withGradientHeader("Login")
                        .opaque(true)
                        .get());
    }

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)
                .withTitledSeparator(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
