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
package org.dataconservancy.mhf.representations;

import org.dataconservancy.mhf.representation.api.Altitude;

/**
 * Simple implementation of {@link org.dataconservancy.mhf.representation.api.Altitude} interface.
 * <p/>
 * {@code unitType} of this implementation is the International System of Units (SI) Meter.
 * <p/>
 * {@code referenceDatum} of this implementation is the mean sea level.
 * <p/>
 * An instance of altitude of 0 value indicate the mean sea level altitude. Points above the mean sea level have positive
 * values, where as points below sea level have negative values.
 */
//TODO: if needed method to compare altitude could be added
public class SimpleAltitudeImpl implements Altitude {

    private double value;
    private static final UNIT_NAME unitType = UNIT_NAME.METERS;
    private static final String referenceDatum = "Sea Level";

    public SimpleAltitudeImpl(double value) {
        this.value = value;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public UNIT_NAME getUnitType() {
        return this.unitType;
    }

    @Override
    public String getReferenceDatum() {
        return this.referenceDatum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleAltitudeImpl)) return false;

        SimpleAltitudeImpl that = (SimpleAltitudeImpl) o;

        if (Double.compare(that.value, value) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        long temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }
}
