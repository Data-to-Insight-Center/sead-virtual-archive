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
package org.dataconservancy.dcs.lineage.http;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.IdentifierImpl;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.dcs.lineage.http.support.RequestUtil;
import org.dataconservancy.dcs.lineage.impl.LineageEntryImpl;
import org.dataconservancy.dcs.lineage.impl.LineageImpl;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ACCEPT;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ETAG;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ID;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.IFMODIFIEDSINCE;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LASTMODIFIED;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LINEAGE;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertAcceptEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEntitiesEmpty;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEntitiesEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEntitiesNotEmpty;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEtagEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEtagNotNull;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertIfModifiedSinceEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertLastModifiedEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertLineageEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEtagNull;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertLastModifiedNull;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertModelAttributeValueNotNull;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertModelAttributeValueNull;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertNoLineage;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertNullLineage;
import static org.dataconservancy.dcs.lineage.http.support.RequestUtil.calculateDigestForEntities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the Lineage Controller
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/lineage-servlet.xml")
public class LineageControllerTest {

    private static final String HOST = "dataconservancy.org";
    private static final int PORT = 80;
    private static final String APPLICATION_XML = "application/xml";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final IdService idService = mock(IdService.class);
    private final LineageService lineageService = mock(LineageService.class);
    private final LookupQueryService<DcsEntity> lookupQueryService = mock(LookupQueryService.class);
    private final LineageController lineageController =
            new LineageController(lineageService, idService, lookupQueryService);
    private final Date now = DateTime.now().toDate();

    Identifier la, lb; //Lineage identifiers
    Identifier w, x, y, z; //DU identifiers (lineage a)
    Identifier p, q; //more DU identifiers (lineage b)

    private Lineage lineageA;

    private Lineage lineageB;

