//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.yahoo.rdl.maven.schema.OSName;
import com.yahoo.rdl.maven.schema.ScratchSpace;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Singleton
public class RdlExecutableFileProviderImpl implements RdlExecutableFileProvider {

    protected Path configuredExecutableFile;
    protected volatile Path actualExecutableFile;

    public RdlExecutableFileProviderImpl(
            @ScratchSpace Path scratchSpace,
            @OSName String osName,
            Path configuredExecutableFile) {
        this.scratchSpace = scratchSpace;
        this.osName = osName;
        this.configuredExecutableFile = configuredExecutableFile;
    }

    protected Path scratchSpace;
    protected String osName;

    @Override
    public Path getRdlExecutableFile() throws MojoExecutionException {
        if (configuredExecutableFile != null) {
            return configuredExecutableFile;
        }
        if (actualExecutableFile != null) {
            return actualExecutableFile;
        }
        File binFolder = new File(scratchSpace.toFile(), "bin");
        if (!binFolder.mkdirs() && !binFolder.exists()) {
            throw new MojoExecutionException("Unable to create folder for rdl executable: " + binFolder);
        }

        Path rdlBinaryPath = scratchSpace.resolve(Paths.get("bin", "rdl"));
        try (OutputStream os = Files.newOutputStream(rdlBinaryPath, StandardOpenOption.CREATE)) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("bin/" + osName + "/rdl")) {
                IOUtils.copy(is, os);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write rdl binary to " + rdlBinaryPath, e);
        }
        if (!rdlBinaryPath.toFile().setExecutable(true) && !rdlBinaryPath.toFile().canExecute()) {
            throw new MojoExecutionException("Unable to chmod +x executable: " + rdlBinaryPath.toAbsolutePath());
        }

        Path rdlGenSwaggerBinaryPath = scratchSpace.resolve(Paths.get("bin", "rdl-gen-swagger"));
        try (OutputStream os = Files.newOutputStream(rdlGenSwaggerBinaryPath, StandardOpenOption.CREATE)) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("bin/" + osName + "/rdl-gen-swagger")) {
                IOUtils.copy(is, os);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write rdl-gen-swagger binary to " + rdlBinaryPath, e);
        }
        if (!rdlGenSwaggerBinaryPath.toFile().setExecutable(true) && !rdlGenSwaggerBinaryPath.toFile().canExecute()) {
            throw new MojoExecutionException("Unable to chmod +x executable: " + rdlBinaryPath.toAbsolutePath());
        }

        actualExecutableFile = rdlBinaryPath;
        return rdlBinaryPath;
    }

}
