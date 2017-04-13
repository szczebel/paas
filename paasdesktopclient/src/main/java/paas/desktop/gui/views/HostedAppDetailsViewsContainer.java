package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.remoting.PaasRestClient;
import paas.shared.Links;
import paas.shared.dto.HostedAppInfo;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.layout.cards.CardPanel;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static paas.shared.dto.HostedAppInfo.getId;
import static swingutils.components.ComponentFactory.decorate;

@Component
public class HostedAppDetailsViewsContainer extends LazyInitRichAbstractView {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private PaasRestClient paasRestClient;
    @Autowired
    private LoginData loginData;

    private CardPanel cardPanel;
    private Map<Long, HostedAppDetailsView> tabsMap = new HashMap<>();

    @Override
    protected JComponent wireAndLayout() {
        eventBus.whenDetailsRequested(this::openDetailsView);
        eventBus.whenLoginChanged(this::closeAll);
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
        long appId = getId(appInfo);
        if (tabsMap.containsKey(appId)) {
            cardPanel.showCard(String.valueOf(appId));
        } else {
            HostedAppDetailsView hostedAppDetailsView = new HostedAppDetailsView(eventBus, appInfo, paasRestClient, getKibanaUrl());
            tabsMap.put(appId, hostedAppDetailsView);

            String key = String.valueOf(appId);
            cardPanel.addCard(key,
                    decorate(hostedAppDetailsView.getComponent())
                            .withTitledSeparator("Manage application of ID : " + key, () -> closeView(appId), null)
                            .withEmptyBorder(2, 4, 4, 4)
                            .get());
            cardPanel.showCard(key);
            hostedAppDetailsView.refreshLogs();
        }
    }

    private String getKibanaUrl() {
        return loginData.getServerUrl() + Links.KIBANA;
    }
}
