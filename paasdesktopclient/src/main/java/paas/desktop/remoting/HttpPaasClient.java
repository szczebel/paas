package paas.desktop.remoting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import paas.desktop.HostedAppInfo;
import paas.desktop.gui.infra.MustBeInBackground;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.util.Arrays.asList;
import static paas.desktop.remoting.RestCall.restGetList;
import static paas.desktop.remoting.RestCall.restPost;

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
    public List<String> tailErrout(int appID, int limit) {
        return tail(appID, limit, "Errout");
    }

    private List<String> tail(int appID, int limit, String out) {
        return asList(
                restGetList(
                        substitute(serverUrl + "/tail{out}?appId={appId}&limit={limit}", out, appID, limit),
                        String[].class)
                        .execute()
        );
    }

    public void serverChanged(String newServerUrl) {
        this.serverUrl = newServerUrl;
    }

    private String substitute(String template, Object... actuals){
        String retval = template;
        for (Object actual : actuals) {
            int start = retval.indexOf('{');
            if(start == -1) throw new IllegalArgumentException("No place to insert " + actual + " missing {");
            int end = retval.indexOf('}');
            if(end == -1) throw new IllegalArgumentException("No place to insert " + actual + " missing }");
            if(end<start) throw new IllegalArgumentException("No place to insert " + actual + ", }...{");
            retval = retval.substring(0, start) + actual + retval.substring(end + 1);
        }

        int start = retval.indexOf('{');
        int end = retval.indexOf("}");
        if(start!=-1 && end>start) throw new IllegalArgumentException("Not enough actuals");

        return retval;
    }
}
