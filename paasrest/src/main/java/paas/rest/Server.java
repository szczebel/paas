package paas.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import paas.host.Shell;
import paas.procman.DatedMessage;
import paas.procman.HostedApp;
import paas.procman.HostedAppManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static paas.rest.FileSystemStorageService.DESKTOP_CLIENT_JAR_NAME;

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

    @Bean
    HostedAppManager hostedAppManager(@Autowired FileSystemStorageService fileSystemStorageService) {
        return new HostedAppManager(fileSystemStorageService.logs);
    }

    @Bean
    FileSystemStorageService fileSystemStorageService(
            @Value("${storage.root}") String storageRoot,
            @Value("${storage.root.is.relative.to.user.home}") boolean relative) {
        if(relative) storageRoot = System.getProperty("user.home") + storageRoot;
        System.out.println("Storage path:" + storageRoot);
        return new FileSystemStorageService(storageRoot);
    }

    @Bean
    Shell shell() {
        return new Shell(System.getProperty("os.name").startsWith("Windows") ? "cmd" : "bash");
    }

    @RestController
    protected static class Mappings {

        @Autowired FileSystemStorageService fileSystemStorageService;
        @Autowired HostedAppManager hostedAppManager;
        @Autowired Shell shell;

        @GetMapping(value = "/PaasDesktopClient.jar")
        public ResponseEntity<FileSystemResource> getDesktopClient() {
            File desktopClientJar = fileSystemStorageService.getDesktopClientJar();
            if(desktopClientJar.exists())
                return ResponseEntity.ok()
                        .contentLength(desktopClientJar.length())
                        .contentType(MediaType.parseMediaType("application/java-archive"))
                        .body(new FileSystemResource(desktopClientJar));
            else throw new IllegalStateException("Call admin and tell him to upload desktop client");
        }

        @GetMapping(value = "/desktopClientLastModified")
        public long getDesktopClientLastModified() {
            File desktopClientJar = fileSystemStorageService.getDesktopClientJar();
            if(desktopClientJar.exists())
                return desktopClientJar.lastModified();
            else throw new IllegalStateException("Call admin and tell him to upload desktop client");
        }

        @PostMapping("/uploadDesktopClient")
        public String uploadDesktopClient(@RequestParam("jarFile") MultipartFile file) throws IOException, InterruptedException {
            String jarFileName = file.getOriginalFilename();
            if(!DESKTOP_CLIENT_JAR_NAME.equals(jarFileName)) throw new IllegalArgumentException("Expected " + DESKTOP_CLIENT_JAR_NAME);
            fileSystemStorageService.saveDesktopClientJar(file);
            return "OK";
        }

        @PostMapping("/executeShellCommand")
        public String executeHostCommand(@RequestParam String command) throws IOException, InterruptedException {
            shell.execute(command);
            return "Executed " + command;
        }

        @GetMapping("/getShellOutput")
        public List<DatedMessage> getShellOutput(@RequestParam(required = false) long timestamp) {
            return shell.getOutputNewerThan(timestamp);
        }

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
        public List<DatedMessage> tailSysout(@RequestParam int appId, @RequestParam(required = false) long timestamp) throws IOException {
            return hostedAppManager.getApp(appId).tailSysout(timestamp);
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
