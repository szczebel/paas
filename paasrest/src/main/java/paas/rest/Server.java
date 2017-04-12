package paas.rest;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import paas.procman.JavaProcessManager;

//todo maven plugin for automated deployment

@SpringBootApplication
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

    @ControllerAdvice
    static class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(value = {Exception.class})
        protected ResponseEntity<Object> onException(Exception ex, WebRequest request) {
            LoggerFactory.getLogger(getClass()).debug("Webrequest " + request.getDescription(true) + " failed with exception", ex);
            return handleExceptionInternal(ex, ex.getClass().getSimpleName() + " : " + ex.getMessage(),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

    @Configuration
    @EnableGlobalMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
    static class SecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("guest").password("guest").roles("USER")
                    .and()
                    .withUser("user2").password("user2").roles("USER")
                    .and()
                    .withUser("admin").password("lupa6").roles("ADMIN")
                    .and()
                    .withUser("adam").password("lupa6").roles("ADMIN", "USER");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().httpBasic();
            //that's it, access is controlled via method security
        }
    }

}
