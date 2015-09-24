//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven.schema;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@BindingAnnotation
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OSName {
}
