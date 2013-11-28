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
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MetadataRefConverterTest extends AbstractXstreamConverterTest {

    private static final String REF = "urn:sdss:12345/metadata";
    private static final String XML = "<metadata xmlns=\"" + XMLNS + "\" ref=\"" + REF + "\" />";

    private DcsMetadataRef mdRef;

    @Before
    public void setUp() {
        super.setUp();
        mdRef = new DcsMetadataRef();
        mdRef.setRef(REF);
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(mdRef));
        XMLAssert.assertXMLEqual(XML, x.toXML(x.fromXML(XML)));
    }

    @Test
    public void testUnmarshal() {
        assertEquals(mdRef, x.fromXML(XML));
        assertEquals(mdRef, x.fromXML(x.toXML(mdRef)));
    }


}
