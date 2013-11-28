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

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Before;
import org.junit.Test;

import org.xml.sax.SAXException;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Geometry.Type;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;

import static org.dataconservancy.model.gqm.builder.xstream.GqmConverter.E_DATETIMEINTERVALS;
import static org.dataconservancy.model.gqm.builder.xstream.GqmConverter.E_ENTITYID;
import static org.dataconservancy.model.gqm.builder.xstream.GqmConverter.E_GQM;
import static org.dataconservancy.model.gqm.builder.xstream.GqmConverter.E_LOCATIONS;
import static org.dataconservancy.model.gqm.builder.xstream.GqmConverter.E_RELATIONS;
import static org.junit.Assert.assertEquals;

public class GQMConverterTest extends AbstractGQMXstreamConverterTest{
    private static final Type TYPE = Type.LINE;
    private static final Point POINT_ONE = new Point( 658.658, 13562.76, 13524.68 );
    private static final Point POINT_TWO = new Point( -75347, -2565.785, 0 );
    private static final Point POINT_THREE = new Point( 3282.828, -238, 829.9);
    private static final Point POINT_FOUR = new Point( .88, .238, .99);
    private static final String OBJECT_ONE = "object_one";
    private static final String OBJECT_TWO = "object_two";
    
    private static final long START_ONE = 3250l;
    private static final long START_TWO = 4259320230l;
    private static final long END_ONE = 000000000000;
    private static final long END_TWO = 333333333333l;
    
    private static final String ENTITY_ID = "id";
    
    private URI predicate;
    private URI srid;
    
    private String xml;
    
    private GQM gqm;
    private Geometry geometryOne;
    private Geometry geometryTwo;
    private Location locationOne;
    private Location locationTwo;
    private DateTimeInterval interval;
    private DateTimeInterval intervalTwo;
    private Relation relationOne;
    private Relation relationTwo;
    
    @Before
    public void setUp() {
        super.setUp();
        
        try {
            predicate = new URI("predicate");
            srid = new URI("wgs84");
        } catch (URISyntaxException e) {}
        
        xml = "<" + E_GQM + " xmlns=\"" + XMLNS +  "\">\n" +
        "       <" + E_ENTITYID + ">" + ENTITY_ID + "</" + E_ENTITYID + ">\n" +
        "       <" + E_RELATIONS + ">\n" +
        "           <Relation predicate=\"" + predicate + "\" object=\"" + OBJECT_ONE + "\"/>\n" +
        "           <Relation predicate=\"" + predicate + "\" object=\"" + OBJECT_TWO + "\"/>\n" +
        "       </" + E_RELATIONS + ">\n" +
        "       <" + E_LOCATIONS + ">\n" +
        "           <Location>\n" +
        "               <srid>" + srid + "</srid>\n" +
        "               <Geometry>\n" +
        "                   <type>" + TYPE + "</type>\n" +
        "                   <point>\n" +
        "                       <coordinate>" + POINT_ONE + "</coordinate>\n" +
        "                   </point>\n" +
        "                   <point>\n" +
        "                       <coordinate>" + POINT_TWO + "</coordinate>\n" +
        "                   </point>\n" +  
        "               </Geometry>\n" + 
        "           </Location>\n" +
        "           <Location>\n" +
        "               <srid>" + srid + "</srid>\n" +
        "               <Geometry>\n" +
        "                   <type>" + TYPE + "</type>\n" +
        "                   <point>\n" +
        "                       <coordinate>" + POINT_THREE + "</coordinate>\n" +
        "                   </point>\n" +
        "                   <point>\n" +
        "                       <coordinate>" + POINT_FOUR + "</coordinate>\n" +
        "                   </point>\n" +  
        "               </Geometry>\n" + 
        "           </Location>\n" +
        "       </" + E_LOCATIONS + ">\n" +
        "       <" + E_DATETIMEINTERVALS + ">\n" +
        "           <DateTimeInterval start=\"" + START_ONE + "\" end=\"" + END_ONE + "\"/>\n" +
        "           <DateTimeInterval start=\"" + START_TWO + "\" end=\"" + END_TWO + "\"/>\n" +
        "       </" + E_DATETIMEINTERVALS + ">\n" +
        "    </" + E_GQM + ">";
        
        gqm = new GQM(ENTITY_ID);
        
        geometryOne = new Geometry(TYPE, POINT_ONE, POINT_TWO);
        geometryTwo = new Geometry(TYPE, POINT_THREE, POINT_FOUR);
        
        locationOne = new Location(geometryOne, srid);
        locationTwo = new Location(geometryTwo, srid);
        gqm.getLocations().add(locationOne);
        gqm.getLocations().add(locationTwo);
        
        relationOne = new Relation(predicate, OBJECT_ONE);
        relationTwo = new Relation(predicate, OBJECT_TWO);
        gqm.getRelations().add(relationOne);
        gqm.getRelations().add(relationTwo);
        
        interval = new DateTimeInterval(START_ONE, END_ONE);
        intervalTwo = new DateTimeInterval(START_TWO, END_TWO);
        gqm.getIntervals().add(interval);
        gqm.getIntervals().add(intervalTwo);
        
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        Diff diff = new Diff(xml, x.toXML(gqm));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + xml + " Actual: " + x.toXML(gqm), diff, true);

        diff = new Diff(xml, x.toXML(x.fromXML(xml)));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + xml + " Actual: " + x.toXML(x.fromXML(xml)), diff, true);
    }

    @Test
    public void testUnmarshal() {
        assertEquals(gqm, x.fromXML(xml));
    }
}
