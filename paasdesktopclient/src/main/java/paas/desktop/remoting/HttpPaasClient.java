package paas.desktop.remoting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import paas.desktop.HostedAppInfo;
import paas.desktop.ShellOutput;
import paas.desktop.gui.infra.MustBeInBackground;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.util.Arrays.asList;
import static paas.desktop.remoting.RestCall.*;

@Component
public class HttpPaasClient {

    @Value("${tiniestpaas.server.url}")
    private String serverUrl;

    @MustBeInBackground
    public List<HostedAppInfo> getHostedApplications() {
        return asList(restGetList(serverUrl + "/applications", HostedAppInfo[].class).execute());
    }

    @MustBeInBackground
    public String deploy(File jarFile, String commandLineArgs) throws IOException, InterruptedException {
        Thread.sleep(3000);
        return restPost(serverUrl + "/deploy", String.class)
                .param("jarFile", new UploadableFile(jarFile.getName(), Files.readAllBytes(jarFile.toPath())))
                .param("commandLineArgs", commandLineArgs)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .execute();
    }

    @MustBeInBackground
    public List<String> tailSysout(int appID, int limit) {
        return tail(appID, limit, "Sysout");
    }

    @MustBeInBackground
    public List<String> tailSyserr(int appID, int limit) {
        return tail(appID, limit, "Syserr");
    }

    private List<String> tail(int appID, int limit, String out) {
        return asList(
                restGetList(
                        substitute(serverUrl + "/tail{out}", out), String[].class)
                        .param("appId", appID)
                        .param("limit", limit)
                        .execute()
        );
    }

    @MustBeInBackground
    public String restart(int appId) {
        return restGet(serverUrl + "/restart", String.class)
                .param("appId", appId)
                .execute();
    }

    @MustBeInBackground
    public String undeploy(int appId) {
        return restGet(serverUrl + "/undeploy", String.class)
                .param("appId", appId)
                .execute();
    }

    public String executeShellCommand(String cmd) {
        return restPost(serverUrl + "/executeShellCommand", String.class)
                .param("command", cmd)
                .execute();
    }

    @MustBeInBackground
    public List<ShellOutput> getShellOutputNewerThan(long timestamp) {
        return asList(
                restGetList(serverUrl + "/getShellOutput", ShellOutput[].class)
                        .param("timestamp", timestamp)
                        .execute());
    }

    public void serverChanged(String newServerUrl) {
        this.serverUrl = newServerUrl;
    }

    private String substitute(String template, Object... actuals) {
        String retval = template;
        for (Object actual : actuals) {
            int start = retval.indexOf('{');
            if (start == -1) throw new IllegalArgumentException("No place to insert " + actual + " missing {");
            int end = retval.indexOf('}');
            if (end == -1) throw new IllegalArgumentException("No place to insert " + actual + " missing }");
            if (end < start) throw new IllegalArgumentException("No place to insert " + actual + ", }...{");
            retval = retval.substring(0, start) + actual + retval.substring(end + 1);
        }

        int start = retval.indexOf('{');
        int end = retval.indexOf("}");
        if (start != -1 && end > start) throw new IllegalArgumentException("Not enough actuals");

        return retval;
    }
}
