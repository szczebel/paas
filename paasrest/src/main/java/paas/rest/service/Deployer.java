package paas.rest.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import paas.procman.JavaProcess;
import paas.procman.JavaProcessManager;
import paas.rest.persistence.entities.Procfile;
import paas.rest.persistence.repos.ProcfileRepository;
import paas.rest.service.provisioning.Provisioner;
import paas.rest.service.provisioning.Provisions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@Component
public class Deployer {

    @Autowired
    Provisioner provisioner;
    @Autowired
    private FileSystemStorageService fileSystemStorageService;
    @Autowired
    private JavaProcessManager processManager;
    @Autowired
    private ProcfileRepository procfileRepository;

    public long deploy(MultipartFile file, String commandLineArgs) throws IOException, InterruptedException {
        File uploaded = fileSystemStorageService.saveUpload(file, false);
        Procfile procfile = new Procfile(uploaded.getName(), commandLineArgs);
        procfileRepository.save(procfile);
        createAndStart(procfile);
        return procfile.getId();
    }

    public long redeploy(MultipartFile file, String commandLineArgs) throws IOException, InterruptedException {
        String jarFileName = file.getOriginalFilename();
        Optional<Procfile> optionalProcfile = procfileRepository.findByJarFileName(jarFileName);
        if (!optionalProcfile.isPresent())
            throw new IllegalArgumentException(jarFileName + " is not hosted, cannot redeploy. Use Deploy instead");

        //else
        Procfile procfile = optionalProcfile.get();
        procfile.setCommandLineArgs(commandLineArgs);
        procfileRepository.save(procfile);

        processManager.stopAndRemoveIfExists(jarFileName);
        fileSystemStorageService.saveUpload(file, true);

        createAndStart(procfile);
        return procfile.getId();
    }

    private void createAndStart(Procfile procfile) throws IOException, InterruptedException {
        File jarFile = fileSystemStorageService.resolveUpload(procfile.getJarFileName()).toFile();
        List<String> commandLine = new ArrayList<>();
        commandLine.addAll(asList(procfile.getCommandLineArgs().split(" ")));
        Provisions provisions = provisioner.createProvisionsFor(procfile);
        commandLine.addAll(provisions.getAdditionalCommandLine());

        JavaProcess newApp = processManager.create(
                procfile.getId(),
                jarFile,
                provisions.getAppWorkDir(),
                commandLine,
                provisions.getOutputLogger());
        newApp.start();
    }

    public void undeploy(long appId) throws InterruptedException, IOException {
        processManager.stopAndRemoveIfExists(appId);
        Procfile procfile = procfileRepository.findOne(appId);
        fileSystemStorageService.deleteUpload(procfile.getJarFileName());
        procfileRepository.delete(procfile);
    }

    void redeployFromProcfiles() {
        procfileRepository.findAll().forEach(this::redeployFromProcfile);
    }

    private void redeployFromProcfile(Procfile procfile) {
        try {
            LoggerFactory.getLogger(getClass()).info("Redeploying from : " + procfile);
            createAndStart(procfile);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Failed to redeploy from " + procfile, e);
        }
    }
}
