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
package org.dataconservancy.ui.stripes;

import javax.servlet.ServletContext;

import org.dataconservancy.ui.model.StatusPropertiesContext;
import org.springframework.web.context.ServletContextAware;

/**
 * Provides a properties loader usable with the PropertyPlaceholderConfigurer.
 */
public class StatusPropertiesLoader implements ServletContextAware {
    private ServletContext servletContext;
    private StatusPropertiesContext propertyHolder;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setPropertyHolder(StatusPropertiesContext propertyHolder) {
        this.propertyHolder = propertyHolder;
    }

    public void init() {
        // Non-ITs usually don't have a ServletContext and therefore blow up.
        if (servletContext != null) {
          servletContext.setAttribute("statusPropertiesBean", propertyHolder);
        }
    }
}
