package paas.desktop.gui.infra.events;

import paas.shared.dto.HostedAppInfo;

import java.io.File;
import java.util.Collection;

public class Events {
    public static final String LOGIN_CHANGED = "LOGIN_CHANGED";
    public static final String APP_UPDATED = "APP_UPDATED";
    public static final String DOWNLOAD_FAILED = "DOWNLOAD_FAILED";

    public static AppsChanged appsChanged(Collection<HostedAppInfo> apps) {
        return new AppsChanged(apps);
    }

    public static ShowDetails showDetails(HostedAppInfo app) {
        return new ShowDetails(app);
    }

    public static NewVersionDownloaded newVersionDownloaded(File newVersion) {
        return new NewVersionDownloaded(newVersion);
    }

    public static class AppsChanged {
        public final Collection<HostedAppInfo> apps;

        AppsChanged(Collection<HostedAppInfo> apps) {
            this.apps = apps;
        }
    }

    public static class ShowDetails {
        public final HostedAppInfo app;

        ShowDetails(HostedAppInfo app) {
            this.app = app;
        }
    }

    public static class NewVersionDownloaded {
        public final File newVersion;

        NewVersionDownloaded(File newVersion) {
            this.newVersion = newVersion;
        }
    }
}
