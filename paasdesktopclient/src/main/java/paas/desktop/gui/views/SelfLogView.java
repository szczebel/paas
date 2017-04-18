package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swingutils.SysoutInterceptor;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.console.RollingConsole;

import javax.swing.*;
import java.awt.*;

@Component
public class SelfLogView extends LazyInitRichAbstractView {

    @Autowired
    SysoutInterceptor sysoutInterceptor;

    @Override
    protected JComponent wireAndLayout() {
        RollingConsole rollingConsole = new RollingConsole(1000);
        //todo: doesn;t work, packed size too wide
        rollingConsole.getComponent().setPreferredSize(new Dimension(800, 600));
        sysoutInterceptor.registerSwingConsumer(rollingConsole::append);
        return rollingConsole.getComponent();
    }
}
