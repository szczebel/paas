package paas.desktop.gui.views;

import paas.desktop.HostedAppInfo;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static swingutils.components.ComponentFactory.label;

public class TailView extends LazyInitRichAbstractView {
    private final HostedAppInfo appInfo;

    TailView(HostedAppInfo appInfo) {

        this.appInfo = appInfo;
    }

    @Override
    protected JComponent wireAndLayout() {
        return label("Tailing");
    }
}
