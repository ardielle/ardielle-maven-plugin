//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.yahoo.rdl.maven.schema.OSName;
import com.yahoo.rdl.maven.schema.ScratchSpace;
import org.apache.maven.plugin.logging.Log;

import javax.inject.Singleton;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CommonModule());
        bind(Log.class).toInstance(mock(Log.class));
        bind(String.class).annotatedWith(OSName.class).toProvider(new OSNameProvider(null));
        bind(Charset.class).toInstance(StandardCharsets.UTF_8);
    }

    @Provides
    @Singleton
    public RdlExecutableFileProvider provideRdlExecutableFileProvider(
            @ScratchSpace Path scratchSpace,
            @OSName String osName) {
        return new RdlExecutableFileProviderImpl(scratchSpace, osName, null);
    }

}
