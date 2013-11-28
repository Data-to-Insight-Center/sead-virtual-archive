/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.ui.api.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.context.WebApplicationContext;

/**
 * Configures a mock {@link WebApplicationContext}.  Each test class (or parent class) using
 * {@link MockWebApplicationContextLoader} must be annotated with this.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MockWebApplication {
    /**
     * The location of the webapp directory relative to your project.
     * For maven users, this is generally src/main/webapp (default).
     */
    String webapp() default "src/main/webapp";

    /**
     * The servlet name as defined in the web.xml.
     */
    String name();
}

