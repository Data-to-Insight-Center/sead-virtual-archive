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
package org.dataconservancy.ui.services.util;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import javax.servlet.GenericServlet;

public class FormatRegistryBootstrapServlet extends GenericServlet {

    @Override
    public void init(ServletConfig config) {
        System.out.println("ServletContextListener started");   
        final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        final FormatRegistryBootstrap bootstrap = (FormatRegistryBootstrap)springContext.getBean("metadataFormatBootstrap");
        bootstrap.bootstrapFormats();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        
    }
}