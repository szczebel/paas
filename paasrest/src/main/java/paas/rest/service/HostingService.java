package paas.rest.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import paas.procman.DatedMessage;
import paas.procman.JavaProcess;
import paas.procman.JavaProcessManager;
import paas.rest.persistence.entities.HostedAppDescriptor;
import paas.rest.persistence.repos.HostedAppDescriptorRepository;
import paas.rest.service.provisioning.Provisioner;
import paas.rest.service.provisioning.Provisions;
import paas.shared.dto.HostedAppInfo;
import paas.shared.dto.HostedAppRequestedProvisions;
import paas.shared.dto.HostedAppStatus;

import javax.annotation.security.RolesAllowed;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static paas.rest.persistence.entities.RequestedProvisions.from;

@Component
public class HostingService {

    @Autowired
    private Provisioner provisioner;
    @Autowired
    private FileSystemStorageService fileSystemStorageService;
    @Autowired
    private JavaProcessManager processManager;
    @Autowired
    private HostedAppDescriptorRepository hostedAppDescriptorRepository;

    @RolesAllowed("USER")
    public long newDeployment(String owner, MultipartFile file, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions)
            throws IOException, InterruptedException {
        File uploaded = fileSystemStorageService.saveUpload(file, false);
        HostedAppDescriptor hostedAppDescriptor = new HostedAppDescriptor(
                owner, uploaded.getName(),
                file.getOriginalFilename(),
                commandLineArgs,
                from(requestedProvisions));
        hostedAppDescriptorRepository.save(hostedAppDescriptor);
        createAndStart(hostedAppDescriptor);
        return hostedAppDescriptor.getId();
    }

    @PreAuthorize("hasRole('USER') AND @ownershipChecker.isCurrentUserOwnerOfAppId(authentication, #appId)")
    public long redeploy(long appId, MultipartFile newJarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) throws IOException, InterruptedException {
        HostedAppDescriptor hostedAppDescriptor = hostedAppDescriptorRepository.findOne(appId);
        return redeploy(hostedAppDescriptor, newJarFile, commandLineArgs, requestedProvisions);
    }

    //The below did not work... security was allowing userA redeploy app owned by userB, and I don't know why :((((
    //And it was public, so that any CGLib magic was able to intercept it
//    @PreAuthorize("hasRole('USER') AND (#hostedAppDescriptor.owner == authentication.name)")
    private long redeploy(HostedAppDescriptor hostedAppDescriptor,
                          MultipartFile newJarFile,
                          String commandLineArgs,
                          HostedAppRequestedProvisions requestedProvisions) throws InterruptedException, IOException {
        processManager.stopAndRemoveIfExists(hostedAppDescriptor.getId());

        if (newJarFile != null) {
            String oldJarFile = hostedAppDescriptor.getLocalJarName();
            fileSystemStorageService.deleteUpload(oldJarFile);
            File uploaded = fileSystemStorageService.saveUpload(newJarFile, false);
            hostedAppDescriptor.setLocalJarName(uploaded.getName());
            hostedAppDescriptor.setOriginalJarName(newJarFile.getOriginalFilename());
        }

        if (commandLineArgs != null) hostedAppDescriptor.setCommandLineArgs(commandLineArgs);
        if (requestedProvisions != null) hostedAppDescriptor.setRequestedProvisions(from(requestedProvisions));
        hostedAppDescriptorRepository.save(hostedAppDescriptor);

        createAndStart(hostedAppDescriptor);
        return hostedAppDescriptor.getId();
    }

    private void createAndStart(HostedAppDescriptor hostedAppDescriptor) throws IOException, InterruptedException {
        File jarFile = fileSystemStorageService.resolveUpload(hostedAppDescriptor.getLocalJarName()).toFile();
        List<String> commandLine = new ArrayList<>();
        commandLine.addAll(asList(hostedAppDescriptor.getCommandLineArgs().split(" ")));
        Provisions provisions = provisioner.createProvisionsFor(hostedAppDescriptor);
        commandLine.addAll(provisions.getAdditionalCommandLine());

        JavaProcess newApp = processManager.create(
                hostedAppDescriptor.getId(),
                jarFile,
                provisions.getAppWorkDir(),
                commandLine,
                provisions.getOutputLogger());
        newApp.start();
    }

    @RolesAllowed({"USER", "ADMIN"})
    public void undeploy(long appId) throws InterruptedException, IOException {
        HostedAppDescriptor hostedAppDescriptor = hostedAppDescriptorRepository.findOne(appId);
        undeploy(hostedAppDescriptor);
    }

    @PreAuthorize("hasRole('ADMIN') OR (#hostedAppDescriptor.owner == authentication.name)")
    protected void undeploy(HostedAppDescriptor hostedAppDescriptor) throws InterruptedException, IOException {
        processManager.stopAndRemoveIfExists(hostedAppDescriptor.getId());
        fileSystemStorageService.deleteUpload(hostedAppDescriptor.getLocalJarName());
        hostedAppDescriptorRepository.delete(hostedAppDescriptor);
    }

    void redeployFromProcfiles() {
        hostedAppDescriptorRepository.findAll().forEach(this::redeployFromProcfile);
    }

    private void redeployFromProcfile(HostedAppDescriptor hostedAppDescriptor) {
        try {
            LoggerFactory.getLogger(getClass()).info("Redeploying from : " + hostedAppDescriptor);
            createAndStart(hostedAppDescriptor);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Failed to redeploy from " + hostedAppDescriptor, e);
        }
    }

    @PreAuthorize("hasRole('ADMIN') OR (#hostedAppDescriptor.owner == authentication.name)")
    public void stop(long appId) throws InterruptedException {
        processManager.findById(appId).ifPresent(JavaProcess::stop);
    }

    @PreAuthorize("hasRole('USER') AND @ownershipChecker.isCurrentUserOwnerOfAppId(authentication, #appId)")
    public void restart(long appId) throws InterruptedException, IOException {
        Optional<JavaProcess> appProcess = processManager.findById(appId);
        if (appProcess.isPresent()) {
            appProcess.get().stop();
            appProcess.get().start();
        } else {
            createAndStart(hostedAppDescriptorRepository.findOne(appId));
        }
    }

    @PreAuthorize("hasRole('USER') AND @ownershipChecker.isCurrentUserOwnerOfAppId(authentication, #appId)")
    public List<DatedMessage> tailSysout(long appId, long timestamp) {
        Optional<JavaProcess> p = processManager.findById(appId);
        if (p.isPresent())
            return p.get().tailSysout(timestamp);
        else
            return Collections.emptyList();
    }

    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @PostFilter("hasRole('ADMIN') OR (filterObject.hostedAppDesc.owner == authentication.name)")
    public List<HostedAppInfo> getApplications() {
        return hostedAppDescriptorRepository.findAll()
                .stream().map(this::info).collect(toList());
    }

    private HostedAppInfo info(HostedAppDescriptor p) {
        return new HostedAppInfo(
                p.toDto(),
                processManager.getStatus(p.getId())
                        .orElse(new HostedAppStatus(false, null)));

    }

}
