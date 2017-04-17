package paas.desktop.remoting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import paas.desktop.dto.DatedMessage;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.gui.infra.security.RequiresLogin;
import paas.shared.Links;
import paas.shared.dto.HostedAppInfo;
import paas.shared.dto.HostedAppRequestedProvisions;
import restcall.RestCall;
import swingutils.spring.edt.MustNotBeInEDT;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static paas.shared.Links.*;
import static restcall.RestCall.*;
import static restcall.UploadableFile.forUpload;

@Component
public class PaasRestClient {

    @Autowired private LoginData loginData;

    private String getServerUrl() {
        return loginData.getServerUrl();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public String uploadDesktopClientJar(File jarFile) throws IOException {
        return restPost(getServerUrl() + Links.ADMIN_UPLOAD_DESKTOP_CLIENT, String.class)
                .param("jarFile", forUpload(jarFile))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public List<HostedAppInfo> getHostedApplications() {
        return asList(restGetList(getServerUrl() + APPLICATIONS, HostedAppInfo[].class)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute());
    }

    @MustNotBeInEDT
    @RequiresLogin
    public String deploy(File jarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) throws IOException {
        return restPost(getServerUrl() + DEPLOY, String.class)
                .param("jarFile", forUpload(jarFile))
                .param("commandLineArgs", commandLineArgs)
                .param("wantsDB", requestedProvisions.isWantsDB())
                .param("wantsFileStorage", requestedProvisions.isWantsFileStorage())
                .param("wantsLogstash", requestedProvisions.isWantsLogstash())
                .param("wantsLogging", requestedProvisions.isWantsLogging())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public String redeploy(long appId, File newJarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) throws IOException {
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
            post.param("jarFile", forUpload(newJarFile));

        return post.execute();
    }


    @MustNotBeInEDT
    @RequiresLogin
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

    @MustNotBeInEDT
    @RequiresLogin
    public String restart(long appId) {
        return restPost(getServerUrl() + RESTART, String.class)
                .param("appId", String.valueOf(appId))//ClassCast was thrown without this explicit conversion
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public String stop(long appId) {
        return restPost(getServerUrl() + STOP, String.class)
                .param("appId", String.valueOf(appId))//ClassCast was thrown without this explicit conversion
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public String undeploy(long appId) {
        return restPost(getServerUrl() + UNDEPLOY, String.class)
                .param("appId", String.valueOf(appId))//ClassCast was thrown without this explicit conversion
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public String executeShellCommand(String cmd) {
        return restPost(getServerUrl() + ADMIN_EXECUTE_SHELL_COMMAND, String.class)
                .param("command", cmd)
                .httpBasic(loginData.getUsername(), loginData.getPassword())
                .execute();
    }

    @MustNotBeInEDT
    @RequiresLogin
    public List<DatedMessage> getShellOutputNewerThan(long timestamp) {
        return asList(
                restGetList(getServerUrl() + ADMIN_GET_SHELL_OUTPUT, DatedMessage[].class)
                        .param("timestamp", timestamp)
                        .httpBasic(loginData.getUsername(), loginData.getPassword())
                        .execute());
    }

    @MustNotBeInEDT
    public long getDesktopClientBuildTime() {
        return restGet(loginData.getServerUrl() + Links.DESKTOP_CLIENT_BUILD_TIMESTAMP, Long.class).execute();
    }
}
