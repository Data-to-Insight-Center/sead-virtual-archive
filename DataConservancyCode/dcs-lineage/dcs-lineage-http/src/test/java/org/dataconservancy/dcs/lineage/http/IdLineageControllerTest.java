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

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.http.support.MockDcsEntityLookupQueryService;
import org.dataconservancy.dcs.lineage.http.support.MockIdService;
import org.dataconservancy.dcs.lineage.http.support.MockLineageService;
import org.dataconservancy.dcs.lineage.http.support.RequestUtil;
import org.dataconservancy.dcs.lineage.impl.LineageEntryImpl;
import org.dataconservancy.dcs.lineage.impl.LineageImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class IdLineageControllerTest {
    private final MockIdService idService = new MockIdService();
    private final MockLineageService lineageService = new MockLineageService();
    private final MockDcsEntityLookupQueryService lookupQueryService = new MockDcsEntityLookupQueryService();
    private final LineageController lineageController = new LineageController(lineageService, idService, lookupQueryService);
    private final RequestUtil util = new RequestUtil();

    Identifier la, lb; //Lineage identifiers
    Identifier w, x; //DU identifiers (lineage a)
    Identifier p; //more DU identifiers (lineage b)

    @Before
    public void setUp() {
        idService.injectIdentifierBeginning("http://instance.org");

        //Create all the identifiers for a pair of lineages
        la = idService.create(Types.LINEAGE.getTypeName());
        w = idService.create(Types.DELIVERABLE_UNIT.getTypeName());
        x = idService.create(Types.DELIVERABLE_UNIT.getTypeName());

        lb = idService.create(Types.LINEAGE.getTypeName());
        p = idService.create(Types.DELIVERABLE_UNIT.getTypeName());

        //Create the two lineages and add them to the lineage service
        List<LineageEntry> llea = new ArrayList<LineageEntry>();
        llea.add(new LineageEntryImpl(x.getUid(), la.getUid(), 20));
        llea.add(new LineageEntryImpl(w.getUid(), la.getUid(), 10));

        List<LineageEntry> lleb = new ArrayList<LineageEntry>();
        lleb.add(new LineageEntryImpl(p.getUid(), lb.getUid(), 50));

        Map<String, Lineage> lineageMap = new HashMap<String, Lineage>();
        lineageMap.put(la.getUid(), new LineageImpl(llea));
        lineageMap.put(lb.getUid(), new LineageImpl(lleb));

        lineageService.injectLineageMap(lineageMap);


        //Make sure the lineage service is using the right id service
        lineageService.injectIdService(idService);
    }

    @Test
    public void testIdNullIfIdpartIsEmpty() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("GET",
                "/lineage/",
                "instance.org",
                80);

        Model m = lineageController.handleEmptyGetRequest(mimeType, now, mockReq);
        
        assertNull(m.asMap().get(ID.name()));
    }

    @Test
    public void testLineageIdFromURL() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";
        
        //Lineage id that exists
        String idPart = la.getUid().substring(la.getUid().lastIndexOf('/') + 1);
        MockHttpServletRequest mockReq = newMockRequest("GET",
                "/lineage/" + idPart,
                "instance.org",
                80);

        Model m = lineageController.handleLineageGetRequest(mimeType, now, mockReq);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));

        //Lineage id that doesn't exist
        idPart = "foo";
        mockReq = newMockRequest("GET",
                "/lineage/" + idPart,
                "instance.org",
                80);

        m = lineageController.handleLineageGetRequest(mimeType, now, mockReq);
        assertEquals(util.buildRequestUrl(mockReq), m.asMap().get(ID.name()));
    }
    
    @Test
    public void testLineageIdFromOriginal() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";

        //Lineage id that exists
        Model m = lineageController.handleOriginalGetRequest(mimeType, now, la.getUid());
        assertEquals(la.getUid(), m.asMap().get(ID.name()));

        //id that doesn't exist
        m = lineageController.handleOriginalGetRequest(mimeType, now, "foo");
        assertEquals("foo", m.asMap().get(ID.name()));
    }
    
    @Test
    public void testLineageIdFromLatest() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";

        //Lineage id that exists
        Model m = lineageController.handleLatestGetRequest(mimeType, now, la.getUid(), -1);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));

        //Lineage id that exists with timestamp
        m = lineageController.handleLatestGetRequest(mimeType, now, la.getUid(), 15);
        assertEquals(la.getUid(), m.asMap().get(ID.name()));

        //Lineage id that doesn't exist
        m = lineageController.handleLatestGetRequest(mimeType, now, "foo", -1);
        assertEquals("foo", m.asMap().get(ID.name()));

        //Lineage id that doesn't exist with timestamp
        m = lineageController.handleLatestGetRequest(mimeType, now, "foo", 15);
        assertEquals("foo", m.asMap().get(ID.name()));
    }
    
    @Test
    public void testLineageIdFromSearch() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";

        //Lineage id that exists
        Model m = lineageController.handleSearchGetRequest(mimeType, now, la.getUid(), "-1", "-1");
        assertEquals(la.getUid(), m.asMap().get(ID.name()));

        //Lineage id that exists
        m = lineageController.handleSearchGetRequest(mimeType, now, la.getUid(), "5", "15");
        assertEquals(la.getUid(), m.asMap().get(ID.name()));
    }

    @Test
    public void testEntityIdFromOriginal() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";

        //Entity id that exists
        Model m = lineageController.handleOriginalGetRequest(mimeType, now, w.getUid());
        assertEquals(w.getUid(), m.asMap().get(ID.name()));

    }
    
    @Test
    public void testEntityIdFromLatest() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";

        //Entity id that exists
        Model m = lineageController.handleLatestGetRequest(mimeType, now, w.getUid(), -1);
        assertEquals(w.getUid(), m.asMap().get(ID.name()));

        //Entity id that exists with timestamp
        m = lineageController.handleLatestGetRequest(mimeType, now, w.getUid(), 15);
        assertEquals(w.getUid(), m.asMap().get(ID.name()));

    }
    
    @Test
    public void testEntityIdFromSearch() throws Exception {
        final Date now = DateTime.now().toDate();
        final String mimeType = "application/xml";

        //Entity id as from
        Model m = lineageController.handleSearchGetRequest(mimeType, now, la.getUid(), w.getUid(), "-1");
        assertEquals(w.getUid(), m.asMap().get(ID.name()));

        //Entity id as to
        m = lineageController.handleSearchGetRequest(mimeType, now, la.getUid(), "-1", x.getUid());
        assertEquals(x.getUid(), m.asMap().get(ID.name()));

        //Entity id as both
        m = lineageController.handleSearchGetRequest(mimeType, now, la.getUid(), w.getUid(), x.getUid());
        assertEquals(w.getUid(), m.asMap().get(ID.name()));

        //Entity ids from different lineages
        m = lineageController.handleSearchGetRequest(mimeType, now, la.getUid(), w.getUid(), p.getUid());
        assertNull(m.asMap().get(ID.name()));
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

        return req;
    }
}
