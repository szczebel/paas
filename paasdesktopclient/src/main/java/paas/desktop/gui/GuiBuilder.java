package paas.desktop.gui;

import com.jgoodies.forms.builder.FormBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.VersionChecker;
import paas.desktop.gui.views.ServerShellConsole;
import swingutils.components.ComponentFactory;
import swingutils.components.IsComponent;
import swingutils.layout.cards.CardMenuBuilders;

import javax.swing.*;
import java.awt.*;

import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.cards.CardLayoutBuilder.cardLayout;

@Component
public class GuiBuilder {

    private static final int MARGIN = 4;

    @Value("${tiniestpaas.server.url}")
    private String initialServerUrl;

    @Autowired private IsComponent hostedApplicationsView;
    @Autowired private IsComponent deployView;
    @Autowired private IsComponent serverUrlView;
    @Autowired private IsComponent tailViewsContainer;
    @Autowired private IsComponent selfLogView;
    @Autowired private ServerShellConsole serverShellConsole;
    @Autowired private EventBus eventBus;
    @Autowired private VersionChecker versionChecker;

    public void showGui() {
        ComponentFactory.initLAF();
        JFrame f = new JFrame("Tiniest PaaS desktop client - " + initialServerUrl);
        eventBus.whenServerChanged(url -> f.setTitle("Tiniest PaaS desktop client - " + url));
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.add(buildContent());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        versionChecker.initialize(f);
    }


    private JComponent buildContent() {
        JPanel north = FormBuilder.create()
                .columns("pref:none, pref:grow")
                .rows("pref:none, pref:none")
                .add(dec(serverUrlView.getComponent(), "Tiniest Paas Server", 0, MARGIN, MARGIN/2, MARGIN/2)).xy(1, 1)
                .add(dec(deployView.getComponent(), "Deploy", MARGIN/2, MARGIN, MARGIN/2, MARGIN/2)).xy(1, 2)
                .add(dec(hostedApplicationsView.getComponent(), "Hosted applications", 0, MARGIN/2, MARGIN/2, MARGIN)).xywh(2, 1, 1, 2)
                .build();
        JComponent logs = dec(tailViewsContainer.getComponent(), "Logs", MARGIN/2, MARGIN, MARGIN, MARGIN);
        logs.setPreferredSize(new Dimension(1300, 600));

        JComponent appsTab = borderLayout()
                .north(north)
                .center(logs)
                .build();

        return cardLayout(CardMenuBuilders.BorderedOrange())
                .addTab("Applications", appsTab)
                .addTab("Server shell console", serverShellConsole.getComponent())
                .addTab("My error log", selfLogView.getComponent())
                .onCardChange((prevCard, newCard) -> {
                    if("Server shell console".equals(newCard)) serverShellConsole.focus();
                })
                .build();
    }

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)
                .withGradientHeader(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
