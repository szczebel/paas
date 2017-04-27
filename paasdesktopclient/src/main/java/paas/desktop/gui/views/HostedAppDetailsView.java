package paas.desktop.gui.views;

import org.slf4j.LoggerFactory;
import paas.desktop.dto.DatedMessage;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.remoting.PaasRestClient;
import paas.shared.Links;
import paas.shared.dto.HostedAppInfo;
import swingutils.background.BackgroundOperation;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.console.RollingConsole;
import swingutils.components.progress.ProgressIndicator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static java.awt.Font.BOLD;
import static javax.swing.SwingConstants.LEFT;
import static paas.shared.dto.HostedAppInfo.getId;
import static swingutils.components.ComponentFactory.*;
import static swingutils.layout.LayoutBuilders.*;

public class HostedAppDetailsView extends LazyInitRichAbstractView {
    private final EventBus eventBus;
    private final PaasRestClient paasRestClient;
    private final String KIBANA_URL;
    private final String MONITORING_URL;
    private final RollingConsole sysout = new RollingConsole(1000);
    private HostedAppInfo appInfo;
    private long lastMessageTimestamp = 0;
    private JCheckBox autorefresh;
    private JLabel tailErrors;
    private Timer timer;
    private ControlPanel controlPanel;

    HostedAppDetailsView(EventBus eventBus, HostedAppInfo appInfo, PaasRestClient paasRestClient, String kibanaUrl, String monitoringUrl) {
        this.eventBus = eventBus;
        this.appInfo = appInfo;
        this.paasRestClient = paasRestClient;
        KIBANA_URL = kibanaUrl;
        MONITORING_URL = monitoringUrl;
    }

    private void startTimer() {
        timer = new Timer(1000, e -> refreshLogs());
        timer.setInitialDelay(1000);
        timer.setRepeats(true);
        timer.start();
    }

    private void stopTimer() {
        timer.stop();
        timer = null;
    }

    @Override
    protected JComponent wireAndLayout() {
        controlPanel = new ControlPanel();
        tailErrors = label("");
        autorefresh = new JCheckBox("Autorefresh (a.k.a. tail -f)");
        autorefresh.addActionListener(e -> {
            if (autorefresh.isSelected()) startTimer();
            else stopTimer();
        });
        JComponent logs = borderLayout()
                .center(sysout.getComponent())
                .south(
                        borderLayout()
                                .center(tailErrors)
                                .west(
                                        hBox(4,
                                                button("Refresh logs", e -> refreshLogs()),
                                                autorefresh)
                                )
                                .build()
                )
                .build();

        return borderLayout()
                .west(decorate(controlPanel.getComponent()).withEmptyBorder(4, 4, 4, 4).get())
                .center(decorate(logs).withEmptyBorder(4, 4, 4, 4).get())
                .build();
    }

    private void openKibana() {
        openUrl(Links.substitute(KIBANA_URL, appInfo.getHostedAppDesc().getId()));
    }

    private void openMonitoring() {
        openUrl(Links.substitute(MONITORING_URL, appInfo.getHostedAppDesc().getId()));
    }

    private void openUrl(String urlString) {
        try {
            URI url = new URI(urlString);
            Desktop.getDesktop().browse(url);
        } catch (URISyntaxException | IOException e) {
            LoggerFactory.getLogger(getClass()).error("Can't open url", e);
        }
    }

    void appUpdated(HostedAppInfo appInfo) {
        long updatedAppId = HostedAppInfo.getId(appInfo);
        long myAppId = HostedAppInfo.getId(this.appInfo);
        if (updatedAppId != myAppId)
            throw new IllegalArgumentException("My app id: " + myAppId + ", updates app id: " + updatedAppId);
        //else
        this.appInfo = appInfo;
        controlPanel.appUpdated();
    }

    private void refreshLogs() {
        BackgroundOperation.execute(
                () -> paasRestClient.tailNewerThan(appInfo.getHostedAppDesc().getId(), lastMessageTimestamp),
                this::newTailReceived,
                this::logRootCause,
                ProgressIndicator.NoOp
        );
    }

    private void newTailReceived(List<DatedMessage> newOutput) {
        tailErrors.setText("");
        if (newOutput.isEmpty()) return;
        lastMessageTimestamp = newOutput.get(newOutput.size() - 1).getTimestamp();
        newOutput.stream().map(DatedMessage::getMessage).forEach(sysout::appendLine);
    }

    private void logRootCause(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        tailErrors.setText("Refreshing tail failed. " + e.getMessage());
        LoggerFactory.getLogger(getClass()).warn("Refreshing tail failed", e);
    }

    void dispose() {
        if (timer != null) stopTimer();
    }

    class ControlPanel extends LazyInitRichAbstractView {
        private final DateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

        private JLabel appStatus = label(getStatusText(), LEFT, BOLD);
        private RedeployForm redeployForm = new RedeployForm() {
            @Override
            protected ProgressIndicator getProgressIndicator() {
                return ControlPanel.this.getProgressIndicator();
            }
        };

        private String getStatusText() {
            Date started = appInfo.getHostedAppStatus().getStarted();
            return "Running " + (appInfo.getHostedAppStatus().isRunning() ? "since " + df.format(started) : ": no");
        }

        void appUpdated() {
            appStatus.setText(getStatusText());
            redeployForm.populateView(appInfo.getHostedAppDesc());
        }

        @Override
        protected JComponent wireAndLayout() {
            JComponent redeployComponent = redeployForm.getComponent();
            redeployForm.populateView(appInfo.getHostedAppDesc());
            return borderLayout()
                    .north(vBox(4,
                            appStatus,
                            button("Browse logs in Kibana (if ELK provisioned)", HostedAppDetailsView.this::openKibana),
                            button("Open monitoring (if provisioned)", HostedAppDetailsView.this::openMonitoring),
                            gridLayout(1, 3,
                                    button("Restart", this::restart),
                                    button("Stop", this::stop),
                                    button("Undeploy", this::undeploy)
                            ))
                    )
                    .center(decorate(redeployComponent).withTitledSeparator("Reconfigure deployment:").get())
                    .build();
        }

        private void restart() {
            updateApp(() -> paasRestClient.restart(getId(appInfo)));
        }

        private void stop() {
            updateApp(() -> paasRestClient.stop(getId(appInfo)));
        }

        private void undeploy() {
            updateApp(() -> paasRestClient.undeploy(getId(appInfo)));
        }

        private void updateApp(Callable<String> task) {
            inBackground(task, res -> eventBus.dispatchEvent(Events.APP_UPDATED));
        }
    }

    class RedeployForm extends DeployView {
        RedeployForm() {
            super(eventBus, paasRestClient);
        }

        @Override
        protected ValidationErrors validate(DeployFormObject fo) {
            return ValidationErrors.empty();//allow no file selected
        }

        @Override
        protected String deploy(DeployFormObject fo) throws IOException, InterruptedException {
            return paasRestClient.redeploy(appInfo.getHostedAppDesc().getId(), fo.jarFile, fo.commandLineArgs, fo.requestedProvisions);
        }

        @Override
        String getDeployLabel() {
            return "Redeploy";
        }
    }
}
