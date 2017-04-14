package com.epam.sandbox.paas.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;

import static restcall.RestCall.restPost;
import static restcall.UploadableFile.forUpload;

@Mojo(name = "redeploy")
public class RedeployMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File target;

    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;

    @Parameter(defaultValue = "${project.packaging}", readonly = true)
    private String packaging;

    @Parameter(required = true) private String paasServerUrl;
    @Parameter(required = true) private String username;
    @Parameter(required = true) private String password;
    @Parameter(required = true) private Long appId;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File artifact = new File(target, finalName + "." + packaging);
        getLog().info("Artifact : " + artifact.getAbsolutePath());
        if(!artifact.exists())
            throw new MojoFailureException("Artifact does not exist");
        if(!artifact.isFile())
            throw new MojoFailureException("Artifact is not a file");
        getLog().info(username + " is deploying artifact as appId=" + appId + " to " + paasServerUrl);
        try {
            getLog().info(redeploy(paasServerUrl, username, password, appId, artifact));
        } catch (Exception e) {
            throw new MojoExecutionException("Deployment failed", e);
        }
    }

    private String redeploy(
            String paasServerUrl, String username, String password, long appId,
            File newJarFile) throws IOException {
        return restPost(paasServerUrl + "/redeploy-jar", String.class)
                .param("appId", String.valueOf(appId))
                .httpBasic(username, password)
                .param("jarFile", forUpload(newJarFile))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .execute();
    }
}
