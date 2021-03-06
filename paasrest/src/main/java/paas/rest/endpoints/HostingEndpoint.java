package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import paas.procman.DatedMessage;
import paas.rest.service.HostingService;
import paas.shared.Links;
import paas.shared.dto.HostedAppInfo;
import paas.shared.dto.HostedAppRequestedProvisions;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
public class HostingEndpoint {

    @Autowired private HostingService hostingService;

    @GetMapping(Links.APPLICATIONS)
    List<HostedAppInfo> getApplications() throws IOException {
        return hostingService.getApplications();
    }

    @PostMapping(Links.DEPLOY)
    public String deploy(
            @RequestParam("jarFile") MultipartFile file,
            @RequestParam String commandLineArgs,
            @RequestParam boolean wantsDB,
            @RequestParam boolean wantsFileStorage,
            @RequestParam boolean wantsLogstash,
            @RequestParam boolean wantsLogging,
            @RequestParam boolean wantsMonitoring,
            Principal principal
            ) throws IOException, InterruptedException {
        return "Deployed. App ID:" + hostingService.newDeployment(principal.getName(), file, commandLineArgs,
                new HostedAppRequestedProvisions(wantsDB, wantsFileStorage, wantsLogstash, wantsLogging, wantsMonitoring));
    }



    @PostMapping("/redeploy-jar")
    public String redeployJarOnly(
            @RequestParam long appId,
            @RequestParam(value = "jarFile", required = false) MultipartFile file) throws IOException, InterruptedException {
        hostingService.redeploy(appId, file, null, null);
        return "Redeployed jar";
    }

    @PostMapping(Links.REDEPLOY)
    public String redeploy(
            @RequestParam long appId,
            @RequestParam(value = "jarFile", required = false) MultipartFile file,
            @RequestParam String commandLineArgs,
            @RequestParam boolean wantsDB,
            @RequestParam boolean wantsFileStorage,
            @RequestParam boolean wantsLogstash,
            @RequestParam boolean wantsLogging,
            @RequestParam boolean wantsMonitoring
            ) throws IOException, InterruptedException {
        hostingService.redeploy(appId, file, commandLineArgs,
                new HostedAppRequestedProvisions(wantsDB, wantsFileStorage, wantsLogstash, wantsLogging, wantsMonitoring));
        return "Redeployed";
    }

    @PostMapping(Links.UNDEPLOY)
    public String undeploy(@RequestParam long appId) throws IOException, InterruptedException {
        hostingService.undeploy(appId);
        return "Undeployed app with ID: " + appId;
    }

    @PostMapping(Links.RESTART)
    public String restart(@RequestParam long appId) throws IOException, InterruptedException {
        hostingService.restart(appId);
        return "Restarted app with ID: " + appId;
    }

    @PostMapping(Links.STOP)
    public String stop(@RequestParam long appId) throws IOException, InterruptedException {
        hostingService.stop(appId);
        return "Restarted app with ID: " + appId;
    }

    @GetMapping(Links.TAIL_SYSOUT)
    public List<DatedMessage> tailSysout(@RequestParam long appId, @RequestParam(required = false) long timestamp) throws IOException {
        return hostingService.tailSysout(appId, timestamp);
    }
}
