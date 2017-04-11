package paas.desktop.gui.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginManager implements LoginData {

    @Autowired private EventBus eventBus;

    @Value("${server.url}")
    private String serverUrl;
    private String username = "not used, no security yet";
    private String password = "not used, no security yet";

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

    public void setLoginData(String newServerUrl) {
        serverUrl = newServerUrl;
        eventBus.serverChanged(serverUrl);
    }
}
