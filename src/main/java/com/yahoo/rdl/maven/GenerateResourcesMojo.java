//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.yahoo.rdl.maven.generate.ResourceGenerator;
import com.yahoo.rdl.maven.generate.ResourceGeneratorFactory;
import com.yahoo.rdl.maven.schema.OSName;
import com.yahoo.rdl.maven.schema.RdlSources;
import com.yahoo.rdl.maven.schema.ScratchSpace;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

@Mojo( name = "generate-jax-rs-resources", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class GenerateResourcesMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project.build.resources[0].directory}/rdl", property = "rdlDirectory", required = true )
    private File rdlDirectory;

    @Parameter( defaultValue = "${project.build.directory}/generated-sources/rdl", property = "generatedResourcesDirectory", required = true )
    private File generatedResourcesDirectory;

    @Parameter( defaultValue = "${os.name}", property = "osName", required = false )
    private String osName;

    @Parameter(property = "rdlExecutableFile")
    private File rdlExecutableFile;

    @Parameter( defaultValue = "${project.build.sourceEncoding}", property = "sourceEncoding", required = true)
    private String sourceEncoding;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Component
    private MavenProject mavenProject;

    private File rdlScratchSpace;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) return;
        rdlScratchSpace = new File(mavenProject.getBuild().getDirectory(), "rdl");
        if (!rdlScratchSpace.mkdirs() && !rdlScratchSpace.exists()) {
            throw new MojoExecutionException("Unable to create folder " + rdlScratchSpace);
        }
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new CommonModule());
                bind(Log.class).toInstance(getLog());
                bind(Path.class).annotatedWith(ScratchSpace.class).toInstance(rdlScratchSpace.toPath());
                bind(Path.class).annotatedWith(RdlSources.class).toInstance(rdlDirectory.toPath());
                bind(String.class).annotatedWith(OSName.class).toProvider(new OSNameProvider(osName));
                bind(Charset.class).toInstance(Charset.forName(sourceEncoding != null ? sourceEncoding : "UTF-8"));
            }

            @Provides
            @Singleton
            public RdlExecutableFileProvider provideRdlExecutableFileProvider(
                    @ScratchSpace Path scratchSpace,
                    @OSName String osName) {
                return new RdlExecutableFileProviderImpl(
                        scratchSpace,
                        osName,
                        rdlExecutableFile == null ? null : rdlExecutableFile.toPath());
            }


        });
        RdlFileFinder rdlFileFinder = injector.getInstance(RdlFileFinder.class);
        List<Path> rdlFiles = rdlFileFinder.findRdlFiles();
        if (!generatedResourcesDirectory.mkdirs() && !generatedResourcesDirectory.exists()) {
            throw new MojoExecutionException("Unable to create resource directory " + generatedResourcesDirectory.getAbsolutePath());
        }
        ResourceGenerator resourceGenerator = injector.getInstance(ResourceGeneratorFactory.class).newResourceGenerator(generatedResourcesDirectory.toPath());
        for (Path rdlFile : rdlFiles) {
            try {
                resourceGenerator.generateResources(rdlFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to generate resources for file: " + rdlFile.toAbsolutePath(), e);
            }
        }

        mavenProject.addCompileSourceRoot(generatedResourcesDirectory.getAbsolutePath());
    }

}
