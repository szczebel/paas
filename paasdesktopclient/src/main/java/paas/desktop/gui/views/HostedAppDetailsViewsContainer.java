package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.PaasRestClient;
import paas.dto.HostedAppInfo;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.layout.cards.CardPanel;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static paas.dto.HostedAppInfo.getId;
import static swingutils.components.ComponentFactory.decorate;

@Component
public class HostedAppDetailsViewsContainer extends LazyInitRichAbstractView {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private PaasRestClient paasRestClient;
    @Value("${server.url}")
    private String serverUrl;

    private CardPanel cardPanel;
    private Map<Long, HostedAppDetailsView> tabsMap = new HashMap<>();

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenDetailsRequested(this::openDetailsView);
        eventBus.whenServerChanged(s -> {
            serverUrl = s;
            closeAll();
        });
        eventBus.whenCurrentAppsChanged(this::currentAppsChanged);

        cardPanel = new CardPanel();
        cardPanel.addCard("EMPTY", decorate(new JPanel())
                .withTitledSeparator("Hosted application details")
                .withEmptyBorder(2, 4, 4, 4)
                .get());
        return cardPanel.getComponent();
    }

    private void currentAppsChanged(Collection<HostedAppInfo> currentApps) {
        List<Long> currentAppIds = currentApps.stream()
                .map(HostedAppInfo::getId)
                .collect(toList());
        for (Long appId : tabsMap.keySet().stream().collect(toList())) {
            if (!currentAppIds.contains(appId)) {
                closeView(appId);
            }
        }
    }

    private void closeView(long appId) {
        HostedAppDetailsView removed = tabsMap.remove(appId);
        removed.dispose();
        cardPanel.removeCard(String.valueOf(appId));
    }

    private void closeAll() {
        tabsMap.keySet().stream().collect(toList()).forEach(this::closeView);
    }

    private void openDetailsView(HostedAppInfo appInfo) {
        if (tabsMap.containsKey(getId(appInfo))) {
            cardPanel.showCard(String.valueOf(getId(appInfo)));
        } else {
            HostedAppDetailsView hostedAppDetailsView = new HostedAppDetailsView(eventBus, appInfo, paasRestClient, getKibanaUrl());
            tabsMap.put(getId(appInfo), hostedAppDetailsView);

            String key = String.valueOf(getId(appInfo));
            cardPanel.addCard(key,
                    decorate(hostedAppDetailsView.getComponent())
                            .withTitledSeparator("Manage application of ID : " + key)
                            .withEmptyBorder(2, 4, 4, 4)
                            .get());
            cardPanel.showCard(key);
            hostedAppDetailsView.refreshLogs();
        }
    }

    private String getKibanaUrl() {
        return serverUrl + "/kibana/";
    }
}
