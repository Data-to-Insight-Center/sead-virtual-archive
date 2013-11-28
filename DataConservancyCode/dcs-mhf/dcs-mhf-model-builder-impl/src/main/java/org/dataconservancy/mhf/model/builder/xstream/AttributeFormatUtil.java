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

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representations.DateTimeRange;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class AttributeFormatUtil {

    // Sample format date time string: 2010-04-01T00:00:00.000-06:00
    private static final DateTimeFormatter isoDateTimeFormatter = ISODateTimeFormat.dateTime();

    private XStreamAttributeValueBuilder attributeValueBuilder;

    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public AttributeFormatUtil() {
        final XStream xstream = new XStream();
        this.attributeValueBuilder = new XStreamAttributeValueBuilder(xstream);
    }

    public DateTime dateTimeAttributeToDateTime(Attribute dateTimeAttribute) throws IllegalArgumentException {
        DateTime dateTime = null;

        try {
            dateTime = isoDateTimeFormatter.parseDateTime(dateTimeAttribute.getValue());
        } catch(IllegalArgumentException except) {
            log.warn("Could not parse GQM date time string: " + except);
            throw except;
        }
        return dateTime;
    }

    public String formatDateTime(DateTime dateTime, String pattern) throws IllegalArgumentException {
        String formattedDateTime = null;

        try {
            DateTimeFormatter previewDateTimeFormatter = DateTimeFormat.forPattern(pattern);
            formattedDateTime = previewDateTimeFormatter.print(dateTime);
        } catch(IllegalArgumentException except) {
            log.warn("Could not parse GQM date time string: " + except);
            throw except;
        }
        return formattedDateTime;
    }

    public String formatDateTimeAttributeForPreview(Attribute dateTimeAttribute) {
        DateTime dateTime = this.dateTimeAttributeToDateTime(dateTimeAttribute);
        return this.formatDateTime(dateTime, "MM/dd/yyyy");
    }

    public List<DateTime> dateTimeRangeAttributeToDateTimes(Attribute dateTimeRangeAttribute) throws UnsupportedEncodingException {
        DateTimeRange dateTimeRange = null;
        List<DateTime> dateTimes = new ArrayList<DateTime>();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(dateTimeRangeAttribute.getValue().getBytes("UTF-8"));
            dateTimeRange = this.attributeValueBuilder.buildDateTimeRange(inputStream);
            dateTimes.add(dateTimeRange.getStartDateTime());
            dateTimes.add(dateTimeRange.getEndDateTime());
        } catch(UnsupportedEncodingException except) {
            log.warn("Couldn't parse GQM date time interval from attribute." + except);
            throw except;
        }
        return dateTimes;
    }

    public List<String> formatDateTimeRangeAttributeForPreview(Attribute dateTimeRangeAttribute) throws UnsupportedEncodingException {
        List<String> formattedDateTimeRange = new ArrayList<String>();
        List<DateTime> rangeTimes = this.dateTimeRangeAttributeToDateTimes(dateTimeRangeAttribute);
        formattedDateTimeRange.add(this.formatDateTime(rangeTimes.get(0), "MM/dd/yyyy"));
        formattedDateTimeRange.add(this.formatDateTime(rangeTimes.get(1), "MM/dd/yyyy"));
        return formattedDateTimeRange;
    }

    public Location locationAttributeToLocation(Attribute locationAttribute) throws UnsupportedEncodingException {
        Location location = null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(locationAttribute.getValue().getBytes("UTF-8"));
            location = this.attributeValueBuilder.buildLocation(inputStream);
        } catch(UnsupportedEncodingException except) {
            log.warn("Couldn't parse GQM location from attribute." + except);
            throw except;
        }
        return location;
    }

    public List<String> formatLocationForPreview(Location location) throws UnsupportedEncodingException {
        List<String> formattedBoundingBoxPoints = new ArrayList<String>();
        Point [] points = location.getGeometry().getPoints();

        formattedBoundingBoxPoints.add(Double.toString(points[0].getCoordinates()[0]));
        formattedBoundingBoxPoints.add(Double.toString(points[2].getCoordinates()[0]));
        formattedBoundingBoxPoints.add(Double.toString(points[2].getCoordinates()[1]));
        formattedBoundingBoxPoints.add(Double.toString(points[0].getCoordinates()[1]));

        return formattedBoundingBoxPoints;
    }

    public List<String> formatLocationAttributeForPreview (Attribute locationAttribute) throws UnsupportedEncodingException {
        Location location = this.locationAttributeToLocation(locationAttribute);
        return this.formatLocationForPreview(location);
    }
}
