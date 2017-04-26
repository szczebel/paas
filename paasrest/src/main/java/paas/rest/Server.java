package paas.rest;

import de.codecentric.boot.admin.config.EnableAdminServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.jmx.JmxMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import paas.procman.JavaProcessManager;
import paas.rest.service.security.Role;
import paas.rest.service.security.UserService;

//fixme: Tomcat session created for each rest call!!!
//todo: monitoring security
//todo: management.security of monitored app
//to do later: monitoring notifications -> email to owner -> requires identity management app

@SpringBootApplication
@EnableAdminServer
public class Server extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Server.class);
    }

    @Bean(name = "processManager")
    JavaProcessManager procman() {
        return new JavaProcessManager();
    }

    @Bean
    @ExportMetricWriter
    MetricWriter metricWriter(MBeanExporter exporter) {
        return new JmxMetricWriter(exporter);
    }

    @ControllerAdvice
    static class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(value = {Exception.class})
        protected ResponseEntity<Object> onException(Exception ex, WebRequest request) {
            LOGGER.debug("Webrequest " + request.getDescription(true) + " failed with exception", ex);
            return handleExceptionInternal(ex, ex.getClass().getSimpleName() + " : " + ex.getMessage(),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

    @Configuration
    @EnableGlobalMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
    static class SecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private UserService userService;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userService).passwordEncoder(passwordEncoder());

            auth.inMemoryAuthentication()
                    .withUser("guest").password("guest").roles(Role.USER)
                    .and()
                    .withUser("admin").password("lupa6").roles(Role.ADMIN)
            ;
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().httpBasic();
            //that's it, access is controlled via method security
        }
    }

}
