package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.LoginExecutor;
import swingutils.components.LazyInitSelfClosableAbstractView;

import javax.swing.*;

import static java.awt.Font.BOLD;
import static javax.swing.SwingConstants.LEFT;
import static paas.desktop.gui.ViewRequest.REGISTRATION;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class LoginForm extends LazyInitSelfClosableAbstractView {

    @Autowired
    private LoginExecutor loginController;
    @Autowired
    private LoginData loginData;
    @Autowired
    private EventBus eventBus;

    @Override
    protected JComponent wireAndLayout() {

        JLabel notLoggedIn = label("<html>You are not logged in.<br/>Guest's password is 'guest'.", LEFT, BOLD);
        eventBus.whenLoginChanged(() -> notLoggedIn.setVisible(false));
        JTextField serverUrl = new JTextField(loginData.getServerUrl());
        JTextField username = new JTextField(loginData.getUsername());
        JPasswordField password = new JPasswordField(loginData.getPassword());

        return decorate(
                simpleForm()
                        .addRow("", notLoggedIn)
                        .addRow("Server url:", serverUrl)
                        .addRow("Username:", username)
                        .addRow("Password:", password)
                        .addRow("", button("Login", () -> loginClick(serverUrl.getText(), username.getText(), String.valueOf(password.getPassword()))))
                        .addRow("", button("Register...", () -> eventBus.requestView(REGISTRATION)))
                        .build())
                .withEmptyBorder(32, 32, 32, 32)
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
}
