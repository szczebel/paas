package paas.rest.service.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.rest.persistence.entities.HostedAppDescriptor;
import paas.rest.persistence.entities.RequestedProvisions;
import paas.rest.service.FileSystemStorageService;
import paas.rest.service.logging.AppsOutputForwarder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

@Component
public class Provisioner {

    private static final BiConsumer<Long, String> NOOP = (id, s) -> {};

    @Autowired
    AppsOutputForwarder appsOutputForwarder;
    @Autowired
    FileSystemStorageService fileSystemStorageService;


    public Provisions createProvisionsFor(HostedAppDescriptor hostedAppDescriptor) throws IOException {
        File appWorkDir = fileSystemStorageService.createWorkDirFor(hostedAppDescriptor.getLocalJarName());
        RequestedProvisions requestedProvisions = hostedAppDescriptor.getRequestedProvisions();

        Set<String> additionalCommandLine = new HashSet<>();
        if(requestedProvisions.isWantsFileStorage()) storageDirectoryArg(appWorkDir, additionalCommandLine);
        if(requestedProvisions.isWantsDB()) dbArgs(appWorkDir, additionalCommandLine);
        if(requestedProvisions.isWantsLogstash()) logstashArg(additionalCommandLine);

        return new Provisions(
                appWorkDir,
                additionalCommandLine,
                getAppLogger(requestedProvisions.isWantsLogging())
        );
    }

    private BiConsumer<Long, String> getAppLogger(boolean wantsLogging) {
        return wantsLogging ? appsOutputForwarder::forward : NOOP;
    }

    private void storageDirectoryArg(File appWorkDir, Collection<String> commandLine) {
        commandLine.add("--storage.directory=\"" + appWorkDir.getAbsolutePath() + "\"");
    }

    private void dbArgs(File appWorkDir, Collection<String> commandLine) {
        //todo:
        //String datasourceUrl = "";
        //commandLine.add("--datasource.url=\"" + datasourceUrl + "\"");
    }

    private void logstashArg(Collection<String> commandLine) {
        //todo:
        //String logstashUrl = "";
        //commandLine.add("--logstash.url=\"" + logstashUrl + "\"");
    }
}
