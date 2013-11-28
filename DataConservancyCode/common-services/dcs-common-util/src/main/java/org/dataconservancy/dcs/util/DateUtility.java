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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Utility for parsing and converting dates.
 * <p>
 * Provides means to parse and convert <a
 * href="http://www.iso.org/iso/date_and_time_format">ISO 8601</a> and <a
 * href="http://www.rfc-editor.org/rfc/rfc822.txt">RFC 822</a> formatted dates
 * into miliseconds since the epoch.
 * </p>
 */
public class DateUtility {

    /*
     * Relatively strict formatter for writing ISO 8601 dateTimes with
     * miliseconds.
     */
    private static final DateTimeFormatter iso8601Writer =
            ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    /* Relatively liberal parser for ISO 8601 dateTimes */
    private static final DateTimeFormatter iso8601Parser =
            ISODateTimeFormat.dateTimeParser();

    /*
     * Using java DateFormat instead of Joda Time here, as joda time parser
     * doesn't handle human-readable time zone names.
     */
    private static final DateFormat rfc822 =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    
    private static final DateFormat rfc850 =
            new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z");
    
    private static final DateFormat asc = 
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    static {
        rfc822.setTimeZone(TimeZone.getTimeZone("GMT"));
        rfc850.setTimeZone(TimeZone.getTimeZone("GMT"));
        asc.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
     /**
     * Parse a date string into milliseconds since the epoch.
     * <p>
     * Automatically determines date format in order to locate the correct
     * parser. Currently, ISO 8601 (as in xml date time) and RFC 822 (as in http
     * headers) are supported.
     * </p>
     * 
     * @param date
     *        String containing date in RFC 822 or ISO 8601 format.
     * @return long containing time in milliseconds since the epoch.
     */
    public static long parseDate(String date) {
        long time = -1;
        
        try {
            time = rfc822.parse(date).getTime();
        } catch (ParseException e) {
        }
        
        //If the date is null then it wasn't an rfc822 date
        if (time == -1) {
            try {
                time = rfc850.parse(date).getTime();
            } catch (ParseException e) {
            }
        }
        
        //If the date is still null try asc parsing
        if (time == -1) {
            try {
                time = asc.parse(date).getTime();
            } catch (ParseException e) {
            }
        }
        
        if (time == -1) {
            time = iso8601Parser.parseMillis(date);
        } 
        
        return time;
    }
    
    /**
     * Parses a Rfc822 formatted date string into a DateTime object
     * @param date The string Rfc822 format.
     * @return A DateTime object representing the date in the string, or null if the string isn't properly formatted. 
     */
    public static DateTime parseRfc822Date(String date) {
        DateTime dateTime = null;
        try {
            dateTime = new DateTime(rfc822.parse(date).getTime()).withZone(DateTimeZone.forID("GMT"));
        } catch (ParseException e) {
            System.out.println("Exception" + e.getMessage());
            //The string was not formatted correctly, ensure null is returned
            dateTime = null;
        }
        return dateTime;
    }
    
    /**
     * Parses a Rfc850 formatted date string into a DateTime object
     * @param date The string Rfc850 format.
     * @return A DateTime object representing the date in the string, or null if the string isn't properly formatted. 
     */
    public static DateTime parseRfc850Date(String date) {
        DateTime dateTime = null;
        try {
            dateTime = new DateTime(rfc850.parse(date).getTime()).withZone(DateTimeZone.forID("GMT"));
        } catch (ParseException e) {
            System.out.println("Exception" + e.getMessage());
            //The string was not formatted correctly, ensure null is returned
            dateTime = null;
        }
        return dateTime;
    }
    
    /**
     * Parses an ASC formatted date string into a DateTime object
     * @param date The string Asc format.
     * @return A DateTime object representing the date in the string, or null if the string isn't properly formatted. 
     */
    public static DateTime parseAscTime(String date) {
        DateTime dateTime = null;
        try {
            
            Date parsedDate = asc.parse(date);
            dateTime = new DateTime(parsedDate.getTime()).withZone(DateTimeZone.forID("GMT"));
        } catch (ParseException e) {
            System.out.println("Exception" + e.getMessage());
            //The string was not formatted correctly, ensure null is returned
            dateTime = null;
        }
        return dateTime;
    }

    /**
     * Parses a string into one of the following formats:
     * Rfc822
     * Rfc850
     * Asc
     * ISO8601
     * @param date The formatted string to parse
     * @return A date time object with the formatted string, or null if the string doesn't match any of the formats.
     */
    public static DateTime parseDateString(String date) {
        long millis = parseDate(date);
        return new DateTime(millis).withZone(DateTimeZone.forID("GMT"));
    }
    
    /**
     * Returns a dateTime value into ISO 8601 format.
     * <p>
     * Will return the UTC date to the millisecond precision. e.g.
     * <code>2010-06-22T07:31.005Z</code>
     * </p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toIso8601(long timestamp) {
        return iso8601Writer.print(timestamp);
    }

    /**
     * Returns a dateTime value into ISO 8601 format.
     * <p>
     * Will return the UTC date to the millisecond precision. e.g.
     * <code>2010-06-22T07:31.005Z</code>
     * </p>
     *
     * @param date
     * @return formatted date string.
     */
    public static String toIso8601(Date date) {
        return iso8601Writer.print(new DateTime(date));
    }

    /**
     * Returns a dateTime value into RFC 822 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toRfc822(long timestamp) {
        return rfc822.format(new Date(timestamp));
    }
    
    /**
     * Returns a dateTime value into RFC 850 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toRfc850(long timestamp) {
        return rfc850.format(new Date(timestamp));
    }
    
    /**
     * Returns a dateTime value into Asc format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toAsc(long timestamp) {
        return asc.format(new Date(timestamp));
    }

    /**
     * Returns a dateTime value into RFC 822 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param date object representing the date to format
     * @return formatted date string.
     */
    public static String toRfc822(DateTime date) {
        rfc822.setTimeZone(TimeZone.getTimeZone("GMT"));
        return toRfc822(date.getMillis());
    }
    
    /**
     * Returns a dateTime value into RFC 850 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param date object representing the date to format
     * @return formatted date string.
     */
    public static String toRfc850(DateTime date) {
        rfc850.setTimeZone(TimeZone.getTimeZone("GMT"));

        return toRfc850(date.getMillis());
    }
    
    /**
     * Returns a dateTime value into Asc format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param date object representing the date to format
     * @return formatted date string.
     */
    public static String toAsc(DateTime date) {
        return toAsc(date.getMillis());
    }
    
    /**
     * Returns the current time in milliseconds.
     * 
     * @return long containing time in milliseconds since the epoch.
     */
    public static long now() {
        return new Instant().getMillis();
    }
}
