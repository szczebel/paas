package paas.desktop.remoting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import paas.desktop.dto.DatedMessage;
import paas.desktop.gui.infra.MustBeInBackground;
import paas.desktop.gui.infra.security.LoginData;
import paas.shared.dto.HostedAppInfo;
import paas.shared.dto.HostedAppRequestedProvisions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.util.Arrays.asList;
import static paas.desktop.remoting.RestCall.restGetList;
import static paas.desktop.remoting.RestCall.restPost;
import static paas.shared.Links.*;

@Component
public class PaasRestClient {

    @Autowired private LoginData loginData;

    private String getServerUrl() {
        return loginData.getServerUrl();
    }

    @MustBeInBackground
    public List<HostedAppInfo> getHostedApplications() {
        return asList(restGetList(getServerUrl() + APPLICATIONS, HostedAppInfo[].class)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute());
    }

    @MustBeInBackground
    public String deploy(File jarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) throws IOException, InterruptedException {
        return restPost(getServerUrl() + DEPLOY, String.class)
                .param("jarFile", new UploadableFile(jarFile.getName(), Files.readAllBytes(jarFile.toPath())))
                .param("commandLineArgs", commandLineArgs)
                .param("wantsDB", requestedProvisions.isWantsDB())
                .param("wantsFileStorage", requestedProvisions.isWantsFileStorage())
                .param("wantsLogstash", requestedProvisions.isWantsLogstash())
                .param("wantsLogging", requestedProvisions.isWantsLogging())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustBeInBackground
    public String redeploy(long appId, File newJarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) throws IOException, InterruptedException {
        RestCall<String> post = restPost(getServerUrl() + REDEPLOY, String.class)
                .param("appId", appId)
                .param("commandLineArgs", commandLineArgs)
                .param("wantsDB", requestedProvisions.isWantsDB())
                .param("wantsFileStorage", requestedProvisions.isWantsFileStorage())
                .param("wantsLogstash", requestedProvisions.isWantsLogstash())
                .param("wantsLogging", requestedProvisions.isWantsLogging())
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .contentType(MediaType.MULTIPART_FORM_DATA);
        if(newJarFile != null)
            post.param("jarFile", new UploadableFile(newJarFile.getName(), Files.readAllBytes(newJarFile.toPath())));

        return post.execute();
    }


    @MustBeInBackground
    public List<DatedMessage> tailNewerThan(long appID, long timestamp) {
        return asList(
                restGetList(
                        getServerUrl() + TAIL_SYSOUT, DatedMessage[].class)
                        .param("appId", appID)
                        .param("timestamp", timestamp)
                        .httpBasic(loginData.getUsername(), loginData.getPassword())
                        .execute()
        );
    }


    @MustBeInBackground
    public String restart(long appId) {
        return restPost(getServerUrl() + RESTART, String.class)
                .param("appId", appId)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustBeInBackground
    public String undeploy(long appId) {
        return restPost(getServerUrl() + UNDEPLOY, String.class)
                .param("appId", appId)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    public String executeShellCommand(String cmd) {
        return restPost(getServerUrl() + ADMIN_EXECUTE_SHELL_COMMAND, String.class)
                .param("command", cmd)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustBeInBackground
    public List<DatedMessage> getShellOutputNewerThan(long timestamp) {
        return asList(
                restGetList(getServerUrl() + ADMIN_GET_SHELL_OUTPUT, DatedMessage[].class)
                        .param("timestamp", timestamp)
                        .httpBasic(loginData.getUsername(), loginData.getPassword())
                        .execute());
    }
}
