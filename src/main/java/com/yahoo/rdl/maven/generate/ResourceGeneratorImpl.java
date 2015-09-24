//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven.generate;

import com.yahoo.rdl.maven.ProcessRunner;
import com.yahoo.rdl.maven.RdlExecutableFileProvider;
import org.apache.maven.plugin.MojoExecutionException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ResourceGeneratorImpl implements ResourceGenerator {

    @Inject protected RdlExecutableFileProvider rdlExecutableFileProvider;
    @Inject protected ProcessRunner processRunner;

    protected Path targetFolder;

    public ResourceGeneratorImpl(Path targetFolder) {
        this.targetFolder = targetFolder;
    }

    @Override
    public void generateResources(Path rdlFile) throws MojoExecutionException, IOException {
        executeRDLBinary(rdlFile, "java-model");
        executeRDLBinary(rdlFile, "java-server");
    }

    protected void executeRDLBinary(Path rdlFile, String generator) throws MojoExecutionException, IOException {
        List<String> command = Arrays.asList(
                rdlExecutableFileProvider.getRdlExecutableFile().toAbsolutePath().toString(),
                "generate",
                "-o",
                targetFolder.toAbsolutePath().toString(),
                generator,
                rdlFile.toAbsolutePath().toString());

        processRunner.run(command);
    }

}
