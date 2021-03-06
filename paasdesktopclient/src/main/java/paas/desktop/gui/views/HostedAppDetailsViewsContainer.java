package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.gui.infra.events.Events.AppsChanged;
import paas.desktop.gui.infra.events.Events.ShowDetails;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.remoting.PaasRestClient;
import paas.shared.Links;
import paas.shared.dto.HostedAppInfo;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.layout.cards.CardPanel;

import javax.swing.*;
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
        eventBus.when(ShowDetails.class, this::openDetailsView);
        eventBus.when(AppsChanged.class, this::updateViews);
        eventBus.when(Events.LOGIN_CHANGED, this::closeAll);

        cardPanel = new CardPanel();
        cardPanel.addCard("EMPTY", decorate(new JPanel())
                .withTitledSeparator("Hosted application details")
                .withEmptyBorder(2, 4, 4, 4)
                .get());
        return cardPanel.getComponent();
    }

    private void updateViews(AppsChanged e) {
        List<Long> openedViews = tabsMap.keySet().stream().collect(toList());
        List<Long> currentAppIds = e.apps.stream()
                .map(HostedAppInfo::getId)
                .collect(toList());
        
        for (Long appIdWithOpenedDetails : openedViews) {
            if (!currentAppIds.contains(appIdWithOpenedDetails)) {
                closeView(appIdWithOpenedDetails);
            }
        }
        for (HostedAppInfo app : e.apps) {
            HostedAppDetailsView view = tabsMap.get(HostedAppInfo.getId(app));
            if(view != null) view.appUpdated(app);
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

    private void openDetailsView(ShowDetails e) {
        HostedAppInfo appInfo = e.app;
        long appId = getId(appInfo);
        if (tabsMap.containsKey(appId)) {
            cardPanel.showCard(String.valueOf(appId));
        } else {
            HostedAppDetailsView hostedAppDetailsView =
                    new HostedAppDetailsView(eventBus, appInfo, paasRestClient, getKibanaUrl(), getMonitoringUrl());
            tabsMap.put(appId, hostedAppDetailsView);

            String key = String.valueOf(appId);
            cardPanel.addCard(key,
                    decorate(hostedAppDetailsView.getComponent())
                            .withTitledSeparator("Manage application of ID : " + key, () -> closeView(appId), null)
                            .withEmptyBorder(2, 4, 4, 4)
                            .get());
            cardPanel.showCard(key);
        }
    }

    private String getKibanaUrl() {
        return loginData.getServerUrl() + Links.KIBANA;
    }

    private String getMonitoringUrl() {
        return loginData.getServerUrl() + Links.MONITOR;
    }
}
