//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.yahoo.rdl.maven.generate.ResourceGeneratorFactory;
import com.yahoo.rdl.maven.generate.ResourceGeneratorImpl;

import javax.inject.Singleton;

public class CommonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
    }

    @Provides
    @Singleton
    public ResourceGeneratorFactory provideResourceGeneratorFactory(Injector i) {
        return outputFolder -> {
            ResourceGeneratorImpl result = new ResourceGeneratorImpl(outputFolder);
            i.injectMembers(result);
            return result;
        };
    }

}
