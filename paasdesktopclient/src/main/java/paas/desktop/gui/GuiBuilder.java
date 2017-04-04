package paas.desktop.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.components.ComponentFactory;
import swingutils.components.IsComponent;

import javax.swing.*;

import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.vBox;

@Component
public class GuiBuilder {

    @Autowired
    private HttpPaasClient httpPaasClient;
    @Autowired
    private IsComponent hostedApplicationsView;
    @Autowired
    private IsComponent deployView;
    @Autowired
    private IsComponent serverUrlView;
    @Autowired
    private IsComponent tailViewsContainer;
    @Autowired
    private EventBus eventBus;

    public void showGui() {
        eventBus.whenServerChanged(httpPaasClient::serverChanged);

        ComponentFactory.initLAF();
        JFrame f = new JFrame("Tiniest PaaS desktop client");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.add(borderLayout()
                .west(
                        borderLayout().north(
                                vBox(0,
                                        dec(serverUrlView.getComponent(), "Tiniest Paas Server", 8, 8, 4, 4),
                                        dec(deployView.getComponent(), "Deploy", 4, 8, 4, 4)
                                        ))
                                .center(dec(hostedApplicationsView.getComponent(), "Hosted applications", 4, 8, 8, 4)
                                )
                                .build()
                )
                //todo split pane
                .center(dec(tailViewsContainer.getComponent(), "Tail", 8, 4, 8, 8))
                .build()
        );
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
