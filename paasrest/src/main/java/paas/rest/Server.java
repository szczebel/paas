package paas.rest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import paas.host.Shell;
import paas.procman.JavaProcessManager;
import paas.rest.service.FileSystemStorageService;
import paas.rest.service.logging.AppsOutputForwarded;

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
    JavaProcessManager procman(@Autowired AppsOutputForwarded appsOutputForwarded) {
        return new JavaProcessManager(appsOutputForwarded::forward);
    }

    @Bean
    Shell shell(@Autowired FileSystemStorageService fileSystemStorageService) {
        return new Shell(System.getProperty("os.name").startsWith("Windows") ? "cmd" : "bash", fileSystemStorageService.getStorageRoot());
    }

    @ControllerAdvice
    public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(value = {Exception.class})
        protected ResponseEntity<Object> onException(Exception ex, WebRequest request) {
            LoggerFactory.getLogger(getClass()).warn("Webrequest " + request.getDescription(true) + " failed with exception", ex);
            return handleExceptionInternal(ex, ex.getClass().getSimpleName() + " : " + ex.getMessage(),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

}
