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
package org.dataconservancy.model.builder.xstream;

import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CollectionConverterTest extends AbstractXstreamConverterTest {

    private static String REF = "http://dataconservancy.org/collections/SDSS_run_5";
    private static String XML_REF = "<collection xmlns=\"" + XMLNS + "\" ref=\"" + REF + "\" />";

    private static String PARENT_REF = "urn:parent-id";

    private final static String AUTHID1 = "aid123";
    private final static String TYPEID1 = "type123";
    private final static String IDVALUE1 = "id123";

    private static String CHILD_ID = "urn:child-id";
    private static String COLLECTION_WITH_PARENT =
        "<Collection xmlns=\"" + XMLNS + "\" id=\"" + CHILD_ID + "\">" +
        "  <" + DeliverableUnitConverter.E_ALTERNATEID + ">" +
        "    <authorityId>" + AUTHID1 + "</authorityId> " +
        "    <typeId>" + TYPEID1 + "</typeId> " +
        "    <idValue>" + IDVALUE1 + "</idValue> " +
        "  </" + CollectionConverter.E_ALTERNATEID + ">" + "\n" +
        "  <parent ref=\"" + PARENT_REF + "\"/>" +
        "</Collection>";

    private DcsCollectionRef collectionRef;
    private DcsResourceIdentifier rid;

    @Before
    public void setUp() {
        super.setUp();
        collectionRef = new DcsCollectionRef();
        collectionRef.setRef(REF);
        rid = new DcsResourceIdentifier();
        rid.setAuthorityId(AUTHID1);
        rid.setTypeId(TYPEID1);
        rid.setIdValue(IDVALUE1);
    }

    @Test
    public void testUnmarshal() {
        assertEquals(collectionRef, x.fromXML(XML_REF));
        assertEquals(collectionRef, x.fromXML(x.toXML(collectionRef)));
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML_REF, x.toXML(collectionRef));
        XMLAssert.assertXMLEqual(XML_REF, x.toXML(x.fromXML(XML_REF)));        
    }

    @Test
    public void testUnmarshalWithParent() {
        final DcsCollection expected = new DcsCollection();
        expected.setId(CHILD_ID);
        expected.setParent(new DcsCollectionRef(PARENT_REF));
        expected.addAlternateId(rid);

        assertEquals(expected, x.fromXML(COLLECTION_WITH_PARENT));
        assertEquals(expected, x.fromXML(x.toXML(expected)));
    }

    @Test
    public void testMarshalWithParent() throws IOException, SAXException {
        final DcsCollection expected = new DcsCollection();
        expected.setId(CHILD_ID);
        expected.setParent(new DcsCollectionRef(PARENT_REF));
        expected.addAlternateId(rid);

        XMLAssert.assertXMLEqual(COLLECTION_WITH_PARENT, x.toXML(expected));
        XMLAssert.assertXMLEqual(COLLECTION_WITH_PARENT, x.toXML(x.fromXML(COLLECTION_WITH_PARENT)));
    }
}
