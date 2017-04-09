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
import paas.rest.dto.HostedAppInfo;
import paas.rest.service.Deployer;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class HostingEndpoint {

    @Autowired
    private JavaProcessManager processManager;
    @Autowired
    private Deployer deployer;

    @GetMapping("/applications")
    List<HostedAppInfo> applications() throws IOException {
        return processManager.getApps()
                .stream()
                .map(ha -> new HostedAppInfo(ha.getAppId(), ha.getJarFile().getName(), ha.getCommandLineArgs(), ha.isRunning(), ha.getStart()))
                .collect(toList());
    }

    @PostMapping("/deploy")
    public String deploy(@RequestParam("jarFile") MultipartFile file, @RequestParam String commandLineArgs) throws IOException, InterruptedException {
        return "Deployed. App ID:" + deployer.deploy(file, commandLineArgs);
    }

    @PostMapping("/redeploy")
    public String redeploy(@RequestParam("jarFile") MultipartFile file, @RequestParam String commandLineArgs) throws IOException, InterruptedException {
        return "Redeployed. App ID:" + deployer.redeploy(file, commandLineArgs);
    }

    @GetMapping(value = "/undeploy")
    public String undeploy(@RequestParam long appId) throws IOException, InterruptedException {
        deployer.undeploy(appId);
        return "Undeployed app with ID: " + appId;
    }

    @GetMapping(value = "/restart")
    public String restart(@RequestParam long appId) throws IOException, InterruptedException {
        JavaProcess app = processManager.getApp(appId);
        app.stop();
        app.start();
        return "Restarted app with ID: " + app.getAppId();
    }

    @GetMapping(value = "/tailSysout")
    public List<DatedMessage> tailSysout(@RequestParam long appId, @RequestParam(required = false) long timestamp) throws IOException {
        return processManager.getApp(appId).tailSysout(timestamp);
    }
}
