package paas.desktop.gui.infra.events;

import paas.shared.dto.HostedAppInfo;

import java.util.Collection;

public class Events {
    public static final String LOGIN_CHANGED = "LOGIN_CHANGED";
    public static final String APP_UPDATED = "APP_UPDATED";

    public static AppsChanged appsChanged(Collection<HostedAppInfo> apps) {
        return new AppsChanged(apps);
    }

    public static ShowDetails showDetails(HostedAppInfo app) {
        return new ShowDetails(app);
    }

    public static class AppsChanged {
        public AppsChanged(Collection<HostedAppInfo> apps) {
            this.apps = apps;
        }

        public final Collection<HostedAppInfo> apps;
    }

    public static class ShowDetails {
        public final HostedAppInfo app;

        public ShowDetails(HostedAppInfo app) {
            this.app = app;
        }
    }
}
