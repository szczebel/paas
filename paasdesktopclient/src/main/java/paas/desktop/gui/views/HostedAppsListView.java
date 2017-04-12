package paas.desktop.gui.views;

import ca.odell.glazedlists.EventList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.PaasRestClient;
import paas.shared.dto.HostedAppInfo;
import swingutils.EventListHelper;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.table.TableFactory;
import swingutils.components.table.TablePanel;
import swingutils.components.table.descriptor.Columns;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static swingutils.EventListHelper.eventList;
import static swingutils.components.ComponentFactory.button;

@Component
public class HostedAppsListView extends LazyInitRichAbstractView {

    private final EventList<HostedAppInfo> apps = eventList();
    @Autowired
    private PaasRestClient paasRestClient;
    @Autowired
    private EventBus eventBus;

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenLoginChanged(newUrl -> this.onServerChange());
        eventBus.whenAppUpdated(this::refreshApps);

        Columns<HostedAppInfo> columns = Columns.create(HostedAppInfo.class)
                .column("App ID", Long.class, HostedAppInfo::getId)
                .column("Owner", String.class, HostedAppInfo::getOwner)
                .column("Jar file", String.class, HostedAppInfo::getJarFile)
                .column("Command line", String.class, HostedAppInfo::getCommandLineArgs)
                .column("Start time", Date.class, HostedAppInfo::getStarted)
                .column("Running", Boolean.class, HostedAppInfo::isRunning);
        TablePanel<HostedAppInfo> tablePanel = TableFactory.createTablePanel(apps, columns);
        tablePanel.getToolbar().removeAll();
        tablePanel.getToolbar().add(button("Refresh", this::refreshApps));
        tablePanel.getTable().setDefaultRenderer(Date.class, new DateRenderer());
        tablePanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            HostedAppInfo selection = tablePanel.getSelection();
            if (selection != null && !e.getValueIsAdjusting()) eventBus.showDetails(selection);
        });

        return tablePanel.getComponent();
    }

    private void onServerChange() {
        EventListHelper.clearEventList(apps);
        refreshApps();
    }

    private void refreshApps() {
        inBackground(paasRestClient::getHostedApplications, this::appsFetched);
    }

    private void appsFetched(List<HostedAppInfo> apps) {
        EventListHelper.replaceContent(this.apps, apps);
        eventBus.currentAppsChanged(apps);
    }


    private static class DateRenderer extends DefaultTableCellRenderer {
        private static final DateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, df.format(value), isSelected, hasFocus, row, column);
        }
    }
}
