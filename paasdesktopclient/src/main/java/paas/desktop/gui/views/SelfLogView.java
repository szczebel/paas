package paas.desktop.gui.views;

import org.springframework.stereotype.Component;
import paas.desktop.DesktopClient;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.console.RollingConsole;

import javax.swing.*;

@Component
public class SelfLogView extends LazyInitRichAbstractView {
    @Override
    protected JComponent wireAndLayout() {
        RollingConsole rollingConsole = new RollingConsole(1000);
        rollingConsole.getComponent();
        SwingUtilities.invokeLater(() ->
            DesktopClient.sysoutInterceptor.registerSwingConsumer(rollingConsole::append)
        );
        return rollingConsole.getComponent();
    }


}
