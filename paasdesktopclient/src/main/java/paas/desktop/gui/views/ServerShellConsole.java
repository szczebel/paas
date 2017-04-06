package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.DatedMessage;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.RollingConsole;
import swingutils.components.progress.BusyFactory;
import swingutils.components.progress.ProgressIndicatingContainer;
import swingutils.components.progress.ProgressIndicator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static swingutils.components.ComponentFactory.blockCaret;
import static swingutils.components.ComponentFactory.button;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.flowLayout;

@Component
public class ServerShellConsole extends LazyInitRichAbstractView {

    @Autowired private EventBus eventBus;
    @Autowired private HttpPaasClient httpPaasClient;

    private RollingConsole output;
    private ProgressIndicator commandProgressIndicator;
    private long lastMessageTimestamp = 0;
    private JTextField commandField;


    @Override
    protected JComponent wireAndLayout() {
        output = new RollingConsole(1000);

        return borderLayout()
                .north(toolbar())
                .center(output.getComponent())
                .south(commandbar())
                .build();
    }

    private JComponent commandbar() {
        ProgressIndicatingContainer c = BusyFactory.progressBarOverlay();
        commandProgressIndicator = c;
        commandField = new JTextField(20);
        commandField.setFont(output.getFont());
        commandField.setBackground(Color.black);
        commandField.setForeground(Color.green);
        commandField.setCaretColor(Color.green);
        commandField.setCaret(blockCaret());
        commandField.addActionListener(e -> sendCommand(commandField.getText()));
        c.getContentPane().add(commandField);
        return c.getComponent();
    }

    private JComponent toolbar() {
        return flowLayout(button("Refresh", this::refresh));
    }

    private void refresh() {
        inBackground(
                () -> httpPaasClient.getShellOutputNewerThan(lastMessageTimestamp),
                this::newOutputReceived,
                ProgressIndicator.NoOp
        );
    }

    private void newOutputReceived(List<DatedMessage> newOutput) {
        if (newOutput.isEmpty()) return;
        lastMessageTimestamp = newOutput.get(newOutput.size() - 1).getTimestamp();
        newOutput.stream().map(DatedMessage::getMessage).forEach(output::appendLine);
    }

    private void sendCommand(String command) {
        inBackground(
                () -> httpPaasClient.executeShellCommand(command),
                this::commandSent,
                commandProgressIndicator
        );
    }

    private void commandSent(String response) {
        commandField.setText("");
        refresh();
    }

    public void focus() {
        commandField.requestFocus();
    }
}
