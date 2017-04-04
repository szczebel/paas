package paas.desktop.gui.views;

import ca.odell.glazedlists.EventList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.HostedAppInfo;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.EventListHelper;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.table.TableFactory;
import swingutils.components.table.TablePanel;
import swingutils.components.table.descriptor.Columns;

import javax.swing.*;

import static swingutils.EventListHelper.eventList;
import static swingutils.background.BackgroundOperation.execute;
import static swingutils.components.ComponentFactory.button;

@Component
public class HostedApplicationsView extends LazyInitRichAbstractView {

    private final EventList<HostedAppInfo> apps = eventList();
    @Autowired
    private HttpPaasClient httpPaasClient;
    @Autowired
    private EventBus eventBus;

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenServerChanged(newUrl -> this.onServerChange());
        eventBus.whenAppDeployed(this::refreshApps);

        Columns<HostedAppInfo> columns = Columns.create(HostedAppInfo.class)
                .column("App ID", Integer.class, HostedAppInfo::getId)
                .column("Jar file", String.class, HostedAppInfo::getJarFile)
                .column("Command line", String.class, HostedAppInfo::getCommandLineArgs)
                .column("Running", Boolean.class, HostedAppInfo::isRunning)
                .actionable("Show tail", "Show", eventBus::showTailFor)
                .actionable("Restart", "Restart", this::restart);
        TablePanel<HostedAppInfo> tablePanel = TableFactory.createTablePanel(apps, columns);
        tablePanel.getToolbar().removeAll();
        tablePanel.getToolbar().add(button("Refresh", this::refreshApps));

        return tablePanel.getComponent();
    }

    private void onServerChange() {
        EventListHelper.clearEventList(apps);
        refreshApps();
    }

    private void refreshApps() {
        execute(
                httpPaasClient::getHostedApplications,
                res -> EventListHelper.replaceContent(apps, res),
                this::onException,
                getParent());
    }

    private void restart(HostedAppInfo appInfo) {
        execute(
                () -> httpPaasClient.restart(appInfo.getId()),
                res -> {
                    getParent().showAndLock(res);
                    refreshApps();
                },
                this::onException
        );
    }
}
