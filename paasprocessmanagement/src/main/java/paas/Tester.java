package paas;

import paas.procman.DatedMessage;
import paas.procman.HostedApp;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Tester {

    public static void main(String[] args) {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.showOpenDialog(null);
//        File selectedFile = fileChooser.getSelectedFile();
        File selectedFile = new File(
        "C:\\Users\\Adam_Szczeblewski\\IdeaProjects\\elevators-epam-fresh\\elevatorcontestwebgroup\\elevatorcontestwebrestserver\\target\\elevator-hackaton-web-rest-server-1.1.jar"
        );
        System.out.println(selectedFile);

        String commandline = JOptionPane.showInputDialog("Additional commandline");

        HostedApp hostedApp = new HostedApp(0, selectedFile, commandline, Paths.get(System.getProperty("user.home")).toFile());

        JFrame f = new JFrame("Littel PaaS");
        final Show show = new Show(hostedApp, f);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel contentPane = new JPanel(new FlowLayout());
        f.setContentPane(contentPane);
        f.add(button("Start", hostedApp::start));
        f.add(button("Check if running", show::status));
        f.add(button("Stop", hostedApp::stop));
        f.add(button("Tail out", show::tailOut));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static JButton button(String start, Action action) {
        final JButton button = new JButton(start);
        button.addActionListener(e -> {
            try {
                action.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(button, ex.getClass().getName() + " : " + ex.getMessage());
            }
        });
        return button;
    }

    interface Action {
        void execute() throws Exception;
    }

    static class Show {
        final HostedApp app;
        final Component parent;

        Show(HostedApp app, Component parent) {
            this.app = app;
            this.parent = parent;
        }

        void status() {
            JOptionPane.showMessageDialog(parent, "Is running? " + app.isRunning());
        }

        void tailOut() throws IOException {
            showTail(app.tailSysout(0).stream().map(DatedMessage::getMessage).collect(Collectors.toList()));
        }

        private void showTail(List<String> strings) {
            JOptionPane.showMessageDialog(parent,
                    new JScrollPane(new JList<>(strings.toArray()))
            );
        }
    }
}
