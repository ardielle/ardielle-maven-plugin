//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import javax.inject.Provider;

public class OSNameProvider implements Provider<String> {

    private String configuredOSName;

    public OSNameProvider(String configuredOSName) {
        this.configuredOSName = configuredOSName;
    }

    @Override
    public String get() {
        String name = configuredOSName != null ? configuredOSName : System.getProperty("os.name");
        String result = name.startsWith("Mac") ? "darwin" : name;
        return result;
    }
}
