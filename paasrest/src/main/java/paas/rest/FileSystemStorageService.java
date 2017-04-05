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

    private final File uploads;
    final File logs;

    FileSystemStorageService(String storageRoot) {
        uploads = Paths.get(storageRoot + "/uploads").toFile();
        logs = Paths.get(storageRoot + "/logs").toFile();
    }

    @SuppressWarnings("unused")
    @PostConstruct
    void init() throws IOException {
        if(!logs.exists()) Files.createDirectory(logs.toPath());
    }

    private File save(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Empty file: " + file.getOriginalFilename());
        }
        if (!uploads.exists()) Files.createDirectory(uploads.toPath());
        Path target = uploads.toPath().resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), target);
        return target.toFile();
    }


    List<File> getFiles() throws IOException {
        File[] files = uploads.listFiles();
        return files != null ? Arrays.asList(files) : emptyList();
    }

    File overwrite(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Empty file: " + file.getOriginalFilename());
        }
        Path target = uploads.toPath().resolve(file.getOriginalFilename());
        Files.deleteIfExists(target);
        return save(file);
    }
}
