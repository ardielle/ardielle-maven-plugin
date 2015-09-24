//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.google.inject.Inject;
import com.yahoo.rdl.maven.schema.RdlSources;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RdlFileFinderImpl implements RdlFileFinder {

    protected Path rdlDirectory;

    @Inject
    public RdlFileFinderImpl(@RdlSources Path rdlDirectory) {
        this.rdlDirectory = rdlDirectory;
    }

    @Override
    public List<Path> findRdlFiles() throws MojoExecutionException, MojoFailureException {
        List<Path> rdlFiles = new ArrayList<Path>();
        try {
            Files.walkFileTree(rdlDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (FilenameUtils.isExtension(file.toString(), "rdl")) {
                        rdlFiles.add(file);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Error walking " + rdlDirectory.toAbsolutePath(), e);
        }
        if (rdlFiles.isEmpty()) {
            throw new MojoFailureException("No files ending in .rdl were found in the directory " + rdlDirectory.toAbsolutePath() + ". Assign <rdlDirectory> in the configuration of rdl-maven-plugin to a folder containing your rdl files.");
        }
        return rdlFiles;
    }
}
