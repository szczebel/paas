package paas.desktop;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;
import swingutils.spring.application.SwingApplication;
import swingutils.spring.application.SwingApplicationBootstrap;
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
}
