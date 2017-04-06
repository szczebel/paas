package paas.desktop.gui.views;

import com.jgoodies.forms.builder.FormBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static javax.swing.SwingConstants.RIGHT;
import static swingutils.components.ComponentFactory.button;
import static swingutils.components.ComponentFactory.label;

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

        return FormBuilder.create()
                .rows("pref:none")
                .columns("pref:none, ${label-component-gap}, pref:grow, ${label-component-gap}, pref:none")
                .add(label("Url:", RIGHT)).xy(1, 1)
                .add(urlTextField).xy(3, 1)
                .add(button).xy(5, 1)
                .build();
    }

    private void changeServer() {
        String res = JOptionPane.showInputDialog(getComponent(), "Provide url of the server", urlTextField.getText());
        if (res != null && !res.equals(urlTextField.getText())) {
            urlTextField.setText(res);
            eventBus.serverChanged(res);
        }
    }
}
