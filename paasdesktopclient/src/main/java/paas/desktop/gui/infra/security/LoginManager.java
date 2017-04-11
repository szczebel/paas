package paas.desktop.gui.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.MustBeInBackground;
import paas.desktop.remoting.RestCall;
import swingutils.background.BackgroundOperation;
import swingutils.components.progress.ProgressIndicator;

import java.util.List;
import java.util.function.Consumer;

import static paas.shared.Links.WHOAMI;

@Component
@Qualifier("loginController, loginData")
public class LoginManager implements LoginData, LoginController {

    @Autowired
    private EventBus eventBus;

    @Value("${server.url}")
    private String serverUrl;
    private String username = "user";
    private String password = "user";

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void tryLogin(String serverUrl, String username, String password,
                         Runnable onSuccess, Consumer<Exception> exceptionHandler,
                         ProgressIndicator progressIndicator) {
        BackgroundOperation.execute(
                () -> callServer(serverUrl, username, password),
                () -> loginSuccess(serverUrl, username, password, onSuccess),
                exceptionHandler,
                progressIndicator
        );
    }

    @SuppressWarnings("WeakerAccess")
    @MustBeInBackground
    protected void callServer(String serverUrl, String username, String password) {
        //UserInfo principalJson =
                RestCall.restGet(serverUrl + WHOAMI, UserInfo.class)
                .httpBasic(username, password)
                .execute();
        //System.out.println(principalJson.getAuthorities());
    }

    private void loginSuccess(String serverUrl, String username, String password, Runnable onSuccess) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        onSuccess.run();
        eventBus.serverChanged(this.serverUrl);
    }

    static class UserInfo {
        List<AuthorityInfo> authorities;

        List<AuthorityInfo> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(List<AuthorityInfo> authorities) {
            this.authorities = authorities;
        }

        static class AuthorityInfo {
            String authority;

            public String getAuthority() {
                return authority;
            }

            public void setAuthority(String authority) {
                this.authority = authority;
            }

            @Override
            public String toString() {
                return authority;
            }
        }
    }
}
