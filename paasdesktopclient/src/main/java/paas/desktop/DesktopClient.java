package paas.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import paas.desktop.gui.MainFrame;
import paas.desktop.gui.infra.MyJar;
import paas.desktop.gui.infra.autoupdate.Autoupdate;
import swingutils.spring.application.SwingApplication;
import swingutils.spring.application.SwingApplicationBootstrap;
import swingutils.spring.application.SwingEntryPoint;
import swingutils.spring.edt.EnableEDTAspects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static paas.desktop.gui.infra.autoupdate.ReplaceAndRelaunch.replaceAndRelaunch;

@SpringBootApplication
@SwingApplication
@EnableEDTAspects
@PropertySource(value = "/server.properties", ignoreResourceNotFound = true)
public class DesktopClient {

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && Autoupdate.AUTOUPDATE_REPLACE.equals(args[0])) {
            replaceAndRelaunch();
        } else {
            SwingApplicationBootstrap.beforeSpring("/splash.png");
            new SpringApplicationBuilder(DesktopClient.class).headless(false).run(args);
        }
    }

    @Bean
    CommandLineRunner autoupdateCleanup() {
        return args -> {
            if (args.length == 1 && Autoupdate.AUTOUPDATE_CLEANUP.equals(args[0])) {
                Logger logger = LoggerFactory.getLogger(Autoupdate.class);
                try {
                    String toDelete = MyJar.getAbsolutePath() + Autoupdate.NEW_VERSION_FILENAME_SUFFIX;
                    logger.info("Cleanup - will delete " + toDelete);
                    Files.deleteIfExists(Paths.get(toDelete));
                } catch (Exception e) {
                    logger.warn("Cleanup failed", e);
                }
            }
        };
    }

    @Bean
    SwingEntryPoint swingEntryPoint(@Autowired MainFrame mainFrame, @Autowired Autoupdate autoupdate) {
        return () -> {
            mainFrame.buildAndShow();
            autoupdate.downloadIfAvailable();
        };
    }
}
