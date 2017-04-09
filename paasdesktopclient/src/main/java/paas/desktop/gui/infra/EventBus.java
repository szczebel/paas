package paas.desktop.gui.infra;

import org.springframework.stereotype.Component;
import paas.desktop.dto.HostedAppInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Component
public class EventBus {
    private List<Consumer<String>> serverUrlObserverList = new ArrayList<>();

    public void whenServerChanged(Consumer<String> listener) {
        serverUrlObserverList.add(listener);
    }

    public void serverChanged(String newUrl) {
        serverUrlObserverList.forEach(l -> l.accept(newUrl));
    }

    private List<Runnable> appDeployedListeners = new ArrayList<>();

    public void whenAppDeployed(Runnable listener) {
        appDeployedListeners.add(listener);
    }

    public void appDeployed() {
        appDeployedListeners.forEach(Runnable::run);
    }

    private List<Consumer<HostedAppInfo>> tailRequestListeners = new ArrayList<>();

    public void whenTailRequested(Consumer<HostedAppInfo> consumer) {
        tailRequestListeners.add(consumer);
    }

    public void showTailFor(HostedAppInfo hostedAppInfo) {
        tailRequestListeners.forEach(l -> l.accept(hostedAppInfo));
    }

    private List<Consumer<Collection<HostedAppInfo>>> currentAppsChangeListeners = new ArrayList<>();

    public void whenCurrentAppsChanged(Consumer<Collection<HostedAppInfo>> listener) {
        currentAppsChangeListeners.add(listener);
    }

    public void currentAppsChanged(Collection<HostedAppInfo> apps) {
        currentAppsChangeListeners.forEach(l -> l.accept(apps));
    }
}
