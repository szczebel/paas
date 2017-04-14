package com.epam.sandbox.paas.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "redeploy")
public class RedeployMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File target;

    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;

    @Parameter(defaultValue = "${project.packaging}", readonly = true)
    private String packaging;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Build dir : " + target.getAbsolutePath());
        getLog().info("Final name: " + finalName);
        getLog().info("Packaging : " + packaging);

    }
}
