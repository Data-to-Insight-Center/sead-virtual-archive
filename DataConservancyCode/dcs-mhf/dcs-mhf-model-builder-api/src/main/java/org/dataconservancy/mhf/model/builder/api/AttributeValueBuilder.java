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
package org.dataconservancy.mhf.model.builder.api;

import org.dataconservancy.mhf.representation.api.Altitude;
import org.dataconservancy.mhf.representations.DateTimeRange;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.ui.model.PersonName;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provide methods to serialize and deserialize {@code Attribute}'s values
 *
 */
public interface AttributeValueBuilder {
    public Location buildLocation(InputStream inputStream);
    public void buildLocation(Location location, OutputStream outputStream);

    public DateTimeRange buildDateTimeRange(InputStream inputStream);
    public void buildDateTimeRange(DateTimeRange dateTimeRange, OutputStream outputStream);

    public Altitude buildAltitude(InputStream inputStream);
    public void buildAltitude(Altitude altitude, OutputStream outputStream);

    public PersonName buildPersonName(InputStream inputStream);
    public void buildPersonName(PersonName personName, OutputStream outputStream);
}
