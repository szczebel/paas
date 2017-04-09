package paas.rest.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import paas.procman.JavaProcess;
import paas.procman.JavaProcessManager;
import paas.rest.persistence.entities.Procfile;
import paas.rest.persistence.repos.ProcfileRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@Component
public class Deployer {

    @Autowired
    private FileSystemStorageService fileSystemStorageService;
    @Autowired
    private JavaProcessManager processManager;
    @Autowired
    private ProcfileRepository procfileRepository;
    @Autowired Provisioning provisioning;


    public long deploy(MultipartFile file, String commandLineArgs) throws IOException, InterruptedException {
        File uploaded = fileSystemStorageService.saveUpload(file, false);
        Long id = procfileRepository.save(new Procfile(uploaded.getName(), commandLineArgs)).getId();
        createAndStart(id, uploaded, commandLineArgs);
        return id;
    }

    public long redeploy(MultipartFile file, String commandLineArgs) throws IOException, InterruptedException {
        String jarFileName = file.getOriginalFilename();
        Optional<Procfile> optionalProcfile = procfileRepository.findByJarFileName(jarFileName);
        if (!optionalProcfile.isPresent())
            throw new IllegalArgumentException(jarFileName + " is not hosted, cannot redeploy. Use Deploy instead");

        //else
        Procfile procfile = optionalProcfile.get();

        processManager.stopAndRemoveIfExists(jarFileName);
        File uploaded = fileSystemStorageService.saveUpload(file, true);
        createAndStart(procfile.getId(), uploaded, commandLineArgs);


        procfile.setCommandLineArgs(commandLineArgs);
        procfileRepository.save(procfile);

        return procfile.getId();
    }

    private void createAndStart(Long id, File jarFile, String commandLineArgs) throws IOException, InterruptedException {
        File appWorkDir = fileSystemStorageService.createWorkDirFor(jarFile);
        List<String> commandLine = new ArrayList<>();
        commandLine.addAll(asList(commandLineArgs.split(" ")));
        Collection<String> additionalCommandLine = provisioning.provision(appWorkDir);
        commandLine.addAll(additionalCommandLine);
        JavaProcess newApp = processManager.create(id, jarFile, appWorkDir, commandLine);
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
            createAndStart(
                    procfile.getId(),
                    fileSystemStorageService.resolveUpload(procfile.getJarFileName()).toFile(),
                    procfile.getCommandLineArgs()
            );
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Failed to redeploy from " + procfile, e);
        }
    }
}
