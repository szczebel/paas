package paas.rest;

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
    private final File root;
    private final File uploads;
    final File logs;

    FileSystemStorageService(String storageRoot) {
        root = Paths.get(storageRoot).toFile();
        uploads = Paths.get(storageRoot + "/uploads").toFile();
        logs = Paths.get(storageRoot + "/logs").toFile();
    }

    @SuppressWarnings("unused")
    @PostConstruct
    void init() throws IOException {
        if(!root.exists()) Files.createDirectory(root.toPath());
        if(!logs.exists()) Files.createDirectory(logs.toPath());
        if (!uploads.exists()) Files.createDirectory(uploads.toPath());
    }


    List<File> getFiles() throws IOException {
        File[] files = uploads.listFiles();
        return files != null ? Arrays.asList(files) : emptyList();
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
        return root.toPath().resolve(DESKTOP_CLIENT_JAR_NAME).toFile();
    }

    void saveDesktopClientJar(MultipartFile file) throws IOException {
        Path target = root.toPath().resolve(DESKTOP_CLIENT_JAR_NAME);
        overwrite(target, file);
    }
}
