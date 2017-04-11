package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import paas.procman.DatedMessage;
import paas.procman.JavaProcess;
import paas.procman.JavaProcessManager;
import paas.rest.persistence.entities.HostedAppDescriptor;
import paas.rest.persistence.repos.HostedAppDescriptorRepository;
import paas.rest.service.Deployer;
import paas.shared.Links;
import paas.shared.dto.HostedAppInfo;
import paas.shared.dto.HostedAppRequestedProvisions;
import paas.shared.dto.HostedAppStatus;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class HostingEndpoint {

    @Autowired
    private JavaProcessManager processManager;
    @Autowired
    private Deployer deployer;
    @Autowired
    private HostedAppDescriptorRepository hostedAppDescriptorRepository;

    @GetMapping(Links.APPLICATIONS)
    List<HostedAppInfo> applications() throws IOException {
        return hostedAppDescriptorRepository.findAll()
                .stream().map(this::info).collect(toList());
    }

    private HostedAppInfo info(HostedAppDescriptor p) {
        return new HostedAppInfo(
                p.toDto(),
                processManager.getStatus(p.getId())
                        .orElse(new HostedAppStatus(false, null)));

    }

    @PostMapping(Links.DEPLOY)
    public String deploy(
            @RequestParam("jarFile") MultipartFile file,
            @RequestParam String commandLineArgs,
            @RequestParam boolean wantsDB,
            @RequestParam boolean wantsFileStorage,
            @RequestParam boolean wantsLogstash,
            @RequestParam boolean wantsLogging
            ) throws IOException, InterruptedException {
        return "Deployed. App ID:" + deployer.newDeployment(file, commandLineArgs,
                new HostedAppRequestedProvisions(wantsDB, wantsFileStorage, wantsLogstash, wantsLogging));
    }

    @PostMapping(Links.REDEPLOY)
    public String redeploy(
            @RequestParam long appId,
            @RequestParam(value = "jarFile", required = false) MultipartFile file,
            @RequestParam String commandLineArgs,
            @RequestParam boolean wantsDB,
            @RequestParam boolean wantsFileStorage,
            @RequestParam boolean wantsLogstash,
            @RequestParam boolean wantsLogging
    ) throws IOException, InterruptedException {
        return "Redeployed. App ID:" + deployer.redeploy(appId, file, commandLineArgs,
                new HostedAppRequestedProvisions(wantsDB, wantsFileStorage, wantsLogstash, wantsLogging));
    }

    @PostMapping(Links.UNDEPLOY)
    public String undeploy(@RequestParam long appId) throws IOException, InterruptedException {
        deployer.undeploy(appId);
        return "Undeployed app with ID: " + appId;
    }

    @PostMapping(Links.RESTART)
    public String restart(@RequestParam long appId) throws IOException, InterruptedException {
        JavaProcess app = processManager.getApp(appId);
        app.stop();
        app.start();
        return "Restarted app with ID: " + app.getAppId();
    }

    @GetMapping(Links.TAIL_SYSOUT)
    public List<DatedMessage> tailSysout(@RequestParam long appId, @RequestParam(required = false) long timestamp) throws IOException {
        return processManager.getApp(appId).tailSysout(timestamp);
    }
}
