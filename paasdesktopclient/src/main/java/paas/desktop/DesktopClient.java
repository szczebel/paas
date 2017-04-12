package paas.desktop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;
import paas.desktop.gui.MainFrame;
import swingutils.SysoutInterceptor;
import swingutils.components.ComponentFactory;

import javax.swing.*;
import java.io.IOException;

//todo: remember last host&username

@SpringBootApplication
public class DesktopClient {

    public static final SysoutInterceptor sysoutInterceptor = new SysoutInterceptor();

    public static void main(String[] args) throws IOException {
        sysoutInterceptor.interceptSystemOutAndErr();
        ComponentFactory.initLAF();
        new SpringApplicationBuilder(DesktopClient.class).headless(false).run(args);
    }

    @Component
    protected static class GuiEntryPoint implements CommandLineRunner {
        @Autowired
        MainFrame mainFrame;

        @Override
        public void run(String... strings) throws Exception {
            SwingUtilities.invokeLater(mainFrame::buildAndShow);
        }


    }
}
