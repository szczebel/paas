package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.security.LoginController;
import paas.desktop.gui.infra.security.LoginData;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static swingutils.components.ComponentFactory.button;
import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class LoginForm extends LazyInitRichAbstractView implements LoginComponent {

    @Autowired private LoginController loginController;
    @Autowired private LoginData loginData;

    private JTextField serverUrl;
    private JTextField username;
    private JPasswordField password;
    private Runnable closeAction;

    @Override
    protected JComponent wireAndLayout() {
        serverUrl = new JTextField(loginData.getServerUrl());
        username = new JTextField(loginData.getUsername());
        password = new JPasswordField(loginData.getPassword());
        JButton login = button("Login", this::loginClick);
        JButton cancel = button("Cancel", this::close);

        return decorate(
                simpleForm()
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
