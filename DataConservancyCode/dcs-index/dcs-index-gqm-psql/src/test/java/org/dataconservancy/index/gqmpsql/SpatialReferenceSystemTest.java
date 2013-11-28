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
package org.dataconservancy.index.gqmpsql;

import java.net.URI;

import junit.framework.TestCase;

public class SpatialReferenceSystemTest extends TestCase {

    public void testEquatorial() {
        URI uri = SpatialReferenceSystem.forEquatorialCoordinateSystem("J2000");
        SpatialReferenceSystem srs = SpatialReferenceSystem.lookup(uri);

        assertTrue(srs.isCelestialCoordinateSystem());
        assertTrue(srs.isEquatorialCoordinateSystem());
        assertFalse(srs.isGeospatialCoordinateSystem());
        assertEquals("J2000", srs.epoch());

        srs = SpatialReferenceSystem.lookup(URI.create("ECS:2000"));

        assertTrue(srs.isCelestialCoordinateSystem());
        assertTrue(srs.isEquatorialCoordinateSystem());
        assertFalse(srs.isGeospatialCoordinateSystem());
        assertEquals(null, srs.epoch());
    }

    public void testGeospatial() {
        URI uri = SpatialReferenceSystem.forEPSG(4326);

        SpatialReferenceSystem srs = SpatialReferenceSystem.lookup(uri);

        assertFalse(srs.isCelestialCoordinateSystem());
        assertFalse(srs.isEquatorialCoordinateSystem());
        assertTrue(srs.isGeospatialCoordinateSystem());
        assertEquals("EPSG", srs.authorityName());
        assertEquals(4326, srs.authorityId());

        srs = SpatialReferenceSystem.lookup(URI.create("EPSG:4326"));

        assertFalse(srs.isCelestialCoordinateSystem());
        assertFalse(srs.isEquatorialCoordinateSystem());
        assertTrue(srs.isGeospatialCoordinateSystem());
        assertEquals("EPSG", srs.authorityName());
        assertEquals(4326, srs.authorityId());
    }
}
