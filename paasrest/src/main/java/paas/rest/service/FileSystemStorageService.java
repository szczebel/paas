package paas.rest.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FileSystemStorageService {

    public static final String DESKTOP_CLIENT_JAR_NAME = "PaaSDesktopClient.jar";
    private final File storageRoot;
    private final File uploads;
    private final File appsWorkingDirs;

    public FileSystemStorageService(@Value("${storage.root}") String storageRoot) {
        this.storageRoot = Paths.get(storageRoot).toFile();
        LoggerFactory.getLogger(getClass()).info("Storage root is : " + this.storageRoot.getAbsolutePath());
        uploads = Paths.get(storageRoot + "/uploads").toFile();
        appsWorkingDirs = Paths.get(storageRoot + "/appsWorkingDirs").toFile();
    }

    @SuppressWarnings("unused")
    @PostConstruct
    void init() throws IOException {
        if(!storageRoot.exists()) Files.createDirectory(storageRoot.toPath());
        if(!appsWorkingDirs.exists()) Files.createDirectory(appsWorkingDirs.toPath());
        if (!uploads.exists()) Files.createDirectory(uploads.toPath());
    }

    public File getStorageRoot() {
        return storageRoot;
    }

    File saveUpload(MultipartFile file, boolean overwrite) throws IOException {
        Path target = resolveUpload(file.getOriginalFilename()+ nowSuffix());
        return save(target, file, overwrite);
    }

    private String nowSuffix() {
        return "["+ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"))+"]";
    }

    void deleteUpload(String jarFileName) throws IOException {
        Files.deleteIfExists(resolveUpload(jarFileName));
    }

    Path resolveUpload(String jarFileName) {
        return uploads.toPath().resolve(jarFileName);
    }

    private File save(Path target, MultipartFile file, boolean overwrite) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Empty file: " + file.getOriginalFilename());
        }
        if(overwrite) Files.deleteIfExists(target);
        Files.copy(file.getInputStream(), target);
        return target.toFile();
    }

    public File getDesktopClientJar() {
        return storageRoot.toPath().resolve(DESKTOP_CLIENT_JAR_NAME).toFile();
    }

    public void saveDesktopClientJar(MultipartFile file) throws IOException {
        Path target = storageRoot.toPath().resolve(DESKTOP_CLIENT_JAR_NAME);
        save(target, file, true);
    }

    public File createWorkDirFor(String jarFileName) throws IOException {
        File retval = appsWorkingDirs.toPath().resolve(jarFileName + "-workdir").toFile();
        if(!retval.exists()) Files.createDirectory(retval.toPath());
        return retval;
    }
}
