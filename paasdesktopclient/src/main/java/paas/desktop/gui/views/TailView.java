package paas.desktop.gui.views;

import paas.desktop.dto.DatedMessage;
import paas.desktop.dto.HostedAppInfo;
import paas.desktop.remoting.PaasRestClient;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.RollingConsole;
import swingutils.components.progress.ProgressIndicator;

import javax.swing.*;
import java.util.List;

import static javax.swing.SwingConstants.LEFT;
import static swingutils.components.ComponentFactory.button;
import static swingutils.layout.LayoutBuilders.*;

public class TailView extends LazyInitRichAbstractView {
    private final HostedAppInfo appInfo;
    private final PaasRestClient paasRestClient;

    private long lastMessageTimestamp = 0;
    private final RollingConsole sysout = new RollingConsole(1000);
    private final RollingConsole connection = new RollingConsole(1000);

    private JCheckBox autorefresh;
    private Timer timer;

    TailView(HostedAppInfo appInfo, PaasRestClient paasRestClient) {
        this.appInfo = appInfo;
        this.paasRestClient = paasRestClient;
    }

    private void startTimer() {
        timer = new Timer(1000, e -> refresh());
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
        autorefresh = new JCheckBox("Autorefresh (a.k.a. tail -f");
        autorefresh.addActionListener(e -> {
            if(autorefresh.isSelected()) startTimer();
            else stopTimer();
        });
        return borderLayout()
                .north(flowLayout(button("Refresh", e -> refresh()), autorefresh))
                .center(
                        tabbedPane(LEFT)
                                .addTab("System.out", sysout.getComponent())
                                .addTab("Connection", connection.getComponent())
                                .build())
                .build();
    }

    void refresh() {
        inBackground(
                () -> paasRestClient.tailNewerThan(appInfo.getId(), lastMessageTimestamp),
                this::newTailReceived
        );
    }

    private void newTailReceived(List<DatedMessage> newOutput) {
        if (newOutput.isEmpty()) return;
        lastMessageTimestamp = newOutput.get(newOutput.size() - 1).getTimestamp();
        newOutput.stream().map(DatedMessage::getMessage).forEach(sysout::appendLine);
    }

    @Override
    protected void onException(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        connection.appendLine(e.getClass().getSimpleName() + " : " + e.getMessage());
    }

    @Override
    protected ProgressIndicator getProgressIndicator() {
        return ProgressIndicator.NoOp;
    }

    void dispose() {
        if(timer != null) stopTimer();
    }
}
