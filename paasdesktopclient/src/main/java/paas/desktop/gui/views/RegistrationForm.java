package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.PopupRequest;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.LoginExecutor;
import paas.desktop.remoting.AuthService;
import swingutils.components.LazyInitSelfClosableAbstractView;

import javax.swing.*;

import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class RegistrationForm extends LazyInitSelfClosableAbstractView {

    @Autowired
    private AuthService authService;
    @Autowired
    private LoginExecutor loginController;
    @Autowired
    private LoginData loginData;
    @Autowired
    private EventBus eventBus;

    @Override
    protected JComponent wireAndLayout() {

        JTextField username = new JTextField(20);
        JPasswordField password = new JPasswordField();
        JPasswordField password2 = new JPasswordField();

        JButton backToLogin = hyperlinkButton("Back to login", () -> eventBus.dispatch(PopupRequest.LOGIN));
        backToLogin.setHorizontalAlignment(SwingConstants.RIGHT);

        return decorate(
                simpleForm()
                        .addRow("Username:", username)
                        .addRow("Password:", password)
                        .addRow("Repeat:", password2)
                        .addRow("", button("Register", () -> registerClick(username.getText(), String.valueOf(password.getPassword()), String.valueOf(password2.getPassword()))))
                        .addRow("", backToLogin)
                        .build())
                .withEmptyBorder(32, 32, 32, 64)
                .withGradientHeader("Registration", this::close, null)
                .opaque(true)
                .get();
    }

    private void registerClick(String username, String password, String repeated) {
        if (!password.equals(repeated)) {
            showMessage("Passwords don't match");
            return;
        }
        inBackground(
                () -> authService.register(username, password, loginData.getServerUrl()),
                () -> onRegistrationSuccess(username, password));
    }

    private void onRegistrationSuccess(String username, String password) {
        loginController.tryLogin(
                loginData.getServerUrl(),
                username,
                password,
                this::close,
                this::onException,
                getProgressIndicator()
        );
    }
}
