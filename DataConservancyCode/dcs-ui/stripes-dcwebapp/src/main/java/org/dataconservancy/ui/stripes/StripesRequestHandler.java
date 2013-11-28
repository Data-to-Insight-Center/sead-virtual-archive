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

import net.sourceforge.stripes.controller.DispatcherServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletConfigAware;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles HTTP requests that are meant to be dispatched by the Stripes Framework.  This class manages the initialization
 * of the {@link DispatcherServlet Stripes Dispatcher Servlet}.  <strong>N.B.:</strong>Requests handled by this class
 * <em>must</em> be processed by the {@code StripesFilter}.  It is important that the {@code web.xml} be set up properly
 * such that all requests that target this handler have been touched by the {@code StripesFilter}.
 */
public class StripesRequestHandler implements ServletConfigAware, HttpRequestHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean isInitialized = false;
    private final Object initLock = new Object();
    private ServletConfig servletConfig;
    private DispatcherServlet stripesDispatcher;

    /**
     * Delegates the processing of the request and response to the
     * {@link #setStripesDispatcher(net.sourceforge.stripes.controller.DispatcherServlet) Stripes Dispatcher Servlet}.
     * <p/>
     * This method keeps track of whether or not the Stripes Dispatcher has been initialized, and initializes it
     * accordingly.
     * 
     * @param request the HTTP Servlet Request
     * @param response the HTTP Servlet Response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.trace("Servicing request: {} {}", request, response);

        // TODO: review initialization of the Stripes Dispatcher (e.g. do it in a Spring Bean Post Processor?)
        synchronized (initLock) {
            if (!isInitialized) {
                try {
                    log.error("Initializing Stripes Dispatcher {} with {}", stripesDispatcher, servletConfig);
                    stripesDispatcher.init(servletConfig);
                    isInitialized = true;
                } catch (ServletException e) {
                    throw new RuntimeException("Error initializing the Stripes Dispatcher: " + e.getMessage(), e);
                }
            }
        }

        stripesDispatcher.service(request, response);
        log.trace("Servicing request complete: {} {}", request, response);
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        log.trace("Received Servlet Config: {}", servletConfig);
        this.servletConfig = servletConfig;
    }

    public DispatcherServlet getStripesDispatcher() {
        return stripesDispatcher;
    }

    public void setStripesDispatcher(DispatcherServlet stripesDispatcher) {
        log.trace("Received Stripes Dispatcher: {}", stripesDispatcher);
        this.stripesDispatcher = stripesDispatcher;
    }

}
