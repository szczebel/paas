package paas.rest.service.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.rest.persistence.entities.Procfile;
import paas.rest.service.FileSystemStorageService;
import paas.rest.service.logging.AppsOutputForwarder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.function.BiConsumer;

@Component
public class Provisioner {

    @Autowired AppsOutputForwarder appsOutputForwarder;
    @Autowired FileSystemStorageService fileSystemStorageService;


    public Provisions createProvisionsFor(Procfile procfile) throws IOException {
        File appWorkDir = fileSystemStorageService.createWorkDirFor(procfile.getJarFileName());
        return new Provisions(
                appWorkDir,
                Collections.singleton(storageDirectoryArg(appWorkDir)),
                getAppLogger(procfile)
        );
    }

    private BiConsumer<Long, String> getAppLogger(Procfile procfile) {
        //todo: if user chose to provision ELK, or if his app logs to ELK directly)
//        if(procfile.wantsELK())
        return appsOutputForwarder::forward;
//        else return (id,s) -> {};//noop

    }

    private String storageDirectoryArg(File appWorkDir) {
        return "--storage.directory=\"" + appWorkDir.getAbsolutePath() + "\"";
    }
}
