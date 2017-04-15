package paas.desktop;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import paas.desktop.gui.MainFrame;
import swingutils.Splash;
import swingutils.SysoutInterceptor;
import swingutils.components.ComponentFactory;

import javax.swing.*;
import java.io.IOException;

import static swingutils.Splash.showSplash;

//todo: home page with public stats of the server (number of hosted apps,

@SpringBootApplication
public class DesktopClient {

    public static final SysoutInterceptor sysoutInterceptor = new SysoutInterceptor();
    public static Splash splash;
    public static void main(String[] args) throws IOException {
        splash = showSplash(new ImageIcon(DesktopClient.class.getResource("/splash.png")), "Loading...");
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

    @Component
    static class SplashUpdater implements BeanPostProcessor, ApplicationContextAware {
        int counter = 0;
        @Override
        public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
            String toShow = o.getClass().getSimpleName();
            if(toShow.contains("$$EnhancerBySpringCGLIB$$"))  toShow = toShow.substring(0, toShow.indexOf("$$EnhancerBySpringCGLIB$$"));
            final String finalToShow = toShow;
            SwingUtilities.invokeLater(() -> splash.setProgressText("Initializing " + finalToShow, ++counter));
            return o;
        }

        @Override
        public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
            return o;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            splash.setProgressRange(applicationContext.getBeanDefinitionCount());
        }
    }
}
