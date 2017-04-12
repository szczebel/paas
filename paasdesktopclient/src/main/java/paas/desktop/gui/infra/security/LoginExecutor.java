package paas.desktop.gui.infra.security;

import swingutils.components.progress.ProgressIndicator;

import java.util.function.Consumer;

public interface LoginExecutor {
    void tryLogin(String serverUrl, String username, String password,
                  Runnable onSuccess, Consumer<Exception> exceptionHandler,
                  ProgressIndicator progressIndicator);
}
