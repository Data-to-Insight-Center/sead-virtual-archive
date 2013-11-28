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
package org.dataconservancy.dcs.lineage.http.view;

import java.io.OutputStream;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.http.LineageModelAttribute;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.APPLICATION_ANY;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.APPLICATION_JSON;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.APPLICATION_XML;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.TEXT_PLAIN;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.getAccept;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.getEntities;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.getIfModifiedSince;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.getLineage;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.getModelAndView;

/**
 * Renders a view of a Lineage.  It reasons over the {@link ModelAndView}, using the values of well-known {@link
 * LineageModelAttribute model attributes} to render the response. This resolver relies on the {@code ModelAndView} to
 * be {@link ExposeModelHandleInterceptor exposed} as a Servlet request attribute.  This implementation handles
 * {@code 404}, {@code 405}, and {@code 304} responses directly.
 * <p/>
 * If the {@link LineageModelAttribute#LINEAGE lineage} is empty or null, return a {@code 404}.
 * If the {@link LineageModelAttribute#ENTITIES entities in the lineage} are empty or null, return a {@code 404}.
 * If the {@link LineageModelAttribute#IFMODIFIEDSINCE If-Modified-Since} is greater than the timestamp of the
 * most {@link LineageModelAttribute#LASTMODIFIED recently updated} {@link LineageEntry}, return a {@code 304}.
 * If the {@link LineageModelAttribute#ACCEPT Accept header} contains a supported value, return a supported view.  Otherwise
 * return a {@code 405}.
 * If the {@code Accept} header doesn't exist (or the value is empty), then return the {@link #getDefaultView() default view}.
 */
public class LineageViewResolver implements ViewResolver {

    private static final View NOT_FOUND_VIEW = new NOT_FOUND_VIEW();

    private static final View NOT_MODIFIED_VIEW = new NOT_MODIFIED_VIEW();

    private static final View NOT_ACCEPTABLE_VIEW = new NOT_ACCEPTABLE_VIEW();

    private static final String NOT_FOUND_MSG = "Lineage '%s' not found.\n";

    private final ServletRequestAttributesSource requestAttributesSource;

    private View defaultView;

    private DcsModelBuilder modelBuilder;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public LineageViewResolver(ServletRequestAttributesSource requestAttributesSource) {
        this.requestAttributesSource = requestAttributesSource;
    }

    /**
     * Resolves the appropriate view using the contents of the {@link ModelAndView} (obtained from the
     * {@code HttpRequest}).  <em>Note that the view name and locale provided to this method are ignored in
     * this implementation.</em>
     * <p/>
     * The resolution logic of this {@code ViewResolver} is as follows:
     * <ol>
     *     <li>If the {@code ModelAndView} is not found, a {@code 500} Internal Server Error view is returned.</li>
     *     <li>If the {@code Lineage} and DCS entities of the lineage are not found in the model, or if they are empty, a
     * {@code 404} Not Found view is returned.</li>
     *     <li>If a {@code If-Modified-Since} header is present, and the lineage has not been modified since the supplied
     * date, then a {@code 304} Not Modified view is returned.</li>
     *     <li>If a {@code Accept} header is present, and it contains a supported value, then the appropriate view for the
     * requested content type is returned.</li>
     *     <li>If a {@code Accept} header is present, but it contains an un-supported value, then a {@code 405} Not Acceptable
     * view is returned.</li>
     *     <li>If {@code Accept} header is not present, then the {@link #setDefaultView(org.springframework.web.servlet.View)
     * default view} is returned.</li>
     * </ol>
     *
     * @param viewName {@inheritDoc} (ignored by this implementation)
     * @param locale {@inheritDoc} (ignored by this implementation)
     * @return {@inheritDoc}
     * @throws Exception {@inheritDoc}
     * @see ExposeModelHandleInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.web.servlet.ModelAndView)
     */
    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {

        final ServletRequestAttributes requestAttributes;
        try {
            requestAttributes = requestAttributesSource.getRequestAttributes();
        } catch (ClassCastException e) {
            throw new RuntimeException(this.getClass().getName() + " can only work with ServletRequestAttributes.", e);
        }

        final ModelAndView mv = getModelAndView(requestAttributes);

        final List<DcsEntity> entities = getEntities(mv);

        final Lineage lineage = getLineage(mv);

        // Handle cases where we can short-circuit: Not Found and Not Modified views

        if (lineage == null) return NOT_FOUND_VIEW;

        if (entities == null) return NOT_FOUND_VIEW;

        if (handleIfModifiedSince(lineage, entities, getIfModifiedSince(mv))) return NOT_MODIFIED_VIEW;

        // Handle the 'Accept' header, if it exists

        View resolvedView = handleAcceptHeader(entities, getAccept(mv));

        // Otherwise return the default view

        if (resolvedView == null) {
            resolvedView = defaultView;
            if (resolvedView instanceof BaseStreamingView) {
                ((BaseStreamingView)resolvedView).setEntities(entities);
            }
        }

        return resolvedView;
    }

