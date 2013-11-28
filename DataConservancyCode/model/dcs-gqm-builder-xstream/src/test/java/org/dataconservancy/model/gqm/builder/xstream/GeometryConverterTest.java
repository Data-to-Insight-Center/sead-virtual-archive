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

import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Before;
import org.junit.Test;

import org.xml.sax.SAXException;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Geometry.Type;
import org.dataconservancy.model.gqm.Point;

import static org.dataconservancy.model.gqm.builder.xstream.GeometryConverter.E_GEOMETRY;
import static org.dataconservancy.model.gqm.builder.xstream.GeometryConverter.E_TYPE;
import static org.junit.Assert.assertEquals;

public class GeometryConverterTest extends AbstractGQMXstreamConverterTest {
    
    public static final Type TYPE = Type.POLYGON;
    public static final Point POINT_ONE = new Point( 1.0, 0.0, 4.5 );
    public static final Point POINT_TWO = new Point( 2.3, 5.2, 6.3 );
    
    private static final String XML = "<" + E_GEOMETRY +  " xmlns=\"" + XMLNS +  "\">\n" +
    "      <" + E_TYPE + ">" + TYPE + "</" + E_TYPE + ">\n" +
    "      <point>\n" +
    "        <coordinate>" + POINT_ONE + "</coordinate>\n" +
    "      </point>\n" +
    "      <point>\n" +
    "        <coordinate>" + POINT_TWO + "</coordinate>\n" +
    "      </point>\n" +
    "    </Geometry>";
    
    private Geometry geometry;
    
    @Before
    public void setUp() {
        super.setUp();
        geometry = new Geometry(TYPE, POINT_ONE, POINT_TWO);
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(geometry));        
    }

    @Test
    public void testUnmarshal() {      
        assertEquals("Expected: " + geometry + " Actual: " + x.fromXML(XML), geometry, x.fromXML(XML));
        assertEquals(geometry, x.fromXML(x.toXML(geometry)));
    }
}
