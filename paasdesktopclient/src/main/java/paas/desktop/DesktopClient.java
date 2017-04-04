package paas.desktop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;
import paas.desktop.gui.GuiBuilder;

import javax.swing.*;

@SpringBootApplication
public class DesktopClient {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DesktopClient.class).headless(false).run(args);
    }

    @Component
    protected static class GuiEntryPoint implements CommandLineRunner {
        @Autowired
        GuiBuilder guiBuilder;

        @Override
        public void run(String... strings) throws Exception {
            SwingUtilities.invokeLater(guiBuilder::showGui);
        }


    }
}
