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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.http.LineageModelAttribute;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ACCEPT;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LINEAGE;
import static org.dataconservancy.dcs.lineage.http.view.ExposeModelHandleInterceptor.LINEAGE_MODEL;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.APPLICATION_JSON;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.APPLICATION_XML;
import static org.dataconservancy.dcs.lineage.http.view.ViewUtil.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the LineageViewResolver
 */
public class LineageViewResolverTest {

    /**
     * Provide a model populated with a Lineage, Entities, and a null value for the
     * "Accept" header.  The expected behavior is that the default view will be returned.
     * Since the default view has not been set, the view should be null.
     *
     * @throws Exception
     */
    @Test
    public void testGetAcceptNullValueWithNullDefaultView() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, null);

        final LineageViewResolver underTest = newViewResolver(mv);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNull(v);
    }

    /**
     * Provide a model populated with a Lineage, Entities, and a null value for the
     * "Accept" header.  The expected behavior is that the default view will be returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetAcceptNullValueWithDefaultView() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, null);

        final View expectedView = new View() {
            @Override
            public String getContentType() {
                return "foo/bar";
            }

            @Override
            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

            }
        };

        final LineageViewResolver underTest = newViewResolver(mv);
        underTest.setDefaultView(expectedView);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);
        assertEquals(expectedView, v);
    }

    /**
     * Provide a model populated with a Lineage, Entities, and a value 'application/xml' for the
     * "Accept" header.  The expected behavior is that the XML Lineage View will be returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetAcceptApplicationXml() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);
        underTest.setModelBuilder(mock(DcsModelBuilder.class));

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);
        assertEquals(APPLICATION_XML, v.getContentType());
    }

    /**
     * Provide a model populated with a Lineage, Entities, and a value 'application/json' for the
     * "Accept" header.  The expected behavior is that the JSON Lineage View will be returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetAcceptApplicationJson() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, APPLICATION_JSON);

        final LineageViewResolver underTest = newViewResolver(mv);
        underTest.setModelBuilder(mock(DcsModelBuilder.class));

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);
        assertEquals(APPLICATION_JSON, v.getContentType());
    }

    /**
     * Provide a model populated with a Lineage, Entities, and a value 'text/plain' for the
     * "Accept" header.  The expected behavior is that a 405 NOT ACCEPTABLE will be returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetAcceptTextPlain() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, TEXT_PLAIN);

        final LineageViewResolver underTest = newViewResolver(mv);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest(), spy);
        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, spy.getStatus());
    }

    /**
     * Provide a model with no Lineage, so we expect a 404 response.
     *
     * @throws Exception
     */
    @Test
    public void testGetNotFoundNoLineage() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest(), spy);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, spy.getStatus());
    }

    /**
     * Provide a model with null Lineage, so we expect a 404 response.
     *
     * @throws Exception
     */
    @Test
    public void testGetNotFoundNullLineage() throws Exception {
        final List<DcsEntity> entities = newEntities();
        entities.add(new DcsEntity());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, null)
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest(), spy);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, spy.getStatus());
    }

    /**
     * Provide a model with an empty Lineage.  A Lineage can be empty for valid reasons: for example, search
     * constraints insured that no lineage entries matched.  This is contrasted with a {@code null} lineage, which
     * would equate to a 404 not found.
     * <p/>
     * So, empty lineages result in a 200 OK response, even though the lineage is empty, because the lineage does
     * exist, it was just constrained to the point where no lineage entries were returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetNotFoundEmptyLineage() throws Exception {
        final List<DcsEntity> entities = newEntities();
        
        final List<LineageEntry> entries = new ArrayList<LineageEntry>();

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());
        
        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, ENTITIES, entities)
                .addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);
        underTest.setModelBuilder(mock(DcsModelBuilder.class));

        View v = underTest.resolveViewName("fooView", Locale.getDefault());
        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest("GET", "/any/request/uri"), spy);
        assertEquals(HttpServletResponse.SC_OK, spy.getStatus());
    }

    /**
     * Provide a model with no Entities, so we expect a 404 response.
     *
     * @throws Exception
     */
    @Test
    public void testGetNotFoundNoEntities() throws Exception {
        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest(), spy);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, spy.getStatus());
    }

    /**
     * Provide a model with null Entities, so we expect a 404 response.
     *
     * @throws Exception
     */
    @Test
    public void testGetNotFoundNullEntities() throws Exception {
        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ENTITIES, null)
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest(), spy);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, spy.getStatus());
    }

    /**
     * Provide a model with empty Entities, so we expect a 200 response.  See javadoc for
     * {@link #testGetNotFoundEmptyLineage}.
     *
     * @throws Exception
     */
    @Test
    public void testGetNotFoundEmptyEntities() throws Exception {
        final List<LineageEntry> entries = new ArrayList<LineageEntry>();
        entries.add(mock(LineageEntry.class));

        final Lineage mockLineage = mock(Lineage.class);
        when(mockLineage.iterator()).thenReturn(entries.iterator());

        final ModelAndView mv = newModelAndView(null, null);

        addAttribute(mv, LINEAGE, mockLineage)
                .addAttribute(mv, ENTITIES, Collections.emptyList())
                .addAttribute(mv, ACCEPT, APPLICATION_XML);

        final LineageViewResolver underTest = newViewResolver(mv);
        underTest.setModelBuilder(mock(DcsModelBuilder.class));

        View v = underTest.resolveViewName("fooView", Locale.getDefault());

        assertNotNull(v);

        MockHttpServletResponse spy = new MockHttpServletResponse();

        v.render(mv.getModel(), new MockHttpServletRequest("GET", "/any/request/uri"), spy);
        assertEquals(HttpServletResponse.SC_OK, spy.getStatus());
    }

    /**
     * Create a new, empty, List of DcsEntities, and add the supplied entities to the list.
     *
     * @param entities
     * @return
     */
    private List<DcsEntity> newEntities(DcsEntity... entities) {
        ArrayList<DcsEntity> e = new ArrayList<DcsEntity>();
        e.addAll(Arrays.asList(entities));
        return e;
    }

    /**
     * Create a new ModelAndView object.  The supplied parameters may be null.
     *
     * @param viewName
     * @param attributes
     * @return
     */
    private ModelAndView newModelAndView(String viewName, Map<String, String> attributes) {
        if (viewName == null) {
            return new ModelAndView();
        }

        if (attributes == null) {
            return new ModelAndView(viewName);
        }

        ModelAndView mav = new ModelAndView(viewName, attributes);
        return mav;
    }

    /**
     * Create a new LineageViewResolver which will have access to the supplied ModelAndView via a MockHttpServletRequest.
     * @param mv
     * @return
     */
    private LineageViewResolver newViewResolver(final ModelAndView mv) {
        return new LineageViewResolver(new ServletRequestAttributesSource() {
                @Override
                public ServletRequestAttributes getRequestAttributes() {
                    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
                    mockRequest.setAttribute(LINEAGE_MODEL, mv);
                    return new ServletRequestAttributes(mockRequest);
                }
            });
    }

    /**
     * Adds the supplied attribute/value pair to the supplied model and view.
     * 
     * @param mav
     * @param attribute
     * @param value
     * @return
     */
    private LineageViewResolverTest addAttribute(ModelAndView mav, LineageModelAttribute attribute, Object value) {
        mav.addObject(attribute.name(), value);
        return this;
    }

}
