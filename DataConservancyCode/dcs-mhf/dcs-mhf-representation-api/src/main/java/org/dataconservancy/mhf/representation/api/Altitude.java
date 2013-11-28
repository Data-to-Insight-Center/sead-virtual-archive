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

package org.dataconservancy.mhf.representation.api;

/**
 * Represent a distance measurement between a reference datum and a point within different Earth's spatial reference
 * system/
 */
public interface Altitude {

    public enum UNIT_NAME { METERS, FEET, MILES }
    /** Numeric values of an altitude
     *
     * @return
     */
    public double getValue();

    /**
     * The name of the unit, in which the altitude is expressed
     * @return
     */
    public UNIT_NAME getUnitType();

    /**
     * The reference point against which the altitude's value is measured.
     * @return
     */
    public String getReferenceDatum();
}
