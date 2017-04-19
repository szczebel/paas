package paas.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static java.util.stream.StreamSupport.stream;

@SpringBootApplication
public class Application {

    static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RestController
    static class Endpoint {

        @GetMapping("/")
        String helloWorld() {
            return "Hello world";
        }
    }

    @Component
    static class Inspector implements CommandLineRunner {

        @Autowired
        private AbstractEnvironment springEnv;
        @Autowired
        private ApplicationContext applicationContext;

        @Override
        public void run(String... strings) throws Exception {

            LOGGER.info("Inspecting application arguments ==============================================");
            stream(strings).forEach(LOGGER::info);

            LOGGER.info("Inspecting some application properties =========================================");
            stream(springEnv.getPropertySources().spliterator(), false)
                    .filter(ps -> ps instanceof EnumerablePropertySource)
                    .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                    .flatMap(Arrays::<String>stream)
                    .filter(s -> s.contains("spring") || s.contains("log"))
                    .distinct()
                    .map(name -> name + "=" + springEnv.getProperty(name))
                    .forEach(LOGGER::info);

            LOGGER.info("Inspecting some beans ==========================================================");
            stream(applicationContext.getBeanDefinitionNames())
                    .filter(s -> !s.contains("."))
                    .forEach(LOGGER::info);
        }
    }
}
