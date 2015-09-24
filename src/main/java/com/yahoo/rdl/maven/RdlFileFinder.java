//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.google.inject.ImplementedBy;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.nio.file.Path;
import java.util.List;

@ImplementedBy(RdlFileFinderImpl.class)
public interface RdlFileFinder {
    List<Path> findRdlFiles() throws MojoExecutionException, MojoFailureException;
}
