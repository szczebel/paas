package paas.desktop.gui.views;

import org.springframework.stereotype.Component;
import swingutils.SysoutInterceptor;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.RollingConsole;

import javax.swing.*;

@Component
public class SelfLogView extends LazyInitRichAbstractView {
    @Override
    protected JComponent wireAndLayout() {
        RollingConsole rollingConsole = new RollingConsole(1000);
        rollingConsole.getComponent();
        SysoutInterceptor.registerSwingConsumer(rollingConsole::appendLine);
        return rollingConsole.getComponent();
    }


}
