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
import org.dataconservancy.model.dcs.DcsFixity;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class FixityConverterTest extends AbstractXstreamConverterTest {

    private static final String ALGO = "md5";
    private static final String VALUE = "fe5b3b4f78b9bf3ae21cd52c2f349174";
    private static final String XML = "<fixity xmlns=\"" + XMLNS + "\" algorithm=\"" + ALGO + "\">" + VALUE + "</fixity>";

    private DcsFixity fixity;

    @Before
    public void setUp() {
        super.setUp();
        fixity = new DcsFixity();
        fixity.setAlgorithm(ALGO);
        fixity.setValue(VALUE);
    }

    @Test
    public void testMarshal() throws Exception {
        XMLAssert.assertXMLEqual(XML, x.toXML(fixity));
        XMLAssert.assertXMLEqual(XML, x.toXML(x.fromXML(XML)));
    }

    @Test
    public void testUnmarshal() throws Exception {
        assertEquals(fixity, x.fromXML(XML));
        assertEquals(fixity, x.fromXML(x.toXML(fixity)));
    }
    
    @Test
    public void testMarshallNullValues() throws Exception {
        x.toXML(new DcsFixity());
    }
}
