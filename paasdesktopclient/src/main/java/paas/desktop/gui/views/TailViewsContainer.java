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

import static swingutils.layout.LayoutBuilders.tabbedPane;

@Component
public class TailViewsContainer extends LazyInitRichAbstractView {

    @Autowired private EventBus eventBus;
    @Autowired private HttpPaasClient httpPaasClient;

    private JTabbedPane tabs;
    private Map<Integer, TailView> tabsMap = new HashMap<>();

    //todo: on server change: close all

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenTailRequested(this::showTailFor);
        tabs = tabbedPane(JTabbedPane.BOTTOM).build();
        return tabs;
    }

    private void showTailFor(HostedAppInfo appInfo) {
        if(tabsMap.containsKey(appInfo.getId())) {
            tabs.setSelectedComponent(tabsMap.get(appInfo.getId()).getComponent());
        } else {
            TailView tailView = new TailView(appInfo);
            tabsMap.put(appInfo.getId(), tailView);
            tabs.addTab("App ID : " + appInfo.getId(), tailView.getComponent());
        }
    }
}
