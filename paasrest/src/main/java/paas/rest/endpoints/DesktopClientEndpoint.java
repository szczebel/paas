package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import paas.rest.service.FileSystemStorageService;
import paas.shared.Links;

import java.io.File;
import java.io.IOException;

import static paas.rest.service.FileSystemStorageService.DESKTOP_CLIENT_JAR_NAME;

@RestController
public class DesktopClientEndpoint {

    @Autowired
    private FileSystemStorageService fileSystemStorageService;


    @GetMapping(Links.PAAS_DESKTOP_CLIENT_JAR)
    public ResponseEntity<FileSystemResource> getDesktopClient() {
        File desktopClientJar = fileSystemStorageService.getDesktopClientJar();
        if(desktopClientJar.exists())
            return ResponseEntity.ok()
                    .contentLength(desktopClientJar.length())
                    .contentType(MediaType.parseMediaType("application/java-archive"))
                    .body(new FileSystemResource(desktopClientJar));
        else throw new IllegalStateException("Call admin and tell him to upload desktop client");
    }

    @GetMapping(Links.DESKTOP_CLIENT_LAST_MODIFIED)
    public long getDesktopClientLastModified() {
        File desktopClientJar = fileSystemStorageService.getDesktopClientJar();
        if(desktopClientJar.exists())
            return desktopClientJar.lastModified();
        else throw new IllegalStateException("Call admin and tell him to upload desktop client");
    }

    @PostMapping(Links.ADMIN_UPLOAD_DESKTOP_CLIENT)
    public String uploadDesktopClient(@RequestParam("jarFile") MultipartFile file) throws IOException, InterruptedException {
        String jarFileName = file.getOriginalFilename();
        if(!DESKTOP_CLIENT_JAR_NAME.equals(jarFileName)) throw new IllegalArgumentException("Expected " + DESKTOP_CLIENT_JAR_NAME);
        fileSystemStorageService.saveDesktopClientJar(file);
        return "OK";
    }
}
