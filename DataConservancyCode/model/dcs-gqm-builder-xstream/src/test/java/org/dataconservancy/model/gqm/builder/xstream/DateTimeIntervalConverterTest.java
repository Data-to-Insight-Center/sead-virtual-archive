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

import org.dataconservancy.model.gqm.DateTimeInterval;

import static org.dataconservancy.model.gqm.builder.xstream.DateTimeIntervalConverter.A_END;
import static org.dataconservancy.model.gqm.builder.xstream.DateTimeIntervalConverter.A_START;
import static org.dataconservancy.model.gqm.builder.xstream.DateTimeIntervalConverter.E_DATETIMEINTERVAL;
import static org.junit.Assert.assertEquals;

public class DateTimeIntervalConverterTest extends AbstractGQMXstreamConverterTest {
    private static final long START = 10000;
    private static final long END = 1000000;
    
    private static final String XML = "<" + E_DATETIMEINTERVAL + " xmlns=\"" + XMLNS + "\" " + A_START + "=\"" + START + "\" " + A_END + "=\"" + END + "\">\n" +
    "    </" + E_DATETIMEINTERVAL + ">";
    
    private DateTimeInterval interval;
    
    @Before
    public void setUp(){
        super.setUp();
        
        interval = new DateTimeInterval(START, END);
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(interval));
    }
    
    @Test
    public void testUnmarshal() {
        assertEquals("Expected: " + interval + " Actual: " + x.fromXML(XML), interval, x.fromXML(XML));
        assertEquals(interval, x.fromXML(x.toXML(interval)));
    }
}
