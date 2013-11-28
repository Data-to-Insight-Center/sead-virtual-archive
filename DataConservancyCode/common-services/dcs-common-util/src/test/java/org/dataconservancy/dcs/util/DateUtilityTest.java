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
package org.dataconservancy.dcs.util;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static junit.framework.Assert.assertEquals;

public class DateUtilityTest {

    @Test
    public void roundTripIso8601Test() {
        long time = new Date().getTime();
        String iso = DateUtility.toIso8601(time);
        assertEquals(time, DateUtility.parseDate(iso));
    }

    @Test
    public void varyingPrecisionIso8601Test() {
        assertEquals(0, DateUtility.parseDate("1970-01-01T00:00:00.000Z"));
        assertEquals(10, DateUtility.parseDate("1970-01-01T00:00:00.010Z"));
        assertEquals(10, DateUtility.parseDate("1970-01-01T00:00:00.01Z"));
        assertEquals(100, DateUtility.parseDate("1970-01-01T00:00:00.1Z"));
        assertEquals(1000, DateUtility.parseDate("1970-01-01T00:00:01.0Z"));
        assertEquals(1000, DateUtility.parseDate("1970-01-01T00:00:01Z"));
    }

    @Test
    public void defaultTimeZoneTest() {
        assertTrue(DateUtility.toIso8601(0).endsWith("Z"));
        assertTrue(DateUtility.toRfc822(0).endsWith("GMT"));
    }

    @Test
    public void roundTripRfc822Test() {

        long time = new Date().getTime();
        String rfc = DateUtility.toRfc822(time);
        long truncated = (time / 1000) * 1000;

        assertEquals(truncated, DateUtility.parseDate(rfc));
    }

    @Test
    public void timeZoneRfc822Test() {
        assertEquals(0, DateUtility.parseDate("Thu, 1 Jan 1970 00:00:00 GMT"));
        assertEquals(0, DateUtility.parseDate("Thu, 1 Jan 1970 00:00:00 UTC"));
        assertEquals(0, DateUtility.parseDate("Thu, 1 Jan 1970 00:00:00 +0000"));
        assertEquals((4 * 60 * 60 * 1000), DateUtility
                .parseDate("Thu, 1 Jan 1970 00:00:00 EDT"));
        assertEquals((4 * 60 * 60 * 1000), DateUtility
                .parseDate("Thu, 1 Jan 1970 00:00:00 -0400"));
    }

    @Test
    public void leadingZerosRfc822Test() {
        assertEquals(DateUtility.parseDate("Thu, 7 Jan 1943 00:00:00 EDT"),
                     DateUtility.parseDate("Thu, 07 Jan 1943 00:00:00 EDT"));
    }
    
    @Test
    public void testFormatRfc822() {
        DateTime testDate = new DateTime(2012, 3, 24, 16, 04, 55, 0, DateTimeZone.forID("GMT"));
        String expectedDateString = "Sat, 24 Mar 2012 16:04:55 GMT";
        String resultString = DateUtility.toRfc822(testDate);
        assertTrue(expectedDateString.equalsIgnoreCase(resultString));
        
        DateTime resultDate = DateUtility.parseRfc822Date(expectedDateString);
        assertNotNull(resultDate);
        
        assertEquals(testDate, resultDate);
    }
    
    @Test
    public void testFormatRfc850() {
        DateTime testDate = new DateTime(2012, 3, 24, 16, 04, 55, 0, DateTimeZone.forID("GMT"));
        String expectedDateString = "Saturday, 24-Mar-12 16:04:55 GMT";
        String resultString = DateUtility.toRfc850(testDate);
        assertTrue(expectedDateString.equalsIgnoreCase(resultString));
        
        DateTime resultDate = DateUtility.parseRfc850Date(expectedDateString);
        assertNotNull(resultDate);
        
        assertEquals(testDate, resultDate);
    }
    
    @Test
    public void testFormatAsc() {
        DateTime testDate = new DateTime(2012, 3, 24, 16, 04, 55, 0, DateTimeZone.forID("GMT"));
        String expectedDateString = "Sat Mar 24 16:04:55 2012";
        String resultString = DateUtility.toAsc(testDate);
        assertTrue(expectedDateString.equalsIgnoreCase(resultString));
        
        DateTime resultDate = DateUtility.parseAscTime(expectedDateString);
        assertNotNull(resultDate);
        
        assertEquals(testDate, resultDate);
    }
    
    @Test
    public void testParseDateString() {
        String rfc822String = "Mon, 26 Mar 2012 02:04:55 GMT";
        DateTime expectedRfc822Date = new DateTime(2012, 3, 26, 02, 04, 55, 0, DateTimeZone.forID("GMT"));
        
        String rfc850String = "Sunday, 25-Mar-12 20:54:55 GMT";
        DateTime expectedRfc850Date = new DateTime(2012, 3, 25, 20, 54, 55, 0, DateTimeZone.forID("GMT"));

        String ascString = "Tue Mar 27 12:59:55 2012";
        DateTime expectedAscDate = new DateTime(2012, 3, 27, 12, 59, 55, 0, DateTimeZone.forID("GMT"));

        DateTime rfc822Date = DateUtility.parseDateString(rfc822String);
        assertEquals(expectedRfc822Date, rfc822Date);
        
        DateTime rfc850Date = DateUtility.parseDateString(rfc850String);
        assertEquals(expectedRfc850Date, rfc850Date);
        
        DateTime ascDate = DateUtility.parseDateString(ascString);
        assertEquals(expectedAscDate, ascDate);
    }

    /**
     * Ensure the toIso8601(...) methods produce the same string for a java.util.Date and a long.
     */
    @Test
    public void dateAndMillisIso8601Test() {
        final Calendar now = Calendar.getInstance();
        assertEquals(DateUtility.toIso8601(now.getTime()), DateUtility.toIso8601(now.getTimeInMillis()));
    }
}
