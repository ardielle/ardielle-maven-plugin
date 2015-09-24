//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import org.apache.maven.plugin.MojoExecutionException;

import java.nio.file.Path;

public interface RdlExecutableFileProvider {
    Path getRdlExecutableFile() throws MojoExecutionException;
}
