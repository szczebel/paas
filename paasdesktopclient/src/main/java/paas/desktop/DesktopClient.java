package paas.desktop;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import swingutils.spring.application.SwingApplication;
import swingutils.spring.application.SwingApplicationBootstrap;
import swingutils.spring.edt.EnableEDTAspects;

import java.io.IOException;

//todo: home page with public stats of the server (number of hosted apps,

@SpringBootApplication
@SwingApplication
@EnableEDTAspects
public class DesktopClient {

    public static void main(String[] args) throws IOException {
        SwingApplicationBootstrap.beforeSpring("/splash.png");
        new SpringApplicationBuilder(DesktopClient.class).headless(false).run(args);
    }
}
