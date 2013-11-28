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
import java.sql.SQLException;

import junit.framework.TestCase;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;

public class DatabaseTest extends TestCase {
    private DatabaseSession db;

    protected void setUp() throws Exception {
        DatabaseSessionPool pool = new DatabaseSessionPool();
        db = pool.connect();
        db.dropTables();
        db.createTables();
    }

    protected void tearDown() throws Exception {
        db.dropTables();
        db.close();
    }

    public void testSimple() throws SQLException {
        GQM gqm = new GQM("test:test");
        gqm.getIntervals().add(new DateTimeInterval(0, 10));
        gqm.getRelations().add(
                new Relation(URI.create("test:predicate"), "object"));
        gqm.getLocations().add(
                new Location(
                        new Geometry(Geometry.Type.POINT, new Point(0, 0)),
                        SpatialReferenceSystem.forEPSG(3000)));
        gqm.getLocations().add(
                new Location(
                        new Geometry(Geometry.Type.POINT, new Point(0, 0)),
                        SpatialReferenceSystem
                                .forEquatorialCoordinateSystem("J2000")));

        assertTrue(db.hasTables());
        
        long id = db.insert(gqm);
        GQM result = db.lookup(id);

        assertEquals(gqm, result);
        assertEquals(1, db.size());
        
        db.clearTables();
        assertEquals(0, db.size());
        assertNull(db.lookup(id));
    }

    public void testCelestial() throws SQLException {
        GQM gqm = new GQM("test:test");
        gqm.getLocations().add(
                new Location(
                        new Geometry(Geometry.Type.POINT, new Point(2, 2)),
                        SpatialReferenceSystem
                                .forEquatorialCoordinateSystem("J2000")));
        gqm.getLocations().add(
                new Location(new Geometry(Geometry.Type.LINE, new Point(0, 0),
                        new Point(5, 0)), SpatialReferenceSystem
                        .forEquatorialCoordinateSystem("J2000")));
        gqm.getLocations().add(
                new Location(new Geometry(Geometry.Type.POLYGON,
                        new Point(0, 0), new Point(5, 0), new Point(5, 5),
                        new Point(0, 5)), SpatialReferenceSystem
                        .forEquatorialCoordinateSystem("J2000")));

        long id = db.insert(gqm);
        GQM result = db.lookup(id);

        assertEquals(gqm, result);
        assertEquals(1, db.size());
    }

    public void testRandom() throws SQLException {
        RandomGqmBuilder rqb = new RandomGqmBuilder();
        int num = 500;

        for (int i = 0; i < num; i++) {
            GQM gqm = rqb.createGqm();

            long id = db.insert(gqm);
            GQM result = db.lookup(id);

            assertEquals(gqm, result);
        }

        assertEquals(num, db.size());

        db.analyze();
    }

    public void testDeleteEntity() throws SQLException {
        RandomGqmBuilder rqb = new RandomGqmBuilder();
        GQM gqm = rqb.createGqm();

        long id = db.insert(gqm);
        GQM result = db.lookup(id);

        assertEquals(gqm, result);
        assertEquals(1, db.size());

        db.deleteByEntity(gqm.getEntityId());

        assertEquals(0, db.size());
        assertNull(db.lookup(id));
    }
}
