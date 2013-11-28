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

import java.util.Arrays;

/**
 * A point, line, or polygon is made up of a list of points. Each point is a
 * list of coordinates.
 */
public class Geometry {

    public enum Type {
        POINT, LINE, POLYGON;
    }

    private Point[] points;
    private Type type;

    /**
     * @param type
     * @param points
     *            Geometry object saves reference.
     */
    public Geometry(Type type, Point... points) {
        this.type = type;
        this.points = points;
    }

    /**
     * @return reference to points making up the Geometry
     */
    public Point[] getPoints() {
        return points;
    }

    /**
     * @param points
     *            Geometry object saves reference.
     */
    public void setPoints(Point... points) {
        this.points = points;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int hashCode() {
        return Arrays.deepHashCode(points);
    }

    public boolean equals(Object o) {
        Geometry g = (Geometry) o;

        if (g == null) {
            return false;
        }

        return Util.equals(type, g.type) && Arrays.deepEquals(points, g.points);
    }

    public String toString() {
        return type == null ? "" : type.name() + " " + Arrays.toString(points);
    }
}
