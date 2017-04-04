package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static swingutils.components.ComponentFactory.button;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class ServerUrlView extends LazyInitRichAbstractView {

    @Value("${tiniestpaas.server.url}")
    private String defaultServerUrl;
    @Autowired private EventBus eventBus;
    private JTextField urlTextField;

    @Override
    protected JComponent wireAndLayout() {
        urlTextField = new JTextField(defaultServerUrl);
        urlTextField.setEditable(false);
        JButton button = button("Change...", this::changeServer);


        return simpleForm()
                .addRow("URL:",
                    borderLayout()
                        .center(urlTextField)
                        .east(button)
                        .build()
                ).build();
    }

    private void changeServer() {
        String res = JOptionPane.showInputDialog(getComponent(), "Provide url of the server");
        if (res != null) {
            urlTextField.setText(res);
            eventBus.serverChanged(res);
        }
    }
}
