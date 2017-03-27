//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
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
import org.codehaus.plexus.util.cli.CommandLineUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class GenerateMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project.build.resources[0].directory}/rdl", property = "rdlDirectory", required = true )
    private File rdlDirectory;

    @Parameter(property = "rawCommands", required = false)
    private List<RawCommand> rawCommands;

    @Parameter(property = "commands", required = false)
    private List<String> commands;

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
        List<List<String>> cmdArgss;
        if (commands.isEmpty() && rawCommands.isEmpty()) {
            List<Path> rdlFiles = injector.getInstance(RdlFileFinder.class).findRdlFiles();
            if (rdlFiles.size() == 0) {
                throw new MojoExecutionException("No rdl files found.");
            } else if (rdlFiles.size() > 1) {
                throw new MojoExecutionException("Multiple rdl files found: " + rdlFiles);
            }
            String rdlFile = rdlFiles.get(0).toAbsolutePath().toString();
            String outputFolder = Paths.get(mavenProject.getBuild().getDirectory(), "generated-sources", "rdl").toAbsolutePath().toString();
            cmdArgss = new ArrayList<>();
            for (String generator : Arrays.asList("java-model", "java-server")) {
                cmdArgss.add(Arrays.asList("generate", "-o", outputFolder, generator, rdlFile));
            }
        } else {
            cmdArgss = new ArrayList<>();
            for (String command : commands) {
                try {
                    cmdArgss.add(Arrays.asList(CommandLineUtils.translateCommandline(command)));
                } catch (Exception e) {
                    throw new MojoExecutionException("Unable to parse command: " + command, e);
                }
            }
            for (RawCommand rawCommand : rawCommands) {
                if (rawCommand.arguments == null) {
                    throw new MojoExecutionException("Raw command has no arguments");
                }
                cmdArgss.add(rawCommand.arguments);
            }
        }
        String rdlBinary = injector.getInstance(RdlExecutableFileProvider.class).getRdlExecutableFile().toAbsolutePath().toString();
        ProcessRunner processRunner = injector.getInstance(ProcessRunner.class);
        for (List<String> cmdArgs : cmdArgss) {
            int outputFlag = cmdArgs.indexOf("-o");
            if (outputFlag >= 0 && outputFlag + 1 < cmdArgs.size()) {
                mavenProject.addCompileSourceRoot(cmdArgs.get(outputFlag + 1));
            }
            List<String> fullCommand = new ArrayList<>(cmdArgs.size() + 1);
            fullCommand.add(rdlBinary);
            fullCommand.addAll(cmdArgs);
            try {
                processRunner.run(fullCommand);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to run rdl command.", e);
            }
        }
    }

}
