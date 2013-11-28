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
package org.dataconservancy.model.gqm;

import java.net.URI;

/**
 * A location in some spatial system is specified by a Geometry and an
 * identifier for that spatial system.
 */
public class Location {
    private Geometry geometry;
    private URI srid;

    public Location(Geometry geometry, URI srid) {
        this.geometry = geometry;
        this.srid = srid;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public URI getSrid() {
        return srid;
    }

    public void setSrid(URI srid) {
        this.srid = srid;
    }

    public int hashCode() {
        return geometry == null ? 0 : geometry.hashCode();
    }

    public boolean equals(Object o) {
        Location l = (Location) o;

        if (l == null) {
            return false;
        }

        return Util.equals(geometry, l.geometry) && Util.equals(srid, l.srid);
    }

    public String toString() {
        return (srid == null ? "" : srid) + " "
                + (geometry == null ? "" : geometry.toString());
    }
}
