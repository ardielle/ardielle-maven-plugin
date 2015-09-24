//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven.generate;

import com.yahoo.rdl.maven.RdlTest;
import org.junit.Test;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class ResourceGeneratorImplTest extends RdlTest {

    @Inject private ResourceGeneratorFactory resourceGeneratorFactory;

    @Test
    public void testGenerateResources() throws Exception {
        ResourceGenerator resourceGenerator = resourceGeneratorFactory.newResourceGenerator(scratchSpace);
        resourceGenerator.generateResources(Paths.get("src", "test", "resources", "namespaced-resources.rdl"));
        assertTrue(Files.exists(scratchSpace.resolve(Paths.get("com", "ApiAssetsRespObject.java"))));
        assertTrue(Files.exists(scratchSpace.resolve(Paths.get("com", "ApiResourcesHandler.java"))));
    }

}
