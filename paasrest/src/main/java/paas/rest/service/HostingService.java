package paas.rest.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import paas.procman.JavaProcess;
import paas.procman.JavaProcessManager;
import paas.rest.persistence.entities.HostedAppDescriptor;
import paas.rest.persistence.repos.HostedAppDescriptorRepository;
import paas.rest.service.provisioning.Provisioner;
import paas.rest.service.provisioning.Provisions;
import paas.shared.dto.HostedAppRequestedProvisions;

import javax.annotation.security.RolesAllowed;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static paas.rest.persistence.entities.RequestedProvisions.from;

@Component
public class HostingService {

    @Autowired private Provisioner provisioner;
    @Autowired private FileSystemStorageService fileSystemStorageService;
    @Autowired private JavaProcessManager processManager;
    @Autowired private HostedAppDescriptorRepository hostedAppDescriptorRepository;

    @RolesAllowed("USER")
    public long newDeployment(MultipartFile file, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions)
            throws IOException, InterruptedException {
        File uploaded = fileSystemStorageService.saveUpload(file, false);
        HostedAppDescriptor hostedAppDescriptor = new HostedAppDescriptor(
                uploaded.getName(),
                file.getOriginalFilename(),
                commandLineArgs,
                from(requestedProvisions));
        hostedAppDescriptorRepository.save(hostedAppDescriptor);
        createAndStart(hostedAppDescriptor);
        return hostedAppDescriptor.getId();
    }

    @RolesAllowed("USER")
    public long redeploy(long appId, MultipartFile newJarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) throws IOException, InterruptedException {
        //sequence is important:
        //before processManager.stopAndRemoveIfExists it has to be checked if current user
        //is owner of the app. This is done with SpringACL annotation over the find() method
        HostedAppDescriptor hostedAppDescriptor = hostedAppDescriptorRepository.findOne(appId);
        processManager.stopAndRemoveIfExists(appId);

        if(newJarFile != null) {
            String oldJarFile = hostedAppDescriptor.getLocalJarName();
            fileSystemStorageService.deleteUpload(oldJarFile);
            File uploaded = fileSystemStorageService.saveUpload(newJarFile, false);
            hostedAppDescriptor.setLocalJarName(uploaded.getName());
            hostedAppDescriptor.setOriginalJarName(newJarFile.getOriginalFilename());
        }

        hostedAppDescriptor.setCommandLineArgs(commandLineArgs);
        hostedAppDescriptor.setRequestedProvisions(from(requestedProvisions));
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

    @RolesAllowed("USER")
    public void undeploy(long appId) throws InterruptedException, IOException {
        //sequence is important:
        //before processManager.stopAndRemoveIfExists it has to be checked if current user
        //is owner of the app. This is done with SpringACL annotation over the find() method
        HostedAppDescriptor hostedAppDescriptor = hostedAppDescriptorRepository.findOne(appId);
        processManager.stopAndRemoveIfExists(appId);
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
}
