package paas.desktop.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.HttpPaasClient;
import swingutils.background.BackgroundOperation;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import static swingutils.components.ComponentFactory.button;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.flowLayout;
import static swingutils.layout.forms.FormLayoutBuilders.simpleForm;

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

        return simpleForm()
                        .addRow("File to deploy:", borderLayout()
                                .east(button("Change...", this::selectFile))
                                .center(fileName)
                                .build())
                        .addRow("Command line:", commandLine)
                        .addRow("", flowLayout(FlowLayout.RIGHT, button("Deploy", this::deploy)))
                        .build()
        ;
    }

    private void selectFile() {
        int i = fileChooser.showOpenDialog(fileName);
        if (i == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileName.setText(selectedFile.getName());
        }
    }

    private void deploy() {
        if (selectedFile != null) {
            BackgroundOperation.execute(
                    () -> httpPaasClient.deploy(selectedFile, commandLine.getText()),
                    this::deployed,
                    this::onException,
                    getParent()
            );
        } else {
            getParent().showAndLock("No file selected");
        }
    }

    private void deployed(String statusMessage) {
        eventBus.appDeployed();
        getParent().showAndLock(statusMessage);
    }
}
