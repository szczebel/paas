package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.LoginExecutor;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static java.awt.Font.BOLD;
import static javax.swing.SwingConstants.LEFT;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class LoginForm extends LazyInitRichAbstractView implements LoginComponent {

    @Autowired
    private LoginExecutor loginController;
    @Autowired
    private LoginData loginData;
    @Autowired
    private EventBus eventBus;

    private JTextField serverUrl;
    private JTextField username;
    private JPasswordField password;
    private Runnable closeAction;

    @Override
    protected JComponent wireAndLayout() {
        JLabel notLoggedIn = label("<html>You are not logged in.<br/>Guest's password is 'guest'.", LEFT, BOLD);
        eventBus.whenLoginChanged(() -> notLoggedIn.setVisible(false));
        serverUrl = new JTextField(loginData.getServerUrl());
        username = new JTextField(loginData.getUsername());
        password = new JPasswordField(loginData.getPassword());
        JButton login = button("Login", this::loginClick);
        JButton cancel = button("Cancel", this::close);

        return decorate(
                simpleForm()
                        .addRow("", notLoggedIn)
                        .addRow("Server url:", serverUrl)
                        .addRow("Username:", username)
                        .addRow("Password:", password)
                        .addRow("", login)
                        .addRow("", cancel)
                        .build())
                .withEmptyBorder(32, 32, 32, 32)
                .withGradientHeader("Login")
                .opaque(true)
                .get();
    }

    private void loginClick() {
        loginController.tryLogin(
                serverUrl.getText(),
                username.getText(),
                String.valueOf(password.getPassword()),
                this::close,
                this::onException,
                getProgressIndicator()
        );
    }

    private void close() {
        closeAction.run();
    }

    @Override
    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }
}