    /**
     * The {@link DcsModelBuilder} used to serialize the response.
     *
     * @param modelBuilder the model builder
     */
    public void setModelBuilder(DcsModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    /**
     * The {@link DcsModelBuilder} used to serialize the response.
     *
     * @return the model builder
     */
    public DcsModelBuilder getModelBuilder() {
        return modelBuilder;
    }

    /**
     * The default view to be rendered if no other view could be determined from the supplied {@link ModelAndView
     * model}.
     *
     * @return the default view to render
     */
    public View getDefaultView() {
        return defaultView;
    }

    /**
     * The default view to be rendered if no other view could be determined from the supplied {@link ModelAndView
     * model}.
     *
     * @param defaultView the default view to render
     */
    public void setDefaultView(View defaultView) {
        this.defaultView = defaultView;
    }

    private View handleAcceptHeader(List<DcsEntity> entities, String acceptHeader) {
        View v = null;

        if (APPLICATION_XML.equalsIgnoreCase(acceptHeader)) {
            XmlLineageView view = new XmlLineageView(modelBuilder);
            view.setEntities(entities);
            v = view;
        } else if (APPLICATION_JSON.equalsIgnoreCase(acceptHeader)) {
            JsonLineageView view = new JsonLineageView(modelBuilder);
            view.setEntities(entities);
            v = view;
        } else if(APPLICATION_ANY.equalsIgnoreCase(acceptHeader)) {
            if (defaultView instanceof BaseStreamingView) {
                ((BaseStreamingView) defaultView).setEntities(entities);
            }
            v = defaultView;
        } else if (acceptHeader != null) {
            v = NOT_ACCEPTABLE_VIEW;
        }

        return v;
    }

    /**
     * Returns {@code true} if all entries in the {@code lineage} have <em>not</em> been modified since
     * the supplied date.
     *
     * @param lineage         a Lineage
     * @param ifModifiedSince the DateTime since modification
     * @return
     */
    private boolean handleIfModifiedSince(Lineage lineage, List<DcsEntity> entities, DateTime ifModifiedSince) {
        if (ifModifiedSince == null) {
            return false;
        }

        //If the entity list is empty return a not modified error code.
        //Note the lineage is the full lineage and thus may not be empty. 
        if (entities.isEmpty()) {
            return true;
        }
        
        long ifModifiedSinceMillis = ifModifiedSince.getMillis();

        for (LineageEntry e : lineage) {
            if (e.getEntryTimestamp() > ifModifiedSinceMillis) {
                return false;
            }
        }

        return true;
    }

    /**
     * View which returns a 404 Not Found response.
     */
    private final static class NOT_FOUND_VIEW implements View {

        @Override
        public String getContentType() {
            return TEXT_PLAIN;
        }

        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(TEXT_PLAIN);
            OutputStream out = response.getOutputStream();
            out.write(String.format(NOT_FOUND_MSG, model.get(LineageModelAttribute.ID.name())).getBytes());
            out.flush();
            out.close();
        }
    }

    /**
     * View which returns a 304 Not Modified response.
     */
    private final static class NOT_MODIFIED_VIEW implements View {

        @Override
        public String getContentType() {
            // Default method body
            return null;
        }

        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.getOutputStream().close();
        }
    }

    /**
     * View which returns a 405 Unacceptable response.
     */
    private final static class NOT_ACCEPTABLE_VIEW implements View {

        @Override
        public String getContentType() {
            // Default method body
            return null;
        }

        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.getOutputStream().close();
        }
    }
}