    @Before
    public void setUp() throws IdentifierNotFoundException {
        // Create all the identifiers for a pair of lineages
        la = new IdentifierImpl(Types.LINEAGE.getTypeName(), "lineage/lineage_a");
        w = new IdentifierImpl(Types.DELIVERABLE_UNIT.getTypeName(), "du_w");
        x = new IdentifierImpl(Types.DELIVERABLE_UNIT.getTypeName(), "du_x");
        y = new IdentifierImpl(Types.DELIVERABLE_UNIT.getTypeName(), "du_y");
        z = new IdentifierImpl(Types.DELIVERABLE_UNIT.getTypeName(), "du_z");

        lb = new IdentifierImpl(Types.LINEAGE.getTypeName(), "lineage/lineage_b");
        p = new IdentifierImpl(Types.DELIVERABLE_UNIT.getTypeName(), "du_p");
        q = new IdentifierImpl(Types.DELIVERABLE_UNIT.getTypeName(), "du_q");
        
        // Configure the behavior on the mock IdService
        when(idService.fromUid(la.getUid())).thenReturn(la);
        when(idService.fromUid(w.getUid())).thenReturn(w);
        when(idService.fromUid(x.getUid())).thenReturn(x);
        when(idService.fromUid(y.getUid())).thenReturn(y);
        when(idService.fromUid(z.getUid())).thenReturn(z);
        when(idService.fromUid(lb.getUid())).thenReturn(lb);
        when(idService.fromUid(p.getUid())).thenReturn(p);
        when(idService.fromUid(q.getUid())).thenReturn(q);
        
        when(idService.fromUrl(la.getUrl())).thenReturn(la);
        when(idService.fromUrl(w.getUrl())).thenReturn(w);
        when(idService.fromUrl(x.getUrl())).thenReturn(x);
        when(idService.fromUrl(y.getUrl())).thenReturn(y);
        when(idService.fromUrl(z.getUrl())).thenReturn(z);
        when(idService.fromUrl(lb.getUrl())).thenReturn(lb);
        when(idService.fromUrl(p.getUrl())).thenReturn(p);
        when(idService.fromUrl(q.getUrl())).thenReturn(q);

        // Create the two lineages and add them to the lineage service
        final LineageEntry lew = new LineageEntryImpl(w.getUid(), la.getUid(), 10);
        final LineageEntry lex = new LineageEntryImpl(x.getUid(), la.getUid(), 20);
        final LineageEntry ley = new LineageEntryImpl(y.getUid(), la.getUid(), 30);
        final LineageEntry lez = new LineageEntryImpl(z.getUid(), la.getUid(), 40);
        List<LineageEntry> llea = new ArrayList<LineageEntry>();
        llea.add(lez);
        llea.add(ley);
        llea.add(lex);
        llea.add(lew);

        List<LineageEntry> lleb = new ArrayList<LineageEntry>();
        lleb.add(new LineageEntryImpl(q.getUid(), lb.getUid(), 100));
        lleb.add(new LineageEntryImpl(p.getUid(), lb.getUid(), 50));

        lineageA = new LineageImpl(llea);
        lineageB = new LineageImpl(lleb);

        // Configure the behavior on the mock LineageService
        when(lineageService.getLineage(la.getUid())).thenReturn(lineageA);
        when(lineageService.getLineage(lb.getUid())).thenReturn(lineageB);
        when(lineageService.getLineage(la.getUrl().toString())).thenReturn(lineageA);
        when(lineageService.getLineage(lb.getUrl().toString())).thenReturn(lineageB);
        when(lineageService.getLineage(w.getUid())).thenReturn(lineageA);
        when(lineageService.getLineage(x.getUid())).thenReturn(lineageA);
        when(lineageService.getLineage(y.getUid())).thenReturn(lineageA);
        when(lineageService.getLineage(z.getUid())).thenReturn(lineageA);
        when(lineageService.getLineage(w.getUrl().toString())).thenReturn(lineageA);
        when(lineageService.getLineage(x.getUrl().toString())).thenReturn(lineageA);
        when(lineageService.getLineage(y.getUrl().toString())).thenReturn(lineageA);
        when(lineageService.getLineage(z.getUrl().toString())).thenReturn(lineageA);
        when(lineageService.getLineage(p.getUid())).thenReturn(lineageB);
        when(lineageService.getLineage(q.getUid())).thenReturn(lineageB);
        when(lineageService.getLineage(p.getUrl().toString())).thenReturn(lineageB);
        when(lineageService.getLineage(q.getUrl().toString())).thenReturn(lineageB);

        when(lineageService.getEntryForDate(la.getUid(), 25L)).thenReturn(lex);

        when(lineageService.getLineageForDateRange(la.getUid(), 5L, 20L)).thenReturn(
                new LineageImpl(Arrays.asList(lex, lew)));

        when(lineageService.getLineageForDateRange(x.getUid(), 15L, 30L)).thenReturn(
                        new LineageImpl(Arrays.asList(ley, lex)));

        when(lineageService.getLineageForEntityRange(x.getUid(), y.getUid())).thenReturn(
                new LineageImpl(Arrays.asList(ley, lex)));

        when(lineageService.getLineageForEntityRange(y.getUid(), null)).thenReturn(
                new LineageImpl(Arrays.asList(lez, ley)));

        // Configure the behavior of the mock LookupQueryService for Lineage A
        for (LineageEntry entry : lineageA) {
            try {
                when(lookupQueryService.lookup(entry.getEntityId()))
                        .thenReturn(new DcsEntity(entry.getEntityId()));
            } catch (QueryServiceException e) {
                throw new RuntimeException(e); // should never happen.
            }
        }

        // Configure the behavior of the mock LookupQueryService for Lineage B
        // q will be a reject and lqs should throw an exception any time it sees it
        for (LineageEntry entry : lineageB) {
            try {
                if (entry.getEntityId().equals(q.getUid())) {
                    when(lookupQueryService.lookup(entry.getEntityId()))
                            .thenThrow(new QueryServiceException("Rejecting entity " + entry.getEntityId()));

                } else {
                    when(lookupQueryService.lookup(entry.getEntityId()))
                            .thenReturn(new DcsEntity(entry.getEntityId()));
                }
            } catch (QueryServiceException e) {
                throw new RuntimeException(e); // should never happen
            }
        }

    }

    /**
     * Insures that a request for an existing lineage correctly populates the model;
     * e.g. http://dataconservancy.org/lineage/lineage_a
     *
     * @throws Exception
     */
    @Test
    public void testControllerSetsModelAttributesForLineageGetRequest() throws Exception {
        final Identifier lineageId = la;
        final Lineage expectedLineage = lineageA;
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        for (LineageEntry e : expectedLineage) {
            final DcsEntity dcsEntity = new DcsEntity();
            dcsEntity.setId(e.getEntityId());
            expectedEntities.add(dcsEntity);
        }
        final MockHttpServletRequest mockReq = newMockRequest(lineageId);

        Model m = lineageController.handleLineageGetRequest(APPLICATION_XML, now, mockReq);

        assertLineageEquals(m, expectedLineage);
        assertEtagEquals(m, RequestUtil.calculateDigestForLineage(expectedLineage));
        assertLastModifiedEquals(m, new Date(40));
        assertEntitiesNotEmpty(m);
        assertEntitiesEquals(m, expectedEntities);
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertModelAttributeValueNotNull(m, ID);

        final Map<String, Object> map = m.asMap();
        assertEquals(idService.fromUid(expectedLineage.getId()), idService.fromUrl(
                new URL((String) map.get(ID.name()))));
    }

