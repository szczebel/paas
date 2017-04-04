package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.HostedAppInfo;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.SwingConstants.TOP;
import static swingutils.layout.LayoutBuilders.tabbedPane;

@Component
public class TailViewsContainer extends LazyInitRichAbstractView {

    @Autowired private EventBus eventBus;
    @Autowired private HttpPaasClient httpPaasClient;

    private JTabbedPane tabs;
    private Map<Integer, TailView> tabsMap = new HashMap<>();
    Timer timer;

    //todo: on server change: close all
    //todo: on apps change: close the ones that are no more

    @Override
    protected JComponent wireAndLayout() {
        timer = new Timer(1000, e -> refreshAll());
        timer.setInitialDelay(1000);
        timer.setRepeats(true);
        timer.start();
        eventBus.whenTailRequested(this::showTailFor);
        tabs = tabbedPane(TOP).build();
        return tabs;
    }

    private void showTailFor(HostedAppInfo appInfo) {
        if(tabsMap.containsKey(appInfo.getId())) {
            tabs.setSelectedComponent(tabsMap.get(appInfo.getId()).getComponent());
        } else {
            TailView tailView = new TailView(appInfo, httpPaasClient);
            tabsMap.put(appInfo.getId(), tailView);
            tabs.addTab("App ID : " + appInfo.getId(), tailView.getComponent());
            tabs.setSelectedComponent(tailView.getComponent());
        }
    }

    private void refreshAll() {
        tabsMap.values().forEach(TailView::refresh);
    }
}
