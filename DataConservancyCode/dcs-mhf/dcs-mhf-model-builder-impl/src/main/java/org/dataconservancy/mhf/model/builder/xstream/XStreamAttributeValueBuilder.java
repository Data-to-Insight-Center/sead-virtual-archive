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
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.representation.api.Altitude;
import org.dataconservancy.mhf.representations.DateTimeRange;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.ui.model.PersonName;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * XStream implementation of {@link AttributeValueBuilder}
 */
public class XStreamAttributeValueBuilder implements AttributeValueBuilder {

    private XStream x;

    public XStreamAttributeValueBuilder(XStream x) {
        this.x = x;
    }

    @Override
    public Location buildLocation(InputStream inputStream) {
        return (Location)x.fromXML(inputStream);
    }

    @Override
    public void buildLocation(Location location, OutputStream outputStream) {
        x.toXML(location, outputStream);
    }

    @Override
    public DateTimeRange buildDateTimeRange(InputStream inputStream) {
        return (DateTimeRange)x.fromXML(inputStream);
    }

    @Override
    public void buildDateTimeRange(DateTimeRange dateTimeRange, OutputStream outputStream) {
        x.toXML(dateTimeRange, outputStream);
    }

    @Override
    public Altitude buildAltitude(InputStream inputStream) {
        return (Altitude)x.fromXML(inputStream);
    }

    @Override
    public void buildAltitude(Altitude altitude, OutputStream outputStream) {
        x.toXML(altitude, outputStream);
    }

    @Override
    public PersonName buildPersonName(InputStream inputStream) {
        return (PersonName)x.fromXML(inputStream);
    }

    @Override
    public void buildPersonName(PersonName personName, OutputStream outputStream) {
        x.toXML(personName, outputStream);
    }
}
