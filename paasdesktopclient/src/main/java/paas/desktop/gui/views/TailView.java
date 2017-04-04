package paas.desktop.gui.views;

import paas.desktop.HostedAppInfo;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.background.BackgroundOperation;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.RollingConsole;

import javax.swing.*;
import java.util.List;

import static javax.swing.SwingConstants.RIGHT;
import static swingutils.layout.LayoutBuilders.tabbedPane;

public class TailView extends LazyInitRichAbstractView {
    private final HostedAppInfo appInfo;
    private final HttpPaasClient httpPaasClient;

    private final RollingConsole sysout = new RollingConsole(100);
    private final RollingConsole syserr = new RollingConsole(100);
    private final RollingConsole connection = new RollingConsole(100);

    TailView(HostedAppInfo appInfo, HttpPaasClient httpPaasClient) {
        this.appInfo = appInfo;
        this.httpPaasClient = httpPaasClient;
    }

    @Override
    protected JComponent wireAndLayout() {
        return tabbedPane(RIGHT)
                .addTab("System.out", sysout.getComponent())
                .addTab("System.err", syserr.getComponent())
                .addTab("Connection", connection.getComponent())
                .build();
    }

    void refresh() {
        BackgroundOperation.execute(
                () -> httpPaasClient.tailSysout(appInfo.getId(), 100),
                res -> replace(sysout, res),
                this::onException
        );
        BackgroundOperation.execute(
                () -> httpPaasClient.tailSyserr(appInfo.getId(), 100),
                res -> replace(syserr, res),
                this::onException
        );
    }

    private void replace(RollingConsole console, List<String> newTail) {
        console.clear();
        newTail.forEach(console::appendLine);
    }

    @Override
    protected void onException(Throwable e) {
        while(e.getCause() != null) {
            e = e.getCause();
        }
        connection.appendLine(e.getClass().getSimpleName() + " : " + e.getMessage());
    }
}
