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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * The theme filter is responsible for setting requests parameters that influence the global look and feel of the
 * DC UI.  The value of the request parameter maps to directory names in the web application which
 * contain files that influence the styling and structure of page elements.
 * <p/>
 * By default, this filter sets the value of the theme to {@code default}.  One can modify this value by updating the
 * {@code ThemeFilter} filter in {@code web.xml}.
 */
public class ThemeFilter implements Filter {

    private final static String THEME_NAME = "themeName";

    private final static String DEFAULT_THEME_NAME = "default";

    private final static String STRIPES_THEME_REQ_ATTR = "stripes_theme";

    private final static String STRIPES_LAYOUT_REQ_ATTR = "stripes_layout";

    private final static String STRIPES_PAGES_REQ_ATTR = "stripes_pages";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String themeName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        themeName = filterConfig.getInitParameter(THEME_NAME);
        if (themeName == null || themeName.trim().length() == 0) {
            themeName = DEFAULT_THEME_NAME;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.trace("Setting theme request attributes {}, {}, {}, to value {} on request {}",
                new Object[] { STRIPES_THEME_REQ_ATTR, STRIPES_LAYOUT_REQ_ATTR, STRIPES_PAGES_REQ_ATTR, themeName, request} );

        request.setAttribute(STRIPES_THEME_REQ_ATTR, themeName);
        request.setAttribute(STRIPES_LAYOUT_REQ_ATTR, themeName);
        request.setAttribute(STRIPES_PAGES_REQ_ATTR, themeName);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
