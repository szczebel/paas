package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.LoginExecutor;
import paas.desktop.remoting.RestCall;
import swingutils.components.LazyInitSelfClosableAbstractView;

import javax.swing.*;

import static paas.shared.Links.REGISTER;
import static swingutils.components.ComponentFactory.button;
import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

@Component
public class RegistrationForm extends LazyInitSelfClosableAbstractView {

    @Autowired
    private LoginExecutor loginController;
    @Autowired
    private LoginData loginData;

    @Override
    protected JComponent wireAndLayout() {

        JTextField username = new JTextField(10);
        JPasswordField password = new JPasswordField();
        JPasswordField password2 = new JPasswordField();

        return decorate(
                simpleForm()
                        .addRow("Username:", username)
                        .addRow("Password:", password)
                        .addRow("Repeat password:", password2)
                        .addRow("", button("Register", () -> registerClick(username.getText(), String.valueOf(password.getPassword()), String.valueOf(password2.getPassword()))))
                        .build())
                .withEmptyBorder(32, 32, 32, 32)
                .withGradientHeader("Registration", this::close, null)
                .opaque(true)
                .get();
    }

    private void registerClick(String username, String password, String repeated) {
        if(!password.equals(repeated)) {
            showMessage("Passwords don't match");
            return;
        }
        inBackground(
                () -> {
                    RestCall.restPostVoid(loginData.getServerUrl() + REGISTER)
                            .param("username", username)
                            .param("password", password)//todo send hashed?
                            .execute();
                },
                () -> loginController.tryLogin(
                        loginData.getServerUrl(),
                        username,
                        password,
                        this::close,
                        this::onException,
                        getProgressIndicator()
                ));
    }
}
