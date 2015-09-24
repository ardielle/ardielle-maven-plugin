//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven.generate;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.nio.file.Path;

public interface ResourceGenerator {

    void generateResources(Path rdlFile) throws MojoExecutionException, IOException;

}
