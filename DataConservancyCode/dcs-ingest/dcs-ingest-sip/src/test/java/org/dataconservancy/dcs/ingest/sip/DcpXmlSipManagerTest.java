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
package org.dataconservancy.dcs.ingest.sip;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Bootstrap;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.PackageException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DcpXmlSipManagerTest {

    private static Schema schema;

    private static final String BAD_SIP = "/nonValidSip.xml";

    private static final String GOOD_SIP = "/validSip.xml";

    private static final BulkIdCreationService idService = new MemoryIdServiceImpl();

    private static final InlineEventManager eventManager =
            new InlineEventManager();

    private static final Abdera abdera = new Abdera();

    private static final SipStager stager = new MemoryStager();

    @BeforeClass
    public static void getSchema() throws Exception {
        schema =
                SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new StreamSource(DcpXmlSipManager.class
                                .getResourceAsStream(DcpXmlSipManager.DCP_SCHEMA_LOC)));

        eventManager.setIdService(idService);
        eventManager.setSipStager(stager);
    }

    @Test
    public void validSipTest() throws Exception {
        assertNotNull("Could not get sip data!", this.getClass()
                .getResourceAsStream(GOOD_SIP));
        DcpXmlSipManager mgr = getManager(new BootstrapProbe());
        mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                    "application/xml",
                    "http://dataconservancy.org/schemas/dcp/1.0",
                    null);
        /* Just checking to make sure no exception is thrown */
    }

    @Test(expected = PackageException.class)
    public void invalidSipTest() throws Exception {
        assertNotNull("Could not get sip data!", this.getClass()
                .getResourceAsStream(BAD_SIP));
        DcpXmlSipManager mgr = getManager(new BootstrapProbe());
        mgr.deposit(this.getClass().getResourceAsStream(BAD_SIP),
                    "application/xml",
                    "http://dataconservancy.org/schemas/dcp/1.0",
                    null);
    }

    @Test
    public void isBootstrapCalledTest() throws Exception {
        BootstrapProbe probe = new BootstrapProbe();
        DcpXmlSipManager mgr = getManager(probe);
        mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                    "application/xml",
                    "http://dataconservancy.org/schemas/dcp/1.0",
                    null);
        assertNotNull(probe.ingested);
    }

    @Test
    public void isDepositEventSetTest() throws Exception {
        BootstrapProbe probe = new BootstrapProbe();
        DcpXmlSipManager mgr = getManager(probe);
        mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                    "application/xml",
                    "http://dataconservancy.org/schemas/dcp/1.0",
                    null);

        /* If it worked, this *won't* throw an exception */
        eventManager.getEventByType(probe.getIngestId(), Events.DEPOSIT);
    }

    @Test
    public void initialStatusTest() throws Exception {
        BootstrapProbe probe = new BootstrapProbe();
        DcpXmlSipManager mgr = getManager(probe);
        DepositInfo deposit =
                mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                            "application/xml",
                            "http://dataconservancy.org/schemas/dcp/1.0",
                            null);
        DepositDocument status = deposit.getDepositStatus();
        assertNotNull(deposit);
        assertNotNull(status);
        assertEquals("application/atom+xml;type=feed", status.getMimeType());

        Document<Feed> doc = abdera.getParser().parse(status.getInputStream());
        Feed feed = doc.getRoot();

        assertTrue(feed.getEntries().size() > 0);

        boolean foundDepositEvent = false;
        for (Entry e : feed.getEntries()) {
            if (e.getTitle().equals(Events.DEPOSIT)) {
                foundDepositEvent = true;
            }
        }

        assertTrue(foundDepositEvent);
    }

    @Test
    public void initialContentTest() throws Exception {
        BootstrapProbe probe = new BootstrapProbe();
        DcpXmlSipManager mgr = getManager(probe);
        DepositInfo deposit =
                mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                            "application/xml",
                            "http://dataconservancy.org/schemas/dcp/1.0",
                            null);
        DepositDocument content = deposit.getDepositContent();
        assertNotNull(deposit);
        assertNotNull(content);
        assertEquals("application/xml", content.getMimeType());

        schema.newValidator().validate(new StreamSource(content
                .getInputStream()));

    }

    @Test
    public void retrievedStatusTest() throws Exception {
        BootstrapProbe probe = new BootstrapProbe();
        DcpXmlSipManager mgr = getManager(probe);
        DepositInfo deposit =
                mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                            "application/xml",
                            "http://dataconservancy.org/schemas/dcp/1.0",
                            null);

        /* Discard the original and retrieve a new deposit info */
        deposit = mgr.getDepositInfo(deposit.getDepositID());

        DepositDocument status = deposit.getDepositStatus();
        assertNotNull(deposit);
        assertNotNull(status);
        assertEquals("application/atom+xml;type=feed", status.getMimeType());

        Document<Feed> doc = abdera.getParser().parse(status.getInputStream());
        Feed feed = doc.getRoot();

        assertTrue(feed.getEntries().size() > 0);

        boolean foundDepositEvent = false;
        for (Entry e : feed.getEntries()) {
            if (e.getTitle().equals(Events.DEPOSIT)) {
                foundDepositEvent = true;
            }
        }

        assertTrue(foundDepositEvent);
    }

    @Test
    public void retrievedContentTest() throws Exception {
        BootstrapProbe probe = new BootstrapProbe();
        DcpXmlSipManager mgr = getManager(probe);
        DepositInfo deposit =
                mgr.deposit(this.getClass().getResourceAsStream(GOOD_SIP),
                            "application/xml",
                            "http://dataconservancy.org/schemas/dcp/1.0",
                            null);

        /* Discard the original and retrieve a new deposit info */
        deposit = mgr.getDepositInfo(deposit.getDepositID());

        DepositDocument content = deposit.getDepositContent();
        assertNotNull(deposit);
        assertNotNull(content);
        assertEquals("application/xml", content.getMimeType());

        schema.newValidator().validate(new StreamSource(content
                .getInputStream()));
    }

    private DcpXmlSipManager getManager(BootstrapProbe boot) {
        DcpXmlSipManager mgr = new DcpXmlSipManager();
        mgr.setEventManager(eventManager);
        mgr.setBootstrap(boot);
        mgr.setSipStager(stager);
        return mgr;
    }

    private class BootstrapProbe
            implements Bootstrap {

        public String ingested = null;

        public void startIngest(String sip) {
            ingested = sip;
        }

        public String getIngestId() {
            return ingested;
        }

    }
}
