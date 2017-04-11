package paas.desktop.gui.views;

import paas.desktop.gui.infra.LoginManager;
import paas.desktop.gui.infra.MustBeInBackground;
import paas.desktop.remoting.RestCall;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static swingutils.components.ComponentFactory.button;
import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

public class LoginForm extends LazyInitRichAbstractView {

    private final LoginManager loginManager;
    private final Runnable close;

    private JTextField serverUrl;
    private JTextField username;
    private JPasswordField password;

    public LoginForm(LoginManager loginManager, Runnable close) {
        this.loginManager = loginManager;
        this.close = close;
        serverUrl = new JTextField(loginManager.getServerUrl());
        username = new JTextField(loginManager.getUsername());
        password = new JPasswordField(loginManager.getPassword());
    }

    @Override
    protected JComponent wireAndLayout() {
        JButton login = button("Login", this::loginClick);
        JButton cancel = button("Cancel", this::cancelClick);

        return decorate(simpleForm()
                .addRow("Server url:", serverUrl)
                .addRow("Username:", username)
                .addRow("Password:", password)
                .addRow("", login)
                .addRow("", cancel)
                .build())
                .withEmptyBorder(32, 32, 32, 32)
                .get();
    }

    private void loginClick() {
        inBackground(this::tryLogin, this::loginOk);
    }

    private void loginOk() {
        close.run();
        loginManager.setLoginData(serverUrl.getText());
    }

    @SuppressWarnings("WeakerAccess")
    @MustBeInBackground
    protected void tryLogin() {
        RestCall.restGet(serverUrl.getText() + "/login", String.class).execute();
    }

    private void cancelClick() {
        close.run();
    }
}
