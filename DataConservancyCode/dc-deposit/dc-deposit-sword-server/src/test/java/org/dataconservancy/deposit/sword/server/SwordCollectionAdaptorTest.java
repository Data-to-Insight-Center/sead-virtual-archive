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
package org.dataconservancy.deposit.sword.server;

import javax.xml.namespace.QName;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Element;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.util.Constants;
import org.dataconservancy.deposit.sword.SWORDPackaging;
import org.dataconservancy.deposit.sword.extension.AcceptPackaging;
import org.dataconservancy.deposit.sword.extension.CollectionPolicy;
import org.dataconservancy.deposit.sword.extension.Mediation;
import org.dataconservancy.deposit.sword.extension.SWORDExtensionFactory;
import org.dataconservancy.deposit.sword.extension.Treatment;
import org.dataconservancy.deposit.sword.server.impl.MockRequestContext;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class SwordCollectionAdaptorTest {
    private static final Abdera abdera = new Abdera();

    @Test
    public void noAcceptedPackagingTest() throws Exception {
        SWORDCollectionAdapter adapter = new TestAdaptor();
        Element collection = getCollectionElement(adapter);
        List<Element> results = getMatchingElements(SWORDExtensionFactory.ACCEPT_PACKAGING,
                                                    collection);
        assertEquals(results.toString(), 0, results.size());
        assertNotNull(adapter.getAcceptedPackaging());
        assertEquals(0, adapter.getAcceptedPackaging().length);
    }

    @Test
    public void oneAcceptedPackagingTest() throws Exception {
        final String PACKAGING = "urn:test:pkg";

        SWORDCollectionAdapter adapter = new TestAdaptor();

        adapter.setAcceptedPackaging(new SWORDPackaging(PACKAGING));

        List<AcceptPackaging> results = getMatchingElements(
            SWORDExtensionFactory.ACCEPT_PACKAGING,
            getCollectionElement(adapter));

        assertEquals(results.toString(), 1, results.size());
        assertEquals(1, adapter.getAcceptedPackaging().length);
        assertTrue(adapter.getAcceptedPackaging().length == 1);

        assertEquals("Serialized output did not contain same packaging",
                     PACKAGING,
                     (results.get(0)).getAcceptedPackaging());
        assertEquals("Bean did not contain same packaging",
                     PACKAGING,
                     (adapter.getAcceptedPackaging()[0].getPackaging()));
    }

    @Test
    public void multipleAcceptedPackagingTest() throws Exception {
        final SWORDPackaging PKG1 = new SWORDPackaging("urn:test:pkg1");
        final SWORDPackaging PKG2 = new SWORDPackaging("urn:test:pkg2");

        SWORDCollectionAdapter adapter = new TestAdaptor();

        adapter.setAcceptedPackaging(PKG1, PKG2);

        List<AcceptPackaging> results = getMatchingElements(
            SWORDExtensionFactory.ACCEPT_PACKAGING,
            getCollectionElement(adapter));

        assertEquals(results.toString(), 2, results.size());
        assertEquals(2, adapter.getAcceptedPackaging().length);

        assertEquals("Serialized output did not contain same packaging",
                     PKG1.getPackaging(),
                     (results.get(0)).getAcceptedPackaging());
        assertEquals("Bean did not contain same packaging",
                     PKG1.getPackaging(),
                     (adapter.getAcceptedPackaging()[0].getPackaging()));
        assertEquals("Serialized output did not contain same packaging",
                     PKG2.getPackaging(),
                     (results.get(1)).getAcceptedPackaging());
        assertEquals("Bean did not contain same packaging",
                     PKG2.getPackaging(),
                     (adapter.getAcceptedPackaging()[1].getPackaging()));
    }

    @Test
    public void noTreatmentTest() throws Exception {
        SWORDCollectionAdapter adapter = new TestAdaptor();
        Element collection = getCollectionElement(adapter);

        assertNull(getMatchingElement(SWORDExtensionFactory.TREATMENT,
                                      collection));
        assertNull(adapter.getTreatment());
    }

    @Test
    public void setTreatmentTest() throws Exception {
        final String TREATMENT = "the treatment";

        SWORDCollectionAdapter adapter = new TestAdaptor();
        adapter.setTreatment(TREATMENT);

        Treatment treatment = (Treatment) getMatchingElement(
            SWORDExtensionFactory.TREATMENT,
            getCollectionElement(adapter));

        assertNotNull(treatment);
        assertNotNull(adapter.getTreatment());
        assertEquals("Serialized treatment is wrong",
                     TREATMENT,
                     treatment.getTreatment());
        assertEquals("Bean did not contain correct treatment",
                     TREATMENT,
                     adapter.getTreatment());

    }

    @Test
    public void defaultAcceptsTest() throws Exception {
        final String DEFAULT = "*/*";
        SWORDCollectionAdapter adapter = new TestAdaptor();
        String[] accepts = adapter.getAccepts(null);

        Element accept = getMatchingElement(Constants.ACCEPT,
                                            getCollectionElement(adapter));
        assertNotNull(accepts);
        assertNotNull(accept);
        assertEquals("Should contain only one accepted mime type",
                     1,
                     accepts.length);
        assertEquals("Default serialized accept value is incorrect",
                     DEFAULT,
                     accept.getText());
        assertEquals("Default bean accept value is incorrect",
                     DEFAULT,
                     accepts[0]);
    }

    @Test
    public void oneAcceptsTest() throws Exception {
        final String ACCEPT = "application/xml";
        SWORDCollectionAdapter adapter = new TestAdaptor();
        adapter.setAccepts(ACCEPT);


        String[] accepts = adapter.getAccepts(null);
        Element accept = getMatchingElement(Constants.ACCEPT,
                                            getCollectionElement(adapter));

        assertEquals("Should contain only one accepted mime type",
                     1,
                     accepts.length);
        assertEquals("Serialized accept MIME is incorrect",
                     ACCEPT,
                     accept.getText());
        assertEquals("Bean accept MIME is incorrect", ACCEPT, accepts[0]);
    }

    @Test
    public void multipleAcceptsTest() throws Exception {
        final String[] ACCEPTS = {"text/1+xml", "text/2+xml"};
        SWORDCollectionAdapter adapter = new TestAdaptor();
        adapter.setAccepts(ACCEPTS);

        String[] accepts = adapter.getAccepts(null);
        List<Element> accept = getMatchingElements(Constants.ACCEPT,
                                                   getCollectionElement(adapter));

        assertEquals("Should contain two accepted mime types",
                     2,
                     accepts.length);

        for (int i = 0; i < 2; i++) {
            assertEquals("Serialized accept MIME is incorrect",
                         ACCEPTS[i],
                         accept.get(i).getText());
            assertEquals("Bean accept MIME is incorrect",
                         ACCEPTS[i],
                         accepts[i]);
        }
    }

    @Test
    public void defaultMediationTest() throws Exception {
        SWORDCollectionAdapter adapter = new TestAdaptor();

        Mediation mediation = getMatchingElement(SWORDExtensionFactory.MEDIATION,
                                                 getCollectionElement(adapter));

        assertNotNull(mediation);
        assertFalse("Serialized default mediation should be false",
                    mediation.getMediation());
        assertFalse("Bean mediation should be false", adapter.getMediation());
    }

    @Test
    public void setMediationTest() throws Exception {
        SWORDCollectionAdapter adapter = new TestAdaptor();
        adapter.setMediation(true);

        Mediation mediation = getMatchingElement(SWORDExtensionFactory.MEDIATION,
                                                 getCollectionElement(adapter));

        assertTrue("Serialized default mediation should be true",
                   mediation.getMediation());
        assertTrue("Bean mediation should be true", adapter.getMediation());

    }

    @Test
    public void nullCollectionPolicyTest() throws Exception {
        SWORDCollectionAdapter adapter = new TestAdaptor();
        CollectionPolicy policy = getMatchingElement(SWORDExtensionFactory.COLLECTION_POLICY,
                                                     getCollectionElement(
                                                         adapter));
        assertNull(adapter.getCollectionPolicy());
        assertNull(policy);
    }

    @Test
    public void setCollectionPolicyTest() throws Exception {
        final String POLICY = "my policy";
        SWORDCollectionAdapter adapter = new TestAdaptor();
        adapter.setCollectionPolicy(POLICY);

        CollectionPolicy policy = getMatchingElement(SWORDExtensionFactory.COLLECTION_POLICY,
                                                     getCollectionElement(
                                                         adapter));
        assertNotNull(policy);
        assertEquals("Serialized policy does not match",
                     POLICY,
                     policy.getPolicy());
        assertEquals("Bean policy does not match",
                     POLICY,
                     adapter.getCollectionPolicy());

    }

    private <T extends Element> T getMatchingElement(QName name,
                                                     Element container) {
        List<T> results = getMatchingElements(name, container);
        if (results.size() == 1) {
            return results.get(0);
        } else if (results.size() > 1) {
            fail("More than one element encountered");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Element> List<T> getMatchingElements(QName name,
                                                            Element container) {
        ArrayList<T> matches = new ArrayList<T>();
        for (Element e : container.getElements()) {
            if (e.getQName().equals(name)) {
                matches.add((T) e);
            }
        }
        return matches;
    }

    /* Serialize and re-constitute app:collection elements and their children */
    private Element getCollectionElement(SWORDCollectionInfo collection)
        throws IOException {
        StringWriter w = new StringWriter();
        collection.asCollectionElement(new MockRequestContext()).writeTo(w);

        StringReader r = new StringReader(w.toString());
        return abdera.getFactory().newParser().parse(r).getRoot();
    }


    private class TestAdaptor extends SWORDCollectionAdapter {
        public ResponseContext headEntry(RequestContext req) {
            return null;
        }

        public ResponseContext getEntry(RequestContext req) {
            return null;
        }

        public ResponseContext postMedia(RequestContext req) {
            return null;
        }
    }
}
