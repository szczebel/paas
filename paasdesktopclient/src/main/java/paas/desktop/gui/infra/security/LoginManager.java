package paas.desktop.gui.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.MustNotBeInEDT;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.gui.infra.security.LoginManager.UserInfo.AuthorityInfo;
import restcall.RestCall;
import swingutils.background.BackgroundOperation;
import swingutils.components.progress.ProgressIndicator;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static paas.shared.Links.WHOAMI;

@Component
@Qualifier("loginController, loginData")
public class LoginManager implements LoginData, LoginExecutor {

    @Autowired
    private EventBus eventBus;

    @Value("${server.url}")
    private String serverUrl;
    private String username = "";
    private String password = "";
    private Optional<List<AuthorityInfo>> roles = Optional.empty();
    private boolean loggedIn = false;

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
                () -> whoAmI(serverUrl, username, password),
                userInfo -> loginSuccess(serverUrl, username, password, userInfo.getAuthorities(), onSuccess),
                exceptionHandler,
                progressIndicator
        );
    }

    @SuppressWarnings("WeakerAccess")
    @MustNotBeInEDT
    protected UserInfo whoAmI(String serverUrl, String username, String password) {
        return
                RestCall.restGet(serverUrl + WHOAMI, UserInfo.class)
                        .httpBasic(username, password)
                        .execute();
    }

    private void loginSuccess(String serverUrl, String username, String password, List<AuthorityInfo> roles, Runnable onSuccess) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.roles = Optional.of(roles);
        this.loggedIn = true;
        onSuccess.run();
        eventBus.dispatchEvent(Events.LOGIN_CHANGED);
    }

    @Override
    public String getRoles() {
        return roles.map(this::join).orElse("[unknown]");
    }

    private String join(List<AuthorityInfo> roles) {
        return String.join(",",
                roles.stream().map(AuthorityInfo::getAuthority)
                        .map(roleString -> roleString.substring(5))//strip ROLE_ prefix
                        .collect(toList())
        );
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
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
