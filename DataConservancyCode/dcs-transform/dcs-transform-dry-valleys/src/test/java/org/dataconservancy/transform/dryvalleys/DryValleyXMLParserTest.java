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
package org.dataconservancy.transform.dryvalleys;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.dcs.DcsMetadata;



public class DryValleyXMLParserTest {
  
    private final String XML = 
        "<TEST xmlns=\"http://dataconservancy.org/schemas/gqm/1.0\">\n" +
        "    <ValueOne>one</ValueOne>\n" +
        "    <ElementWithChildren>\n" +
        "      <ChildValue>child</ChildValue>\n" +
        "      <NestedElement>\n" +
        "        <nestedValue>nest</nestedValue>\n" +
        "      </NestedElement>\n" +     
        "    </ElementWithChildren>\n" + 
        "    <Latitude>\n" +
        "       <value>10.5</value>\n" +
        "    </Latitude>\n" +
        "    <Longitude>\n" +
        "       <value>90</value>\n" +
        "    </Longitude>\n" +
        "  </TEST>";
       
    private DryValleyXMLParser parser;
    
    @Before
    public void setUp() {
        DcsMetadata md = new DcsMetadata();  
        md.setMetadata(XML);
        parser = new DryValleyXMLParser(md);
    }
    
    @Test
    public void parseXMLTest() {
       
       List<String> elementValue = parser.getValue("ValueOne");
       Assert.assertNotNull(elementValue);
       Assert.assertTrue(elementValue.get(0).equalsIgnoreCase("one"));
       
       elementValue = parser.getValue("ChildValue");
       Assert.assertNotNull(elementValue);
       Assert.assertTrue(elementValue.get(0).equalsIgnoreCase("child"));
       
       elementValue = parser.getValue("nestedValue");
       Assert.assertNotNull(elementValue);
       Assert.assertTrue(elementValue.get(0).equalsIgnoreCase("nest"));
    }
    
    @Test
    public void getRootNameTest() {
      
        String rootName = parser.getRootNodeName();
        Assert.assertTrue(rootName.equalsIgnoreCase("TEST"));
    }
    
    @Test
    public void hasElementTest(){
        Assert.assertTrue(parser.hasElement("ElementWithChildren"));
        Assert.assertTrue(parser.hasElement("ChildValue"));
        Assert.assertFalse(parser.hasElement("foo"));                           
    }
    
    @Test
    public void getChildValueTest(){
        double lat = Double.parseDouble(parser.getChildValue("Latitude").get(0));
        Assert.assertEquals(10.5, lat, 0.0);
        
        double lon = Double.parseDouble(parser.getChildValue("Longitude").get(0));
        Assert.assertEquals(90, lon, 0.0);
    }
}
