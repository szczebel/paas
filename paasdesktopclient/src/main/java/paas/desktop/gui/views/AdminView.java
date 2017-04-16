package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.dto.DatedMessage;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.remoting.PaasRestClient;
import swingutils.components.LazyInitRichAbstractView;
import swingutils.components.console.AsyncCommandConsole;
import swingutils.components.progress.ProgressIndicator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.swing.SwingConstants.RIGHT;
import static swingutils.components.ComponentFactory.button;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.flowLayout;

@Component
public class AdminView extends LazyInitRichAbstractView {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private PaasRestClient paasRestClient;

    private AsyncCommandConsole shellConsole;
    private long lastMessageTimestamp = 0;

    @Override
    protected JComponent wireAndLayout() {
        shellConsole = new AsyncCommandConsole("  Shell command  > ", 1000, this::executeCommand);

        return borderLayout()
                .center(shellConsole.getComponent())
                .south(commandBar())
                .build();
    }

    private JComponent commandBar() {
        return flowLayout(RIGHT,
                button("Refresh output", this::refresh),
                button("Clear output", this::clear),
                button("Upload PaasDesktopClient.jar", this::upload)
        );
    }

    private void upload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java artifacts", "jar", "war"));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(getComponent())) {
            inBackground(
                    () -> paasRestClient.uploadDesktopClientJar(fileChooser.getSelectedFile()),
                    s -> {
                    }
            );
        }
        focus();
    }

    private void refresh() {
        inBackground(
                () -> paasRestClient.getShellOutputNewerThan(lastMessageTimestamp),
                this::newOutputReceived,
                ProgressIndicator.NoOp
        );
        focus();
    }

    private void clear() {
        shellConsole.getOutput().clear();
        focus();
    }

    private void newOutputReceived(List<DatedMessage> newOutput) {
        if (newOutput.isEmpty()) return;
        lastMessageTimestamp = newOutput.get(newOutput.size() - 1).getTimestamp();
        newOutput.stream().map(DatedMessage::getMessage).forEach(m -> shellConsole.getOutput().appendLine(m));
    }

    private String executeCommand(String command) {
        paasRestClient.executeShellCommand(command);
        List<DatedMessage> newOutput = paasRestClient.getShellOutputNewerThan(lastMessageTimestamp);
        if (newOutput.isEmpty()) return null;
        lastMessageTimestamp = newOutput.get(newOutput.size() - 1).getTimestamp();
        List<String> strings = newOutput.stream().map(DatedMessage::getMessage).collect(toList());
        return String.join(System.lineSeparator(), strings);
    }


    public void focus() {
        shellConsole.focus();
    }
}
