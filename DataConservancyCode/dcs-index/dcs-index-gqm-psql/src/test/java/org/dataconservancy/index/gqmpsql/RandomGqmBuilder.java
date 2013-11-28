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
import java.util.Random;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;

public class RandomGqmBuilder {
    private final Random rand;
    private int next_id;

    public RandomGqmBuilder() {
        this.rand = new Random();
        this.next_id = 0;
    }

    public RandomGqmBuilder(long seed) {
        this.rand = new Random(seed);
        this.next_id = 0;
    }

    public GQM createGqm() {
        GQM result = new GQM(next_entity_id());

        int num_locations = rand.nextInt(4);

        for (int i = 0; i < num_locations; i++) {
            result.getLocations().add(createLocation());
        }

        int num_intervals = rand.nextInt(4);

        for (int i = 0; i < num_intervals; i++) {
            DateTimeInterval dti = new DateTimeInterval(rand.nextLong(),
                    rand.nextLong());
            result.getIntervals().add(dti);
        }

        int num_relations = rand.nextInt(4);

        for (int i = 0; i < num_relations; i++) {
            Relation rel = new Relation(random_uri(), random_string(4));
            result.getRelations().add(rel);
        }

        return result;
    }

    private Location createLocation() {
        int[] epsg_ids = new int[] { 3000, 4326, 3500, 4300 };
        int epsg_id = epsg_ids[rand.nextInt(epsg_ids.length)];

        if (rand.nextBoolean()) {
            return new Location(createGeometry(),
                    SpatialReferenceSystem.forEPSG(epsg_id));
        } else {
            return new Location(createGeometry(),
                    SpatialReferenceSystem.forEquatorialCoordinateSystem("J2000"));
        }
    }

    private Geometry createGeometry() {
        Geometry.Type type = Geometry.Type.values()[rand.nextInt(Geometry.Type
                .values().length)];

        return new Geometry(type, createPoints(type));
    }

    private Point createPoint(int dim) {
        double[] coords = new double[dim];

        for (int i = 0; i < coords.length; i++) {
            coords[i] = rand.nextDouble() * 20;
        }

        return new Point(coords);
    }

    // Make sure x points are not negative
    private Point[] createSimplePolygon(int points) {
        Point[] result = new Point[points];

        double center_x = 20.0 + rand.nextDouble() * 5;
        double center_y = rand.nextDouble() * 20;

        double angle_increment = 2 * Math.PI / points;
        double angle = 0.0;
        
        for (int i = 0; i < points; i++) {
            double radius = 1.0 + rand.nextDouble() * 15;

            double point_x = center_x + (radius * Math.cos(angle));
            double point_y = center_y + (radius * Math.sin(angle));

            result[i] = new Point(point_x, point_y);
            angle += angle_increment;
        }

        return result;
    }

    private Point[] createLineString(int segments) {
        Point[] result = new Point[segments + 1];

        result[0] = createPoint(2);

        // always move in 0 to pi direction to prevent intersection
        for (int i = 1; i < result.length; i++) {
            double angle = rand.nextDouble() * Math.PI;
            double len = 1.0 + rand.nextDouble() * 5;

            double[] last = result[i - 1].getCoordinates();
            double x = last[0] + len * Math.cos(angle);
            double y = last[1] + len * Math.sin(angle);

            result[i] = new Point(x, y);
        }

        return result;
    }

    private Point[] createPoints(Geometry.Type type) {
        if (type == Geometry.Type.POINT) {
            return new Point[] { createPoint(2) };
        } else if (type == Geometry.Type.LINE) {
            return createLineString(rand.nextInt(2) + 1);
        } else {
            return createSimplePolygon(rand.nextInt(10) + 3);
        }
    }

    private String random_string(int length) {
        char[] buf = new char[length];

        for (int i = 0; i < length; i++) {
            buf[i] = (char) ('a' + rand.nextInt(26));
        }

        return new String(buf);
    }

    private URI random_uri() {
        return URI.create(random_string(3) + ":" + random_string(5));
    }

    private String next_entity_id() {
        return "http://test.dataconservancy.org/" + next_id++;
    }
}
