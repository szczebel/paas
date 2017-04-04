package paas.desktop.remoting;

import org.springframework.core.io.ByteArrayResource;

public class UploadableFile extends ByteArrayResource {
    private final String fileName;

    UploadableFile(String fileName, byte[] fileBytes) {
        super(fileBytes);
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}
