package paas.desktop.gui.views;

import paas.desktop.HostedAppInfo;
import paas.desktop.remoting.HttpPaasClient;
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
    private final HttpPaasClient httpPaasClient;

    private final RollingConsole sysout = new RollingConsole(100);
    private final RollingConsole connection = new RollingConsole(100);

    private JCheckBox autorefresh;
    private Timer timer;//todo websockets :>

    TailView(HostedAppInfo appInfo, HttpPaasClient httpPaasClient) {
        this.appInfo = appInfo;
        this.httpPaasClient = httpPaasClient;
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
        autorefresh = new JCheckBox("Autorefresh (a.k.a. tail -f -100");
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
                () -> httpPaasClient.tailSysout(appInfo.getId(), 100),
                res -> replace(sysout, res)
        );
    }

    private void replace(RollingConsole console, List<String> newTail) {
        console.clear();
        newTail.forEach(console::appendLine);
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
