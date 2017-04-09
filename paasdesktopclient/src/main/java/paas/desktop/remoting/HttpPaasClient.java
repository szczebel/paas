package paas.desktop.remoting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import paas.desktop.dto.DatedMessage;
import paas.desktop.dto.HostedAppInfo;
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
        return send("/deploy", jarFile, commandLineArgs);
    }

    @MustBeInBackground
    public String redeploy(File jarFile, String commandLineArgs) throws IOException, InterruptedException {
        return send("/redeploy", jarFile, commandLineArgs);
    }

    private String send(String link, File jarFile, String commandLineArgs) throws IOException {
        return restPost(serverUrl + link, String.class)
                .param("jarFile", new UploadableFile(jarFile.getName(), Files.readAllBytes(jarFile.toPath())))
                .param("commandLineArgs", commandLineArgs)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .execute();
    }

    @MustBeInBackground
    public List<DatedMessage> tailNewerThan(long appID, long timestamp) {
        return asList(
                restGetList(
                        serverUrl + "/tailSysout", DatedMessage[].class)
                        .param("appId", appID)
                        .param("timestamp", timestamp)
                        .execute()
        );
    }


    @MustBeInBackground
    public String restart(long appId) {
        return restGet(serverUrl + "/restart", String.class)
                .param("appId", appId)
                .execute();
    }

    @MustBeInBackground
    public String undeploy(long appId) {
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
    public List<DatedMessage> getShellOutputNewerThan(long timestamp) {
        return asList(
                restGetList(serverUrl + "/getShellOutput", DatedMessage[].class)
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
