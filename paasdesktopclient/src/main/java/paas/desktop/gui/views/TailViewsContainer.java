package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.HostedAppInfo;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.swing.SwingConstants.TOP;
import static swingutils.layout.LayoutBuilders.tabbedPane;

@Component
public class TailViewsContainer extends LazyInitRichAbstractView {

    @Autowired private EventBus eventBus;
    @Autowired private HttpPaasClient httpPaasClient;

    private JTabbedPane tabs;
    private Map<Long, TailView> tabsMap = new HashMap<>();

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenTailRequested(this::showTailFor);
        eventBus.whenServerChanged(s -> closeAll());
        eventBus.whenCurrentAppsChanged(this::currentAppsChanged);
        tabs = tabbedPane(TOP).build();
        return tabs;
    }

    private void currentAppsChanged(Collection<HostedAppInfo> currentApps) {
        List<Long> currentAppIds = currentApps.stream().map(HostedAppInfo::getId).collect(Collectors.toList());
        for (Long appId : tabsMap.keySet()) {
            if(!currentAppIds.contains(appId)) {
                closeTailView(appId);
            }
        }
    }

    private void closeTailView(long appId) {
        TailView removed = tabsMap.remove(appId);
        removed.dispose();
        tabs.remove(removed.getComponent());
    }

    private void closeAll() {
        tabsMap.keySet().forEach(this::closeTailView);
    }

    private void showTailFor(HostedAppInfo appInfo) {
        if(tabsMap.containsKey(appInfo.getId())) {
            TailView tailView = tabsMap.get(appInfo.getId());
            tabs.setSelectedComponent(tailView.getComponent());
            tailView.refresh();
        } else {
            TailView tailView = new TailView(appInfo, httpPaasClient);
            tabsMap.put(appInfo.getId(), tailView);
            tabs.addTab("App ID : " + appInfo.getId(), tailView.getComponent());
            tabs.setSelectedComponent(tailView.getComponent());
            tailView.refresh();
        }
    }
}
