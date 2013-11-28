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

import java.net.URI;
import java.net.URISyntaxException;

import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Before;
import org.junit.Test;

import org.xml.sax.SAXException;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Geometry.Type;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;

import static org.dataconservancy.model.gqm.builder.xstream.LocationConverter.E_LOCATION;
import static org.dataconservancy.model.gqm.builder.xstream.LocationConverter.E_SRID;
import static org.junit.Assert.assertEquals;

public class LocationConverterTest extends AbstractGQMXstreamConverterTest {
    
    public static final Type TYPE = Type.POINT;
    public static final Point POINT = new Point( 42.0, -26.0, 1689 );
    private URI srid;
    private String xml;
    
    private Geometry geometry;
    private Location location;
    
    @Before
    public void setUp() {
        super.setUp(); 
        
        try {
            srid = new URI("WGS84");
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        xml = "<" + E_LOCATION + " xmlns=\"" + XMLNS +  "\">\n" +
        "    <" + E_SRID + ">" + srid + "</" + E_SRID + ">\n" +
        "    <Geometry>\n" +
        "      <type>" + TYPE + "</type>\n" +
        "      <point>\n" +
        "        <coordinate>" + POINT + "</coordinate>\n" +
        "      </point>\n" +     
        "    </Geometry>\n" + 
        "  </Location>";
        geometry = new Geometry(TYPE, POINT);
        location = new Location(geometry, srid);
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(xml, x.toXML(location));        
    }

    @Test
    public void testUnmarshal() {      
        assertEquals("Expected: " + location + " Actual: " + x.fromXML(xml), location, x.fromXML(xml));
        assertEquals(location, x.fromXML(x.toXML(location)));
    }
}
