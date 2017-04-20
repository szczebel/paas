package paas.desktop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import paas.desktop.gui.MainFrame;
import paas.desktop.gui.infra.version.VersionChecker;
import swingutils.spring.application.SwingApplication;
import swingutils.spring.application.SwingApplicationBootstrap;
import swingutils.spring.application.SwingEntryPoint;
import swingutils.spring.edt.EnableEDTAspects;

import java.io.IOException;

@SpringBootApplication
@SwingApplication
@EnableEDTAspects
@PropertySource(value = "/server.properties", ignoreResourceNotFound = true)
public class DesktopClient {

    public static void main(String[] args) throws IOException {
        SwingApplicationBootstrap.beforeSpring("/splash.png");
        new SpringApplicationBuilder(DesktopClient.class).headless(false).run(args);
    }

    @Bean
    SwingEntryPoint swingEntryPoint(@Autowired MainFrame mainFrame, @Autowired VersionChecker versionChecker) {
        return () -> {
            mainFrame.buildAndShow();
            versionChecker.checkVersion();
        };
    }
}
