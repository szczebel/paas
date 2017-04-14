package paas.desktop.gui.infra;

import org.springframework.stereotype.Component;
import paas.desktop.gui.ViewRequest;
import paas.shared.dto.HostedAppInfo;

import java.util.Collection;
import java.util.function.Consumer;

@Component
public class EventBus extends GenericEventBus{



    public void whenViewRequested(Consumer<ViewRequest> l) {
        subscribe(ViewRequest.class, l);
    }

    @MustBeInEDT
    public void requestView(ViewRequest request) {
        post(request);
    }

    public void whenLoginChanged(Runnable listener) {
        subscribe("LOGIN_CHANGED", listener);
    }

    @MustBeInEDT
    public void loginChanged() {
        trigger("LOGIN_CHANGED");
    }


    public void whenAppUpdated(Runnable listener) {
        subscribe("APP_UPDATED", listener);
    }

    @MustBeInEDT
    public void appUpdated() {
        trigger("APP_UPDATED");
    }

    public void whenDetailsRequested(Consumer<HostedAppInfo> consumer) {
        subscribe(HostedAppInfo.class, consumer);
    }

    @MustBeInEDT
    public void showDetails(HostedAppInfo hostedAppInfo) {
        post(hostedAppInfo);
    }

    public void whenCurrentAppsChanged(Consumer<Collection<HostedAppInfo>> listener) {
        subscribe(HostedAppInfos.class, p -> listener.accept(p.apps));
    }

    @MustBeInEDT
    public void currentAppsChanged(Collection<HostedAppInfo> apps) {
        post(new HostedAppInfos(apps));
    }

    static class HostedAppInfos {
        public HostedAppInfos(Collection<HostedAppInfo> apps) {
            this.apps = apps;
        }

        Collection<HostedAppInfo> apps;
    }
}
