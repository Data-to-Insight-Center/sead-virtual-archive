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
import java.net.URISyntaxException;

import java.util.Arrays;

import junit.framework.TestCase;

public class GQMTest extends TestCase {

    public void testRelation() {
        URI pred = URI.create("farm:hasCow");

        Relation r = new Relation(pred, "true");

        assertEquals(pred, r.getPredicate());
        assertEquals("true", r.getObject());

        r.setObject("false");
        assertEquals("false", r.getObject());

        pred = URI.create("farm:butcherCow");
        r.setPredicate(pred);
        assertEquals(pred, r.getPredicate());
    }

    public void testDateTimeInterval() throws URISyntaxException {
        DateTimeInterval dti = new DateTimeInterval(0, 2);

        assertEquals(0, dti.getStart());
        assertEquals(2, dti.getEnd());

        dti.setStart(3);
        dti.setEnd(4);

        assertEquals(3, dti.getStart());
        assertEquals(4, dti.getEnd());
    }

    public void testGeometry() {
        Geometry g = new Geometry(Geometry.Type.LINE, new Point[] { new Point(new double[] { 3, 3 }),
                new Point(new double[] { 4, 4 })});

        assertEquals(Geometry.Type.LINE, g.getType());
        assertTrue(Arrays.deepEquals(new Point[] { new Point(new double[] { 3, 3 }),
                new Point(new double[] { 4, 4 }) }, g.getPoints()));
    }

    public void testLocation() {
        URI srid = URI.create("epsg:123");
        Geometry g = new Geometry(Geometry.Type.POINT, new Point[] { new Point(new double[]{1, 2} )} );
        Location l = new Location(g, srid);

        assertEquals(g, l.getGeometry());
        assertEquals(srid, l.getSrid());
    }

    public void testGQM() {
        String entity_id = "http://serenity.dkc.jhu.edu/dcs/bull42";
        GQM gqm = new GQM(entity_id);

        assertEquals(entity_id, gqm.getEntityId());
        assertNotNull(gqm.getIntervals());
        assertNotNull(gqm.getLocations());
        assertNotNull(gqm.getRelations());
    }
}
