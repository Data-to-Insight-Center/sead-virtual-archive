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

/**
 * Represents supported spatial reference systems. They can be encoded as a URI
 * and decoded from a URI.
 * 
 * The Equatorial Coordinate System (a particular Celestial Coordinate System)
 * and OGC geospatial coordinate systems (such as EPSG) are supported
 */
public class SpatialReferenceSystem {
    // TODO choose a better URI
    private static final String EQUATORIAL_COORDINATE_SYSTEM_URI = "http://dataconservancy.org/EquatorialCoordinateSystem";
    private static final String EQUATORIAL_COORDINATE_SYSTEM_ABBREV_URI = "ECS";
    private static final String SPATIAL_REF_ORG_URI = "http://spatialreference.org/ref/";

    public static URI forEPSG(int epsg_id) {
        return URI.create(SPATIAL_REF_ORG_URI + "epsg/" + epsg_id);
    }

    public static URI forEquatorialCoordinateSystem(String epoch) {
        return URI.create(EQUATORIAL_COORDINATE_SYSTEM_URI + "#" + epoch);
    }

    /**
     * @param uri
     * @return The spatial reference system identified by a uri.
     * @throws IllegalArgumentException
     *             if the URI does not match one of several supported templates
     */
    // TODO Use a checked exception?

    public static SpatialReferenceSystem lookup(URI uri) {
        if (uri.toString().startsWith(EQUATORIAL_COORDINATE_SYSTEM_URI)) {
            String epoch = uri.getFragment();
            return new SpatialReferenceSystem(Type.CELESTIAL, epoch, null, -1);
        } else if (uri.toString().startsWith(
                EQUATORIAL_COORDINATE_SYSTEM_ABBREV_URI)) {
            String epoch = uri.getPath();
            return new SpatialReferenceSystem(Type.CELESTIAL, epoch, null, -1);
        } else if (uri.toString().startsWith(SPATIAL_REF_ORG_URI)) {
            String path = uri.getPath();

            if (path == null) {
                throw new IllegalArgumentException("No authority " + uri);
            }

            String[] path_parts = path.split("/");

            if (path_parts.length != 4) {
                throw new IllegalArgumentException("Unable to get authority "
                        + uri);
            }

            String auth = path_parts[2].toUpperCase();
            int auth_id;

            try {
                auth_id = Integer.parseInt(path_parts[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid authority id: "
                        + uri);
            }

            return new SpatialReferenceSystem(Type.GEOSPATIAL, null, auth,
                    auth_id);
        } else {
            // Assume geospatial reference system encoded as AUTHORITY:ID.
            String auth = uri.getScheme();

            int auth_id = -1;
            String s = uri.getSchemeSpecificPart();

            if (s == null) {
                throw new IllegalArgumentException("No auth id: " + uri);
            }

            try {
                auth_id = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid auth id: " + uri);
            }

            return new SpatialReferenceSystem(Type.GEOSPATIAL, null, auth,
                    auth_id);
        }
    }

    private enum Type {
        CELESTIAL, GEOSPATIAL;
    }

    private final Type type;
    private final String epoch;
    private final String auth_name;
    private final int auth_id;

    private SpatialReferenceSystem(Type type, String epoch, String auth_name,
            int auth_id) {
        this.type = type;
        this.epoch = epoch;
        this.auth_name = auth_name;
        this.auth_id = auth_id;
    }

    public boolean isCelestialCoordinateSystem() {
        return type == Type.CELESTIAL;
    }

    public boolean isEquatorialCoordinateSystem() {
        return type == Type.CELESTIAL;
    }

    public boolean isGeospatialCoordinateSystem() {
        return type == Type.GEOSPATIAL;
    }

    /**
     * Only supported for Geospatial Coordinate Systems
     * 
     * @return
     */
    public String authorityName() {
        return auth_name;
    }

    /**
     * Only supported for Geospatial Coordinate Systems
     * 
     * @return
     */
    public int authorityId() {
        return auth_id;
    }

    /**
     * Only supported for Equatorial Coordinate System.
     * 
     * @return epoch or null if not known
     */
    public String epoch() {
        return epoch;
    }
}
