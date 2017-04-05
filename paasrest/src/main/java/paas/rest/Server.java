package paas.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import paas.procman.HostedApp;
import paas.procman.HostedAppManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class Server extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Server.class);
    }

    @Bean
    HostedAppManager hostedAppManager(@Autowired FileSystemStorageService fileSystemStorageService) {
        return new HostedAppManager(fileSystemStorageService.logs);
    }

    @Bean
    FileSystemStorageService fileSystemStorageService() {
        //todo externalize storageRoot
        return new FileSystemStorageService(System.getProperty("user.home") + "/hackathon-upload-dir/paas");
    }

    @RestController
    protected static class Mappings {

        @Autowired FileSystemStorageService fileSystemStorageService;
        @Autowired HostedAppManager hostedAppManager;

        @GetMapping("/files")
        List<String> files() throws IOException {
            return fileSystemStorageService.getFiles().stream().map(File::getName).collect(toList());
        }

        @GetMapping("/applications")
        List<HostedAppInfo> applications() throws IOException {
            return hostedAppManager.getApps()
                    .stream()
                    .map(ha -> new HostedAppInfo(ha.getId(), ha.getJarFile().getName(), ha.getCommandLineArgs(), ha.isRunning(), Date.from(ha.getStart().toInstant())))
                    .collect(toList());
        }

        @PostMapping("/deploy")
        public String deploy(@RequestParam("jarFile") MultipartFile file, @RequestParam String commandLineArgs) throws IOException, InterruptedException {
            String jarFileName = file.getOriginalFilename();
            Optional<HostedApp> existingApp = hostedAppManager.findByJarName(jarFileName);
            if(!existingApp.isPresent()) {
                File uploaded = fileSystemStorageService.overwrite(file);//after PaaS restart, file may exist even if no HostedApp exists
                HostedApp newApp = hostedAppManager.add(uploaded, commandLineArgs);
                newApp.start();
                return "Deployed. App ID: " + newApp.getId();
            } else {
                HostedApp app = existingApp.get();
                app.stop();
                fileSystemStorageService.overwrite(file);
                app.start();
                return "Redeployed. App ID: " + app.getId();
            }
        }

        @GetMapping(value = "/undeploy")
        public String undeploy(@RequestParam int appId) throws IOException, InterruptedException {
            HostedApp app = hostedAppManager.getApp(appId);
            app.stop();
            hostedAppManager.remove(appId);
            Files.delete(app.getJarFile().toPath());
            return "Undeployed app with ID: " + app.getId();
        }

        @GetMapping(value = "/restart")
        public String restart(@RequestParam int appId) throws IOException, InterruptedException {
            HostedApp app = hostedAppManager.getApp(appId);
            app.stop();
            app.start();
            return "Restarted app with ID: " + app.getId();
        }

        @GetMapping(value = "/tailSysout")
        public List<String> tailSysout(@RequestParam int appId, @RequestParam int limit) throws IOException {
            return hostedAppManager.getApp(appId).tailSysout(limit);
        }

        @GetMapping(value = "/tailSyserr")
        public List<String> tailSyserr(@RequestParam int appId, @RequestParam int limit) throws IOException {
            return hostedAppManager.getApp(appId).tailSyserr(limit);
        }
    }

    @ControllerAdvice
    public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(value = {Exception.class})
        protected ResponseEntity<Object> onException(Exception ex, WebRequest request) {
            ex.printStackTrace();
            return handleExceptionInternal(ex, "Error occured! " + ex.getClass().getSimpleName() + " : " + ex.getMessage(),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

}
