package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.ShellOutput;
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


        JTextField textField = new JTextField(20);
        textField.setFont(output.getFont());
        textField.setBackground(Color.black);
        textField.setForeground(Color.green);
        textField.setCaretColor(Color.green);//todo make it a blinking block
        textField.addActionListener(e -> sendCommand(textField.getText(), () -> {
            textField.setText("");
            refresh();
        }));
        c.getContentPane().add(textField);
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

    private void newOutputReceived(List<ShellOutput> newOutput) {
        if (newOutput.isEmpty()) return;
        lastMessageTimestamp = newOutput.get(newOutput.size() - 1).getTimestamp();
        newOutput.stream().map(ShellOutput::getOutputLine).forEach(output::appendLine);
    }

    private void sendCommand(String command, Runnable done) {
        inBackground(
                () -> httpPaasClient.executeShellCommand(command),
                s -> done.run(),
                commandProgressIndicator
        );
    }


}
