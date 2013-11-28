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

import org.dataconservancy.model.gqm.Relation;

import static org.dataconservancy.model.gqm.builder.xstream.RelationConverter.A_OBJECT;
import static org.dataconservancy.model.gqm.builder.xstream.RelationConverter.A_PREDICATE;
import static org.dataconservancy.model.gqm.builder.xstream.RelationConverter.E_RELATION;
import static org.junit.Assert.assertEquals;

public class RelationConverterTest extends AbstractGQMXstreamConverterTest {
    
    private static final String OBJECT = "object";
    private URI predicate;
    
    private String xml;
    
    private Relation relation;
    
    @Before
    public void setUp() {
        super.setUp();
        
        try {
            predicate = new URI("predicate");
        } catch (URISyntaxException e) {
        }
        xml = "<" + E_RELATION + " xmlns=\"" + XMLNS + "\" " + A_PREDICATE + "=\"" + predicate + "\" " + A_OBJECT + "=\"" + OBJECT + "\">\n" +
        "  </" + E_RELATION + ">"; 
        relation = new Relation(predicate, OBJECT);
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(xml, x.toXML(relation));        
    }

    @Test
    public void testUnmarshal() {      
        assertEquals("Expected: " + relation + " Actual: " + x.fromXML(xml), relation, x.fromXML(xml));
        assertEquals(relation, x.fromXML(x.toXML(relation)));
    }
}
