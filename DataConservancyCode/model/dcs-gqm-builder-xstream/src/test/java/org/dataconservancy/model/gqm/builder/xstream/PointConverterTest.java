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
package org.dataconservancy.model.gqm.builder.xstream;

import java.io.IOException;

import java.util.Arrays;

import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Before;
import org.junit.Test;

import org.xml.sax.SAXException;

import org.dataconservancy.model.gqm.Point;

import static org.dataconservancy.model.gqm.builder.xstream.PointConverter.E_COORDINATE;
import static org.dataconservancy.model.gqm.builder.xstream.PointConverter.E_POINT;
import static org.junit.Assert.assertEquals;

public class PointConverterTest extends AbstractGQMXstreamConverterTest {
  
    public static final double[] COORDINATES = { 325, -.462, 436.4223 };
    
    private static final String XML = "<" + E_POINT + " xmlns=\"" + XMLNS +  "\">\n" +
    "        <" + E_COORDINATE + ">" + Arrays.toString(COORDINATES) + "</" + E_COORDINATE + ">\n" +
    "      </point>\n";
    
    private Point point;
    
    @Before
    public void setUp() {
        super.setUp();
        point = new Point(COORDINATES);
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(point));        
    }

    @Test
    public void testUnmarshal() {      
        assertEquals("Expected: " + point + " Actual: " + x.fromXML(XML), point, x.fromXML(XML));
        assertEquals(point, x.fromXML(x.toXML(point)));
    }
}
