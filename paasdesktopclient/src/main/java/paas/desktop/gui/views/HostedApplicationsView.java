package paas.desktop.gui.views;

import ca.odell.glazedlists.EventList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.dto.HostedAppInfo;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.PaasRestClient;
import swingutils.EventListHelper;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.table.TableFactory;
import swingutils.components.table.TablePanel;
import swingutils.components.table.descriptor.Columns;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static swingutils.EventListHelper.eventList;
import static swingutils.components.ComponentFactory.button;

@Component
public class HostedApplicationsView extends LazyInitRichAbstractView {

    private final EventList<HostedAppInfo> apps = eventList();
    @Autowired
    private PaasRestClient paasRestClient;
    @Autowired
    private EventBus eventBus;

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenServerChanged(newUrl -> this.onServerChange());
        eventBus.whenAppDeployed(this::refreshApps);

        Columns<HostedAppInfo> columns = Columns.create(HostedAppInfo.class)
                .column("App ID", Long.class, HostedAppInfo::getId)
                .column("Jar file", String.class, HostedAppInfo::getJarFile)
                .column("Command line", String.class, HostedAppInfo::getCommandLineArgs)
                .column("Running", Boolean.class, HostedAppInfo::isRunning)
                .column("Start time", Date.class, HostedAppInfo::getStart)
                .actionable("Show tail", "Show", eventBus::showTailFor)
                .actionable("Restart", "Restart", this::restart)
                .actionable("Undeploy", "Undeploy", this::undeploy);
        TablePanel<HostedAppInfo> tablePanel = TableFactory.createTablePanel(apps, columns);
        tablePanel.getToolbar().removeAll();
        tablePanel.getToolbar().add(button("Refresh", this::refreshApps));
        tablePanel.getTable().setDefaultRenderer(Date.class, new DateRenderer());

        return tablePanel.getComponent();
    }

    private void onServerChange() {
        EventListHelper.clearEventList(apps);
        eventBus.currentAppsChanged(Collections.emptyList());
        refreshApps();
    }

    private void refreshApps() {
        inBackground(paasRestClient::getHostedApplications, this::appsFetched);
    }

    private void appsFetched(List<HostedAppInfo> apps) {
        EventListHelper.replaceContent(this.apps, apps);
        eventBus.currentAppsChanged(apps);
    }

    private void restart(HostedAppInfo appInfo) {
        doInBackgroundAndRefreshApps(() -> paasRestClient.restart(appInfo.getId()));
    }

    private void undeploy(HostedAppInfo appInfo) {
        doInBackgroundAndRefreshApps(() -> paasRestClient.undeploy(appInfo.getId()));
    }

    private void doInBackgroundAndRefreshApps(Callable<String> task) {
        inBackground(task, res -> {
            showMessage(res);
            refreshApps();
        });
    }

    private static class DateRenderer extends DefaultTableCellRenderer {
        private static final DateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, df.format(value), isSelected, hasFocus, row, column);
        }
    }
}
