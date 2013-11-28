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
package org.dataconservancy.model.dcs;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * See DC-334
 */
public class DC_334 {

    @Test
    public void testMetadataHashcode() throws IOException, SAXException {
        // XML bits are equivalent XML, but not equal Strings
        final String xml1 = "<blah abc='def'></blah>";
        final String xml2 = "<blah abc=\"def\"></blah>";
        XMLAssert.assertXMLEqual(xml1, xml2);
        assertFalse(xml1.equals(xml2));

        // When the XML bits are set on DcsMetadata...
        final DcsMetadata md1 = new DcsMetadata();
        final DcsMetadata md2 = new DcsMetadata();
        md1.setMetadata(xml1);
        md2.setMetadata(xml2);

        // ...the metadata strings are normalized inside DcsMetadata...
        final String xmlNormalized = "<blahabc=def></blah>";

        // ...in order to provide equal hashcodes
        assertEquals(xmlNormalized, md1.getNormalizedMetadata());
        assertEquals(xmlNormalized, md2.getNormalizedMetadata());
        assertEquals(md1.getNormalizedMetadata(), md2.getNormalizedMetadata());

        // ...therefore these objects should be equal to each other and have the same hashcode
        assertEquals(md1.hashCode(), md2.hashCode());
        assertEquals(md1, md2);
    }


    @Test
    public void testNormalization() {
        final DcsMetadata md = new DcsMetadata();

        final String withSingleQuotes = "<foo bar='baz'> hi </foo>";
        final String withDoubleQuotes = "<foo bar=\"baz\"> hi </foo>";
        final String withNewline = "<foo bar='baz'>" +
                " hi " +
                "</foo>";
        final String mix = "<foo bar='baz'>" +
                " hi " +
                "</foo>";

        final String normalized = "<foobar=baz>hi</foo>";

        md.setMetadata(withSingleQuotes);
        assertEquals(normalized, md.getNormalizedMetadata());
        md.setMetadata(withDoubleQuotes);
        assertEquals(normalized, md.getNormalizedMetadata());
        md.setMetadata(withNewline);
        assertEquals(normalized, md.getNormalizedMetadata());
        md.setMetadata(mix);
        assertEquals(normalized, md.getNormalizedMetadata());
    }
}