    /**
     * Insures that a request for a non-existent lineage correctly populates the model;
     * e.g. http://dataconservancy.org/lineage/
     *
     *
     * @throws Exception
     */
    @Test
    public void testHandleEmptyGetRequestReturnsNothing() throws Exception {
        final MockHttpServletRequest mockReq = newMockRequest("GET", "/lineage/", HOST, PORT);

        Model m = lineageController.handleEmptyGetRequest(APPLICATION_XML, now, mockReq);

        assertNoLineage(m);
        assertEntitiesEmpty(m);
        assertEtagNull(m);
        assertLastModifiedNull(m);
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertModelAttributeValueNull(m, ID);
    }

    /**
     * Insures that a HEAD request for an existing lineage populates the model correctly;
     * e.g. HEAD http://dataconservancy.org/lineage/lineage_a
     *
     * @throws Exception
     */
    @Test
    public void testHandleLineageHeadRequest() throws Exception {
        final Identifier lineageId = la;
        final Lineage expectedLineage = lineageA;
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        for (LineageEntry e : expectedLineage) {
            final DcsEntity dcsEntity = new DcsEntity();
            dcsEntity.setId(e.getEntityId());
            expectedEntities.add(dcsEntity);
        }
        final MockHttpServletRequest mockReq = newMockRequest("HEAD", lineageId);

        Model m = lineageController.handleLineageGetRequest(APPLICATION_XML, now, mockReq);

        assertLineageEquals(m, expectedLineage);
        assertEtagEquals(m, RequestUtil.calculateDigestForLineage(expectedLineage));
        assertLastModifiedEquals(m, new Date(40));
        assertEntitiesEquals(m, expectedEntities);
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertModelAttributeValueNotNull(m, ID);
        final Map<String, Object> map = m.asMap();
        assertEquals(idService.fromUid(expectedLineage.getId()), idService.fromUrl(
                new URL((String) map.get(ID.name()))));
    }

    /**
     * Insures that a request using a non-existent lineage id populates the model correctly;
     * e.g. http://dataconservancy.org/lineage/foo
     *
     * @throws Exception
     */
    @Test
    public void testHandleLineageGetRequestWithBadLineageId() throws Exception {
        final String idPart = "lineage/foo";
        final MockHttpServletRequest mockReq = newMockRequest(new IdentifierImpl(Types.LINEAGE.getTypeName(), idPart));

        Model m = lineageController.handleLineageGetRequest(APPLICATION_XML, now, mockReq);

        assertNullLineage(m);
        assertEntitiesEmpty(m);
        assertEtagNull(m);
        assertLastModifiedNull(m);
        assertIfModifiedSinceEquals(m, now);
        assertEquals(APPLICATION_XML, m.asMap().get(ACCEPT.name()));
        assertEquals("http://" + HOST + "/" + idPart, m.asMap().get(ID.name()));
    }

    /**
     * Insures that when the LookupQueryService throws an exception looking up an entity, that
     * the model is populated correctly.  In this test, the query service is configured to throw
     * an exception when encountering {@link #q Identifier Q}.
     *
     * @throws Exception
     */
    @Test
    public void testHandleLineageGetRequestWithBadEntityId() throws Exception {
        final MockHttpServletRequest mockReq = newMockRequest(lb);

        Model m = null;
        try {
            m = lineageController.handleLineageGetRequest(APPLICATION_XML, now, mockReq);
            throw new RuntimeException("Expected an exception to be thrown!");
        } catch (Exception e) {
            assertNull(m);
            assertEquals(QueryServiceException.class, e.getClass());
        }
    }

