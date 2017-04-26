package paas.desktop.gui;

import java.util.function.Consumer;

public enum PopupRequest {
    LOGIN(MainFrameOverlay::showLogin),
    REGISTRATION(MainFrameOverlay::showRegistration);

    private final Consumer<MainFrameOverlay> invoker;
    PopupRequest(Consumer<MainFrameOverlay> invoker) {
        this.invoker = invoker;
    }

    final void visit(MainFrameOverlay mainFrame) {
        invoker.accept(mainFrame);
    }
}
