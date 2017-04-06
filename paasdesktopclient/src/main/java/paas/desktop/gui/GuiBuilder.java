package paas.desktop.gui;

import com.jgoodies.forms.builder.FormBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.components.ComponentFactory;
import swingutils.components.IsComponent;

import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingConstants.BOTTOM;
import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.tabbedPane;

//todo splashscreen
//todo fix too long error messages not fitting overlays
//todo redirect Syserr/out to  gui
//todo local tomcat&paas forever

@Component
public class GuiBuilder {

    @Autowired private HttpPaasClient httpPaasClient;
    @Autowired private IsComponent hostedApplicationsView;
    @Autowired private IsComponent deployView;
    @Autowired private IsComponent serverUrlView;
    @Autowired private IsComponent tailViewsContainer;
    @Autowired private IsComponent serverShellConsole;
    @Autowired private EventBus eventBus;

    public void showGui() {
        eventBus.whenServerChanged(httpPaasClient::serverChanged);//todo: this does not belong here
        ComponentFactory.initLAF();
        JFrame f = new JFrame("Tiniest PaaS desktop client");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.add(buildContent());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private JComponent buildContent() {
        JPanel north = FormBuilder.create()
                .columns("pref:none, pref:grow")
                .rows("pref:none, pref:none")
                .add(dec(serverUrlView.getComponent(), "Tiniest Paas Server", 8, 8, 4, 4)).xy(1, 1)
                .add(dec(deployView.getComponent(), "Deploy", 4, 8, 4, 4)).xy(1, 2)
                .add(dec(hostedApplicationsView.getComponent(), "Hosted applications", 8, 4, 4, 8)).xywh(2, 1, 1, 2)
                .build();
        JComponent logs = dec(tailViewsContainer.getComponent(), "Logs", 4, 8, 8, 8);
        logs.setPreferredSize(new Dimension(1300, 600));

        return borderLayout()
                .north(north)
                .center(tabbedPane(BOTTOM)
                        .addTab("Logs", logs)
                        .addTab("Server shell console", serverShellConsole.getComponent())
                        .build())
                .build();
    }

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(4, 4, 4, 4)
                .withGradientHeader(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