    @Test
    public void testHandleOriginalGetRequestWithLineageId() throws Exception {
        final Identifier lineageId = la;
        final Lineage expectedLineage = new LineageImpl(Arrays.asList(lineageA.getOldest()));
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        for (LineageEntry e : expectedLineage) {
            final DcsEntity dcsEntity = new DcsEntity();
            dcsEntity.setId(e.getEntityId());
            expectedEntities.add(dcsEntity);
        }

        Model m = lineageController.handleOriginalGetRequest(APPLICATION_XML, now, lineageId.getUid());

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForLineage(expectedLineage));
        assertLastModifiedEquals(m, new Date(10));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(lineageId.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Insures that the model is correctly populated when requesting the original entry
     * of a lineage using an identifier of an entity;
     * e.g. http://dataconservancy.org/lineage/original?id=du_x
     *
     * @throws Exception
     */
    @Test
    public void testHandleOriginalGetRequestWithEntityId() throws Exception {
        final Lineage expectedLineage = new LineageImpl(Arrays.asList(
                (LineageEntry)new LineageEntryImpl(w.getUid(), la.getUid(), 10)));
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        expectedEntities.add(new DcsEntity(w.getUid()));

        Model m = lineageController.handleOriginalGetRequest(APPLICATION_XML, now, x.getUid());

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(10));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(x.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Insures that the model is correctly populated when requesting the latest entry
     * of a lineage using a lineage identifier;
     * e.g. http://dataconservancy.org/lineage/latest?id=lineage/lineage_a
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestGetRequestWithLineageId() throws Exception {
        final DcsEntity expectedEntity = new DcsEntity(z.getUid());
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        expectedEntities.add(expectedEntity);
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleLatestGetRequest(APPLICATION_XML, now, la.getUid(), -1);

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(40));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Insures that the model is correctly populated when requesting the latest entry
     * of a lineage using an entity identifier;
     * e.g. http://dataconservancy.org/lineage/latest?id=du_x
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestGetRequestWithEntityId() throws Exception {
        final DcsEntity expectedEntity = new DcsEntity(z.getUid());
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        expectedEntities.add(expectedEntity);
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleLatestGetRequest(APPLICATION_XML, now, x.getUid(), -1);

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(40));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(x.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that even when a lineage's latest entry cannot be looked up ({@link #q}), the original
     * entry can be;
     * e.g. http://dataconservancy.org/lineage/original/?id=lineage/lineage_b
     *
     * @throws Exception
     */
    @Test
    public void testHandleOriginalWithBadLatest() throws Exception {
        final DcsEntity expectedEntity = new DcsEntity(p.getUid());
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        expectedEntities.add(expectedEntity);
        final Lineage expectedLineage = lineageB;

        Model m = lineageController.handleOriginalGetRequest(APPLICATION_XML, now, lb.getUid());

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(50));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(lb.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that when a lineage's latest entry cannot be looked up ({@link #q}), the latest endpoint throws
     * an exception;
     * e.g. http://dataconservancy.org/lineage/latest/?id=lineage/lineage_b
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestWithBadLatest() throws Exception {
        Model m = null;
        try {
            m = lineageController.handleLatestGetRequest(APPLICATION_XML, now, lb.getUid(), -1);
            throw new RuntimeException("Expected an exception to be thrown!");
        } catch (Exception e) {
            assertNull(m);
            assertEquals(QueryServiceException.class, e.getClass());
        }
    }

    /**
     * Demonstrates that obtaining the latest entry of a lineage can be successfully bounded by a timestamp;
     * e.g. http://dataconservancy.org/lineage/latest?id=lineage/lineage_a&ts=25
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestWithTimestamp() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        expectedEntities.add(new DcsEntity(x.getUid()));
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleLatestGetRequest(APPLICATION_XML, now, la.getUid(), 25);

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(20));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that obtaining the latest entry of a lineage with a timestamp earlier than all entries in the
     * lineage returns an empty lineage;
     * e.g. http://dataconservancy.org/lineage/latest?id=lineage/lineage_a&ts=5
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestWithTimestampBeforeStart() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>();
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleLatestGetRequest(APPLICATION_XML, now, la.getUid(), 5);

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagNull(m);
        assertLastModifiedNull(m);
        assertAcceptEquals(m, APPLICATION_XML);
        assertIfModifiedSinceEquals(m, now);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that a range of lineage entries can be obtained by specifiying entity ids as 'from' and 'to'
     * parameters;
     * e.g. http://dataconservancy.org/lineage/search?from=du_x&to=du_y
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenEntities() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>(
                Arrays.asList(new DcsEntity(y.getUid()), new DcsEntity(x.getUid())));
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, "", x.getUid(), y.getUid());

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(30));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(x.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that a range of lineage entries can be obtained by specifying only a 'from' parameter, leaving
     * the 'to' parameter null;
     * e.g. http://dataconservancy.org/lineage/search?from=du_y
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenEntitiesWithOneNull() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>(
                    Arrays.asList(new DcsEntity(z.getUid()), new DcsEntity(y.getUid())));
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, "", y.getUid(), null);

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(40));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(y.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that a range of lineage entries can be obtained by specifying with a valid 'from' parameter, and a
     * 'to' parameter equal to -1 (not sure if this is a good thing to allow or not).
     * e.g. http://dataconservancy.org/lineage/search?from=du_y&to=-1
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenEntitiesWithNegativeDefaultTo() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>(
                Arrays.asList(new DcsEntity(z.getUid()), new DcsEntity(y.getUid())));
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, "", y.getUid(), null);

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(40));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(y.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Insures that the model is correctly populated when a search bounded by entities belonging to different
     * lineages is handled;
     * e.g. http://dataconservancy.org/lineage/search?from=du_x&to=du_p (where x and p are from different lineages)
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenEntitiesOfDifferentLineages() throws Exception {
        Model m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, "", x.getUid(), p.getUid());

        assertNullLineage(m);
        assertEntitiesEmpty(m);
        assertEtagNull(m);
        assertLastModifiedNull(m);
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertModelAttributeValueNull(m, ID);
    }

    /**
     * Insures that the model is correctly populated when using timestamps to bound a lineage search;
     * e.g. http://dataconservancy.org/lineage/search?from=5&to=20
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenDatesWithLineage() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>(
                Arrays.asList(new DcsEntity(x.getUid()), new DcsEntity(w.getUid())));
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, la.getUid(), "5", "20");

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(20));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Insures that the model is correctly populated when using timestamps to bound a lineage search;
     * e.g. http://dataconservancy.org/lineage/search?from=15&to=30
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenDatesWithEntity() throws Exception {
        final List<DcsEntity> expectedEntities = new ArrayList<DcsEntity>(
                Arrays.asList(new DcsEntity(y.getUid()), new DcsEntity(x.getUid())));
        final Lineage expectedLineage = lineageA;

        Model m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, x.getUid(), "15", "30");

        assertLineageEquals(m, expectedLineage);
        assertEntitiesEquals(m, expectedEntities);
        assertEtagEquals(m, RequestUtil.calculateDigestForEntities(expectedEntities));
        assertLastModifiedEquals(m, new Date(30));
        assertIfModifiedSinceEquals(m, now);
        assertAcceptEquals(m, APPLICATION_XML);
        assertEquals(x.getUid(), m.asMap().get(ID.name()));
    }

    /**
     * Demonstrates that performing a search mixing the types of the 'to' and 'from' parameters is illegal, even
     * when a valid lineage id is specified;
     * e.g. http://dataconservancy.org/lineage/search?id=lineage/lineage_b&from=6549874&to=du_x
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenEntityAndDateWithId() throws Exception {
        Model m = null;
        try {
            m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, lb.getUid(), "6549874", x.getUid());
            throw new RuntimeException("Expected an exception to be thrown.");
        } catch (Exception e) {
            assertNull(m);
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    /**
     * Demonstrates that performing a search mixing the types of the 'to' and 'from' parameters is illegal, even
     * when the id is empty;
     * e.g. http://dataconservancy.org/lineage/search?id=du_q&from=du_x&to=6549874
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchBetweenEntityAndDateWithoutId() throws Exception {
        Model m = null;
        try {
            m = lineageController.handleSearchGetRequest(APPLICATION_XML, now, "", q.getUid(), "6549874");
            throw new RuntimeException("Expected an exception to be thrown!");
        } catch (Exception e) {
            assertNull(m);
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    private MockHttpServletRequest newMockRequest(String method, String requestUri, String host, int port) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, requestUri);
        req.setRemoteHost(host);
        req.setRemotePort(port);
        if (port == 443) {
            req.setScheme("https");
            req.setSecure(true);
        } else {
            req.setScheme("http");
            req.setSecure(false);
        }

        log.debug("Generated mock lineage request: '" + req.getMethod() + " " + req.getRequestURI() + "'");
        return req;
    }

    private MockHttpServletRequest newMockRequest(Identifier lineageIdentifier) {
       return newMockRequest("GET", lineageIdentifier);
    }

    private MockHttpServletRequest newMockRequest(String methodName, Identifier lineageIdentifier) {
        return newMockRequest(methodName, "/" + lineageIdentifier.getUid(), HOST, PORT);
    }

}
