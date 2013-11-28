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

public class Point {

    private double[] coordinates;

    public Point(double... coordinates) {
        this.coordinates = coordinates;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double... coordinates) {
        this.coordinates = coordinates;
    }

    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    public boolean equals(Object o) {

        Point p = (Point) o;

        if (p == null) {
            return false;
        }

        return (Arrays.equals(coordinates, p.getCoordinates()));
    }

    public String toString() {
        return Arrays.toString(coordinates);
    }
    
    public double[] fromString(String in) {
        in = in.replace("[", "");
        in = in.replace("]", "");
        
        String[] strings = in.split(", ");
        double result[] = new double[strings.length];
        
        for (int i = 0; i < result.length; i++) {
                result[i] = Double.parseDouble(strings[i]);
        }
        return result;
    }
}
