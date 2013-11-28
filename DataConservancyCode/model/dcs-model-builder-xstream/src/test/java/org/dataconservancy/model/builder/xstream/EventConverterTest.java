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

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.junit.Before;
import org.junit.Test;

import static org.dataconservancy.model.builder.xstream.EventConverter.*;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class EventConverterTest extends AbstractXstreamConverterTest {

    private static final String EVENT_ID = "urn:Event:9912";
    private static final String EVENT_TYPE = "INGEST";
    private static final String EVENT_TARGET_1 = "urn:DeliverableUnit:34";
    private static final String EVENT_TARGET_2 = "urn:DeliverableUnit:35";
    private static final String EVENT_DETAIL = "Object xyz was ingested.";
    private static final String EVENT_OUTCOME = "SUCCESS";
    private static final String EVENT_DATE = "2002-10-10T17:00:00Z";

    private static final String XML = "<" + E_EVENT + " xmlns=\"" + XMLNS + "\" id=\"" + EVENT_ID + "\">\n" +
            "      <" + E_TYPE + ">" + EVENT_TYPE + "</" + E_TYPE + ">\n" +
            "      <" + E_DATE + ">" + EVENT_DATE + "</" + E_DATE + ">\n" +
            "      <" + E_TARGET + " ref=\"" + EVENT_TARGET_1 + "\"/>\n" +
            "      <" + E_TARGET + " ref=\"" + EVENT_TARGET_2 + "\"/>\n" +
            "      <" + E_DETAIL + ">" + EVENT_DETAIL + "</" + E_DETAIL + ">\n" +
            "      <" + E_OUTCOME + ">" + EVENT_OUTCOME + "</" + E_OUTCOME + ">\n" +
            "</" + E_EVENT + ">\n";

    private DcsEvent event = new DcsEvent();

    @Before
    public void setUp() {
        super.setUp();
        event.setEventType(EVENT_TYPE);
        event.setDate(EVENT_DATE);
        event.setDetail(EVENT_DETAIL);
        event.setOutcome(EVENT_OUTCOME);
        event.addTargets(new DcsEntityReference(EVENT_TARGET_1));
        event.addTargets(new DcsEntityReference(EVENT_TARGET_2));
        event.setId(EVENT_ID);
    }

    @Test
    public void testMarshal() throws Exception {
        final String actualXml = x.toXML(event);
        final Diff diff = new Diff(XML, actualXml);
        // to allow the Set of targets to be in any order
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + actualXml, diff, true);

        final String roundTripActualXml = x.toXML(x.fromXML(XML));
        final Diff rtDiff = new Diff(XML, roundTripActualXml);
        // to allow the Set of targets to be in any order
        rtDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + roundTripActualXml, rtDiff, true);
    }

    @Test
    public void testUnmarshal() throws Exception {
        final DcsEvent actual = (DcsEvent)x.fromXML(XML);
        assertEquals(event, actual);
        assertEquals(event, x.fromXML(x.toXML(event)));
    }
}
