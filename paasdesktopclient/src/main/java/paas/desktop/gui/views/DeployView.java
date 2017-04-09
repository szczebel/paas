package paas.desktop.gui.views;

import com.jgoodies.forms.builder.FormBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.concurrent.Callable;

import static javax.swing.SwingConstants.RIGHT;
import static swingutils.components.ComponentFactory.button;
import static swingutils.components.ComponentFactory.label;

@Component
public class DeployView extends LazyInitRichAbstractView {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private HttpPaasClient httpPaasClient;

    private JTextField fileName;
    private JTextField commandLine;
    private JFileChooser fileChooser;
    private File selectedFile;

    @Override
    protected JComponent wireAndLayout() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java artifacts", "jar", "war"));
        fileName = new JTextField(10);
        fileName.setEditable(false);
        commandLine = new JTextField(10);

        return FormBuilder.create()
                .columns("pref:none, ${label-component-gap}, pref:grow, ${label-component-gap}, pref:none")
                .rows("pref:none, $lg, pref:none, $lg, pref:none, $lg, pref:none")
                .add(label("File to deploy:", RIGHT))       .xy(1, 1)
                .add(fileName)                                  .xy(3, 1)
                .add(button("Change...", this::selectFile)).xy(5, 1)
                .add(label("Command line:", RIGHT))         .xy(1, 3)
                .add(commandLine)                              .xyw(3, 3, 3)
                .add(button("Deploy", this::deploy))       .xy(5, 5)
                .add(button("Redeploy", this::redeploy))   .xy(5, 7)
                .build();
    }

    private void selectFile() {
        int i = fileChooser.showOpenDialog(fileName);
        if (i == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileName.setText(selectedFile.getName());
        }
    }

    private void deploy() {
        send(() -> httpPaasClient.deploy(selectedFile, commandLine.getText()));
    }

    private void redeploy() {
        send(() -> httpPaasClient.redeploy(selectedFile, commandLine.getText()));
    }

    private void send(Callable<String> callable) {
        if (selectedFile != null) {
            inBackground(callable, this::deployed);
        } else {
            JOptionPane.showMessageDialog(getComponent(), "No file selected");
        }
    }

    private void deployed(String statusMessage) {
        eventBus.appDeployed();
    }
}
