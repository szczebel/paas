package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.LoginExecutor;
import swingutils.components.LazyInitSelfClosableAbstractView;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

import static java.awt.Font.BOLD;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingConstants.LEFT;
import static paas.desktop.gui.ViewRequest.REGISTRATION;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class LoginForm extends LazyInitSelfClosableAbstractView {

    private LinkedList<String> knownServers;

    @Autowired
    private LoginExecutor loginController;
    @Autowired
    private LoginData loginData;
    @Autowired
    private EventBus eventBus;

    @Override
    protected JComponent wireAndLayout() {

        JLabel notLoggedIn = label("You are not logged in.", LEFT, BOLD);
        eventBus.when(Events.LOGIN_CHANGED, () -> notLoggedIn.setVisible(false));
        JTextField username = new JTextField(loginData.getUsername());
        JPasswordField password = new JPasswordField(20);
        knownServers.remove(loginData.getServerUrl());
        knownServers.addFirst(loginData.getServerUrl());
        JComboBox<String> serverUrl = new JComboBox<>(knownServers.toArray(new String[knownServers.size()]));
        serverUrl.setEditable(true);
        JButton registerButton = hyperlinkButton("Registration", () -> eventBus.dispatch(REGISTRATION));
        registerButton.setHorizontalAlignment(SwingConstants.RIGHT);
        return decorate(
                simpleForm()
                        .addRow("", notLoggedIn)
                        .addRow("Server url:", serverUrl)
                        .addRow("Username:", username)
                        .addRow("Password:", password)
                        .addRow("", button("Login", () -> loginClick((String) serverUrl.getSelectedItem(), username.getText(), String.valueOf(password.getPassword()))))
                        .addRow("", button("Login as guest", () -> loginClick((String) serverUrl.getSelectedItem(), "guest", "guest")))
                        .addRow("", registerButton)
                        .build())
                .withEmptyBorder(32, 32, 32, 64)
                .withGradientHeader("Login", this::close, null)
                .opaque(true)
                .get();
    }

    private void loginClick(String server, String username, String password) {
        loginController.tryLogin(
                server,
                username,
                password,
                this::close,
                this::onException,
                getProgressIndicator()
        );
    }

    @Value("#{'${knownServers}'.split(',')}")
    public void setKnownServers(List<String> knownServers) {
        this.knownServers = new LinkedList<>(knownServers.stream().map(String::trim).collect(toList()));
    }
}
