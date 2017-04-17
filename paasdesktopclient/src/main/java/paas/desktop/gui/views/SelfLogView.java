package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swingutils.SysoutInterceptor;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.console.RollingConsole;

import javax.swing.*;

@Component
public class SelfLogView extends LazyInitRichAbstractView {

    @Autowired
    SysoutInterceptor sysoutInterceptor;

    @Override
    protected JComponent wireAndLayout() {
        RollingConsole rollingConsole = new RollingConsole(1000);
        rollingConsole.getComponent();
        sysoutInterceptor.registerSwingConsumer(rollingConsole::append);
        return rollingConsole.getComponent();
    }
}
