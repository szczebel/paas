package paas.desktop.gui;

import java.util.function.Consumer;

public enum PopupRequest {
    LOGIN(MainFrameOverlay::showLogin),
    REGISTRATION(MainFrameOverlay::showRegistration),
    NEW_VERSION(MainFrameOverlay::tellUserAboutNewVersion);

    private final Consumer<MainFrameOverlay> invoker;
    PopupRequest(Consumer<MainFrameOverlay> invoker) {
        this.invoker = invoker;
    }

    final void visit(MainFrameOverlay mainFrame) {
        invoker.accept(mainFrame);
    }
}
