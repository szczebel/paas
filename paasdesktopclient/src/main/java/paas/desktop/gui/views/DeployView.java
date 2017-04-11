package paas.desktop.gui.views;

import com.jgoodies.forms.builder.FormBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;
import paas.desktop.remoting.PaasRestClient;
import paas.dto.HostedAppDesc;
import paas.dto.HostedAppRequestedProvisions;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.SwingConstants.RIGHT;
import static swingutils.components.ComponentFactory.button;
import static swingutils.components.ComponentFactory.label;

@Component
public class DeployView extends LazyInitRichAbstractView {

    private final EventBus eventBus;
    private final PaasRestClient paasRestClient;

    private JTextField fileName;
    private JTextField commandLine;
    private JFileChooser fileChooser;
    private JCheckBox wantsDb;
    private JCheckBox wantsStorage;
    private JCheckBox wantsLogstash;
    private JCheckBox wantsLogging;

    @Autowired
    public DeployView(EventBus eventBus, PaasRestClient paasRestClient) {
        this.eventBus = eventBus;
        this.paasRestClient = paasRestClient;
    }

    @Override
    protected JComponent wireAndLayout() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java artifacts", "jar", "war"));
        fileName = new JTextField(10);
        fileName.setEditable(false);
        commandLine = new JTextField(10);
        wantsDb = new JCheckBox("I want a database (H2)");
        wantsStorage = new JCheckBox("I want disk space");
        wantsLogstash = new JCheckBox("I want a Logstash url");
        wantsLogging = new JCheckBox("I want my System.out logs published to ELK");

        return FormBuilder.create()
                .columns("pref:none, ${label-component-gap}, pref:grow, ${label-component-gap}, pref:none")
                .rows("pref:none, $lg, " +
                        "pref:none, $lg, " +
                        "pref:none, $lg, " +
                        "pref:none, $lg, " +
                        "pref:none, $lg, " +
                        "pref:none, $lg, " +
                        "pref:none, $lg, " +
                        "pref:none")
                .add(label("Jar to deploy:", RIGHT)).xy(1, 1)
                .add(fileName).xy(3, 1)
                .add(button("Change...", this::selectFile)).xy(5, 1)
                .add(label("Command line:", RIGHT)).xy(1, 3)
                .add(commandLine).xyw(3, 3, 3)
                .add(label("Provisions:")).xyw(1, 5, 5)
                .add(wantsDb).xyw(1, 7, 5)
                .add(wantsStorage).xyw(1, 9, 5)
                .add(wantsLogstash).xyw(1, 11, 5)
                .add(wantsLogging).xyw(1, 13, 5)
                .add(button(getDeployLabel(), this::deployClick)).xyw(1, 15, 5)
                .build();
    }

    String getDeployLabel() {
        return "Deploy";
    }

    private void selectFile() {
        int i = fileChooser.showOpenDialog(fileName);
        if (i == JFileChooser.APPROVE_OPTION) {
            fileName.setText(fileChooser.getSelectedFile().getName());
        }
    }

    private DeployFormObject populateFormObject() {
        return new DeployFormObject(fileChooser.getSelectedFile(), commandLine.getText(),
                new HostedAppRequestedProvisions(
                        wantsDb.isSelected(),
                        wantsStorage.isSelected(),
                        wantsLogstash.isSelected(),
                        wantsLogging.isSelected()
                ));
    }

    void populateView(HostedAppDesc desc) {
        fileChooser.setSelectedFile(null);
        fileName.setText(desc.getJarFile());
        commandLine.setText(desc.getCommandLineArgs());
        wantsDb.setSelected(desc.getRequestedProvisions().isWantsDB());
        wantsStorage.setSelected(desc.getRequestedProvisions().isWantsFileStorage());
        wantsLogstash.setSelected(desc.getRequestedProvisions().isWantsLogstash());
        wantsLogging.setSelected(desc.getRequestedProvisions().isWantsLogging());
    }

    private void deployClick() {
        DeployFormObject fo = populateFormObject();
        ValidationErrors errors = validate(fo);
        if (errors.isEmpty()) {
            inBackground(() -> deploy(fo),
            this::deployed);
        } else {
            errors.errorMessages.forEach(error ->
                    JOptionPane.showMessageDialog(getComponent(), error));
        }
    }

    protected ValidationErrors validate(DeployFormObject fo) {
        ValidationErrors errors = new ValidationErrors();
        if(fo.jarFile == null) errors.addError("No file selected");
        return errors;
    }

    protected String deploy(DeployFormObject fo) throws IOException, InterruptedException {
        return paasRestClient.deploy(fo.jarFile, fo.commandLineArgs, fo.requestedProvisions);
    }

    private void deployed(String statusMessage) {
        eventBus.appUpdated();
    }

    @Override
    protected String formatExceptionToShow(Throwable ex) {
        return "Deployment failed. " + super.formatExceptionToShow(ex);
    }

    static class DeployFormObject {
        final File jarFile;
        final String commandLineArgs;
        final HostedAppRequestedProvisions requestedProvisions;

        DeployFormObject(File jarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) {
            this.jarFile = jarFile;
            this.commandLineArgs = commandLineArgs;
            this.requestedProvisions = requestedProvisions;
        }
    }

    static class ValidationErrors {

        static ValidationErrors empty() {
            return new ValidationErrors();
        }

        final List<String> errorMessages = new ArrayList<>();

        void addError(String error) {
            errorMessages.add(error);
        }

        boolean isEmpty() {
            return errorMessages.isEmpty();
        }
    }
}
