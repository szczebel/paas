package paas.desktop.gui.infra;

import org.springframework.stereotype.Component;
import paas.desktop.gui.ViewRequest;
import paas.shared.dto.HostedAppInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Component
public class EventBus {

    private List<Consumer<ViewRequest>> viewRequestListeners = new ArrayList<>();

    public void whenViewRequested(Consumer<ViewRequest> l) {
        viewRequestListeners.add(l);
    }

    @MustBeInEDT
    public void requestView(ViewRequest request) {
        viewRequestListeners.forEach(l -> l.accept(request));
    }


    private List<Runnable> loginDataObservers = new ArrayList<>();

    public void whenLoginChanged(Runnable listener) {
        loginDataObservers.add(listener);
    }

    @MustBeInEDT
    public void loginChanged() {
        loginDataObservers.forEach(Runnable::run);
    }

    private List<Runnable> appUpdatedListeners = new ArrayList<>();

    public void whenAppUpdated(Runnable listener) {
        appUpdatedListeners.add(listener);
    }

    @MustBeInEDT
    public void appUpdated() {
        appUpdatedListeners.forEach(Runnable::run);
    }

    private List<Consumer<HostedAppInfo>> showDetailsListeners = new ArrayList<>();

    public void whenDetailsRequested(Consumer<HostedAppInfo> consumer) {
        showDetailsListeners.add(consumer);
    }

    @MustBeInEDT
    public void showDetails(HostedAppInfo hostedAppInfo) {
        showDetailsListeners.forEach(l -> l.accept(hostedAppInfo));
    }

    private List<Consumer<Collection<HostedAppInfo>>> currentAppsChangeListeners = new ArrayList<>();

    public void whenCurrentAppsChanged(Consumer<Collection<HostedAppInfo>> listener) {
        currentAppsChangeListeners.add(listener);
    }

    @MustBeInEDT
    public void currentAppsChanged(Collection<HostedAppInfo> apps) {
        currentAppsChangeListeners.forEach(l -> l.accept(apps));
    }
}
