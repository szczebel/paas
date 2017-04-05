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

import static swingutils.components.ComponentFactory.decorate;

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
    @Autowired private EventBus eventBus;

    public void showGui() {
        eventBus.whenServerChanged(httpPaasClient::serverChanged);

        ComponentFactory.initLAF();
        JFrame f = new JFrame("Tiniest PaaS desktop client");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel north = FormBuilder.create()
                .columns("pref:none, pref:grow")
                .rows("pref:none, pref:none")
                .add(dec(serverUrlView.getComponent(), "Tiniest Paas Server", 8, 8, 4, 4)).xy(1, 1)
                .add(dec(deployView.getComponent(), "Deploy", 4, 8, 4, 4)).xy(1, 2)
                .add(dec(hostedApplicationsView.getComponent(), "Hosted applications", 8, 4, 4, 8)).xywh(2, 1, 1, 2)
                .build();
        f.add(north, BorderLayout.NORTH);
        JComponent center = dec(tailViewsContainer.getComponent(), "Logs", 4, 8, 8, 8);
        center.setPreferredSize(new Dimension(1300, 600));
        f.add(center);

        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private JComponent dec(JComponent component, String title, int top, int left, int bottom, int right) {
        return decorate(component)
                .withEmptyBorder(4, 4, 4, 4)
                .withGradientHeader(title)
                .withEmptyBorder(top, left, bottom, right)
                .get();
    }
}
