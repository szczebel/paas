package paas.rest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import paas.host.Shell;
import paas.procman.JavaProcessManager;
import paas.rest.service.FileSystemStorageService;

//todo maven plugin for automated deployment
//todo restrict access to apps per owner/user
//todo admins see all apps

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

    @Bean
    Shell shell(@Autowired FileSystemStorageService fileSystemStorageService) {
        return new Shell(System.getProperty("os.name").startsWith("Windows") ? "cmd" : "bash", fileSystemStorageService.getStorageRoot());
    }

    @ControllerAdvice
    static class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(value = {Exception.class})
        protected ResponseEntity<Object> onException(Exception ex, WebRequest request) {
            LoggerFactory.getLogger(getClass()).warn("Webrequest " + request.getDescription(true) + " failed with exception", ex);
            return handleExceptionInternal(ex, ex.getClass().getSimpleName() + " : " + ex.getMessage(),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

    @Configuration
    static class SecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("user").password("user").roles("USER")
                    .and()
                    .withUser("admin").password("lupa6").roles("ADMIN");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .httpBasic()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/", "/unrestricted/*").permitAll()
                    .antMatchers("/admin/*").hasRole("ADMIN")
                    .anyRequest().authenticated()
            ;
        }
    }

}
