package paas.rest.service.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${logstash.endpoint}")
    private String logstashEndpoint;

    @Value("${server.contextPath}")
    private String contextPath;
    @Value("${spring.boot.admin.context-path}")
    private String monitoringPath;


    public Provisions createProvisionsFor(HostedAppDescriptor hostedAppDescriptor) throws IOException {
        File appWorkDir = fileSystemStorageService.createWorkDirFor(hostedAppDescriptor.getLocalJarName());
        RequestedProvisions requestedProvisions = hostedAppDescriptor.getRequestedProvisions();

        Set<String> additionalCommandLine = new HashSet<>();
        if (requestedProvisions.isWantsFileStorage()) storageDirectoryArg(appWorkDir, additionalCommandLine);
        if (requestedProvisions.isWantsDB()) dbArgs(appWorkDir, additionalCommandLine);
        if (requestedProvisions.isWantsLogstash()) logstashArg(additionalCommandLine);
        if (requestedProvisions.isWantsMonitoring()) monitoringArg(additionalCommandLine, hostedAppDescriptor.getId());

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
        //working directory is root ("." should work)
        commandLine.add("--paas.storage.directory=.");
//        commandLine.add("--paas.storage.directory=\"" + appWorkDir.getAbsolutePath() + "\"");
    }

    private void dbArgs(File appWorkDir, Collection<String> commandLine) {
        //just a file in working directory ("." should work)
        commandLine.add("--paas.datasource.url=jdbc:h2:file:./h2db;DB_CLOSE_ON_EXIT=FALSE");
        commandLine.add("--paas.datasource.username=paas");
        commandLine.add("--paas.datasource.password=paas");
        commandLine.add("--paas.datasource.driverClassName=org.h2.Driver");
    }

    private void logstashArg(Collection<String> commandLine) {
        commandLine.add("--paas.logstash.url=" + logstashEndpoint);
    }

    private void monitoringArg(Collection<String> commandLine, long appId) {
        // todo: get rid of hardcoded host
        String monitorUrl = "http://localhost:8080" + contextPath + monitoringPath;
        commandLine.add("--spring.boot.admin.url=" + monitorUrl);
        commandLine.add("--spring.application.name=PaaS.HostedApp."+ appId);
        commandLine.add("--management.security.enabled=false");
    }

}
