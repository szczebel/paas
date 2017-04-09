package paas.rest;

import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

class FileSystemStorageService {

    static final String DESKTOP_CLIENT_JAR_NAME = "PaaSDesktopClient.jar";
    private final File storageRoot;
    private final File uploads;
    private final File appsWorkingDirs;

    FileSystemStorageService(String storageRoot) {
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


    List<File> getFiles() throws IOException {
        File[] files = uploads.listFiles();
        return files != null ? Arrays.asList(files) : emptyList();
    }

    public File getStorageRoot() {
        return storageRoot;
    }

    public File getAppsWorkingDirs() {
        return appsWorkingDirs;
    }

    File overwrite(MultipartFile file) throws IOException {
        Path target = uploads.toPath().resolve(file.getOriginalFilename());
        return overwrite(target, file);
    }

    private File overwrite(Path target, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Empty file: " + file.getOriginalFilename());
        }
        Files.deleteIfExists(target);
        Files.copy(file.getInputStream(), target);
        return target.toFile();
    }

    File getDesktopClientJar() {
        return storageRoot.toPath().resolve(DESKTOP_CLIENT_JAR_NAME).toFile();
    }

    void saveDesktopClientJar(MultipartFile file) throws IOException {
        Path target = storageRoot.toPath().resolve(DESKTOP_CLIENT_JAR_NAME);
        overwrite(target, file);
    }
}
