/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.mhf.model.builder.xstream;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.junit.Test;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AttributeFormatUtilTest {

    private AttributeFormatUtil attributeFormatUtil = new AttributeFormatUtil();

    private String simpleStringDateTime = "2010-04-01T00:00:00.000-06:00";
    private Attribute simpleDateTimeAttribute;

    private String xmlStringDateTimeRange;
    private Attribute xmlDateTimeRangeAttribute;

    private String xmlStringLocation;
    private Attribute xmlLocationAttribute;

    @Before
    public void setup() {
        this.simpleDateTimeAttribute = new MetadataAttribute("dateTimeAttribute", "DateTime", this.simpleStringDateTime);

        this.xmlStringDateTimeRange =
                "<org.dataconservancy.mhf.representations.DateTimeRange>\n" +
                        "  <startDateTime>\n" +
                        "    <iMillis>725871600000</iMillis>\n" +
                        "    <iChronology class=\"org.joda.time.chrono.ISOChronology\" resolves-to=\"org.joda.time.chrono.ISOChronology$Stub\" serialization=\"custom\">\n" +
                        "      <org.joda.time.chrono.ISOChronology_-Stub>\n" +
                        "        <org.joda.time.tz.CachedDateTimeZone resolves-to=\"org.joda.time.DateTimeZone$Stub\" serialization=\"custom\">\n" +
                        "          <org.joda.time.DateTimeZone_-Stub>\n" +
                        "            <string>America/Denver</string>\n" +
                        "          </org.joda.time.DateTimeZone_-Stub>\n" +
                        "        </org.joda.time.tz.CachedDateTimeZone>\n" +
                        "      </org.joda.time.chrono.ISOChronology_-Stub>\n" +
                        "    </iChronology>\n" +
                        "  </startDateTime>\n" +
                        "  <endDateTime>\n" +
                        "    <iMillis>1238565600000</iMillis>\n" +
                        "    <iChronology class=\"org.joda.time.chrono.ISOChronology\" reference=\"../../startDateTime/iChronology\"/>\n" +
                        "  </endDateTime>\n" +
                        "</org.dataconservancy.mhf.representations.DateTimeRange>";
        this.xmlDateTimeRangeAttribute = new MetadataAttribute("DateTimeRangeAttribute", "DateTimeRange", this.xmlStringDateTimeRange);

        this.xmlStringLocation =
                "<org.dataconservancy.model.gqm.Location>\n" +
                        "  <geometry>\n" +
                        "    <points>\n" +
                        "      <org.dataconservancy.model.gqm.Point>\n" +
                        "        <coordinates>\n" +
                        "          <double>-80.22399</double>\n" +
                        "          <double>55.56779</double>\n" +
                        "        </coordinates>\n" +
                        "      </org.dataconservancy.model.gqm.Point>\n" +
                        "      <org.dataconservancy.model.gqm.Point>\n" +
                        "        <coordinates>\n" +
                        "          <double>-78.78479</double>\n" +
                        "          <double>55.56779</double>\n" +
                        "        </coordinates>\n" +
                        "      </org.dataconservancy.model.gqm.Point>\n" +
                        "      <org.dataconservancy.model.gqm.Point>\n" +
                        "        <coordinates>\n" +
                        "          <double>-78.78479</double>\n" +
                        "          <double>56.93479</double>\n" +
                        "        </coordinates>\n" +
                        "      </org.dataconservancy.model.gqm.Point>\n" +
                        "      <org.dataconservancy.model.gqm.Point>\n" +
                        "        <coordinates>\n" +
                        "          <double>-80.22399</double>\n" +
                        "          <double>56.93479</double>\n" +
                        "        </coordinates>\n" +
                        "      </org.dataconservancy.model.gqm.Point>\n" +
                        "    </points>\n" +
                        "    <type>POLYGON</type>\n" +
                        "  </geometry>\n" +
                        "</org.dataconservancy.model.gqm.Location>";
        this.xmlLocationAttribute = new MetadataAttribute("LocationAttribute", "Location", this.xmlStringLocation);
    }

    @Test
    public void testDateTimeAttributeToDateTimeWithValidValue() {
        DateTime expectedDateTime = new DateTime(2010, 4, 1, 0, 0);

        DateTime actualDateTime = this.attributeFormatUtil.dateTimeAttributeToDateTime(this.simpleDateTimeAttribute);
        assertEquals(expectedDateTime.toLocalDate(), actualDateTime.toLocalDate());
    }

    @Test
    public void testFormatDateTimeForPreviewWithValidDateTime() {
        DateTime inputDateTime = new DateTime(2010, 4, 1, 3, 12, 16);
        String expected = "04/01/2010";

        String actual = this.attributeFormatUtil.formatDateTime(inputDateTime, "MM/dd/yyyy");

        assertEquals(expected, actual);
    }

    @Test
    public void testFormatDateTimeAttributeForPreviewWithValidAttributeValue() {
        String expected = "04/01/2010";

        String actual = this.attributeFormatUtil.formatDateTimeAttributeForPreview(this.simpleDateTimeAttribute);

        assertEquals(expected, actual);
    }

    @Test
    public void testDateTimeRangeAttributeToDateTimesWithXmlValue() throws UnsupportedEncodingException {
        DateTimeZone dateTimeZone = DateTimeZone.forID("America/Denver");
        DateTime expectedStart = new DateTime(1993, 1, 1, 0, 0, dateTimeZone);
        DateTime expectedEnd = new DateTime(2009, 4, 1, 0, 0, dateTimeZone);
        List<DateTime> actual = this.attributeFormatUtil.dateTimeRangeAttributeToDateTimes(this.xmlDateTimeRangeAttribute);

        assertEquals(expectedStart, actual.get(0));
        assertEquals(expectedEnd, actual.get(1));
    }

    @Test
    public void testFormatDateTimeRangeAttributeForPreviewWithValidAttributeValue() throws UnsupportedEncodingException {
        List<String> expected = new ArrayList<String>();
        expected.add("01/01/1993");
        expected.add("04/01/2009");

        List<String> actual = this.attributeFormatUtil.formatDateTimeRangeAttributeForPreview(this.xmlDateTimeRangeAttribute);

        assertEquals(expected, actual);
    }

    @Test
    public void testLocationAttributeToLocationWithXmlValue() throws UnsupportedEncodingException {
        Geometry geometry = new Geometry(Geometry.Type.POLYGON, new Point(-80.22399, 55.56779),
                new Point(-78.78479, 55.56779), new Point(-78.78479, 56.93479), new Point(-80.22399, 56.93479));
        Location expected = new Location(geometry, null);

        Location actual = this.attributeFormatUtil.locationAttributeToLocation(this.xmlLocationAttribute);

        assertEquals(expected, actual);
    }

    @Test
    public void testFormatLocationAttributeWithXmlValue() throws UnsupportedEncodingException {
        Geometry geometry = new Geometry(Geometry.Type.POLYGON, new Point(-80.22399, 55.56779),
                new Point(-78.78479, 55.56779), new Point(-78.78479, 56.93479), new Point(-80.22399, 55.56779));
        Location inputLocation = new Location(geometry, null);

        List<String> expected = new ArrayList<String>();
        expected.add("-80.22399");
        expected.add("-78.78479");
        expected.add("56.93479");
        expected.add("55.56779");

        List<String> actual = this.attributeFormatUtil.formatLocationForPreview(inputLocation);

        assertEquals(expected, actual);
    }
}
