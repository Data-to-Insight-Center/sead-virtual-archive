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
package org.dataconservancy.query.gqmpsql;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.index.gqmpsql.DatabaseSessionPool;
import org.dataconservancy.index.gqmpsql.GqmIndexService;
import org.dataconservancy.index.gqmpsql.SpatialReferenceSystem;

public class QueryTest extends TestCase {
    private GqmQueryService query_service;
    private GqmIndexService index_service;

    protected void setUp() throws Exception {
        DatabaseSessionPool pool = new DatabaseSessionPool();
        index_service = new GqmIndexService(pool);
        query_service = new GqmQueryService(pool);
        index_service.clear();
    }

    protected void tearDown() throws Exception {
        index_service.clear();
        index_service.shutdown();
        query_service.shutdown();
    }

    private void index(GQM... gqms) throws IndexServiceException {
        BatchIndexer<GQM> batch = index_service.index();

        for (GQM gqm : gqms) {
            batch.add(gqm);
        }

        batch.close();
    }

    private void check_query(String query, GQM... correct_matches)
            throws QueryServiceException {
        QueryResult<GQM> result = query_service.query(query, 0, -1);

        assertEquals(0, result.getOffset());
        assertEquals(correct_matches.length, result.getTotal());
        assertEquals(correct_matches.length, result.getMatches().size());

        HashSet<GQM> matches = new HashSet<GQM>();

        for (QueryMatch<GQM> match : result.getMatches()) {
            assertNotNull(match);
            matches.add(match.getObject());
        }

        assertEquals(new HashSet<GQM>(Arrays.asList(correct_matches)), matches);
    }

    public void testEntityId() throws Exception {
        GQM gqm = new GQM("test:cow");

        index(gqm);

        check_query("entity-id('test:cow')", gqm);
        check_query("entity-id('test:doesnotexist')");
    }

    public void testRelated() throws Exception {
        GQM gqm = new GQM("test:cow");

        gqm.getRelations().add(new Relation(URI.create("fn:eats"), "grass"));

        index(gqm);

        check_query("relation('fn:eats' 'grass')", gqm);
        check_query("relation('blah:doesnotexist'  'foo')");
    }

    public void testDateTimeInterval() throws Exception {
        GQM gqm1 = new GQM("test:cow");
        gqm1.getIntervals().add(new DateTimeInterval(0, 10));

        GQM gqm2 = new GQM("test:cow2");
        gqm2.getIntervals().add(new DateTimeInterval(20, 21));

        index(gqm1, gqm2);

        check_query("datetime-covered-by('-10' '100')", gqm1, gqm2);
        check_query("datetime-covered-by('-100' '0')");
        check_query("datetime-covered-by('20' '30')", gqm2);
        check_query("datetime-covered-by('5' '8')");

        check_query("datetime-covers('10' '11')");
        check_query("datetime-covers('8' '9')", gqm1);
        check_query("datetime-covers('20' '21')", gqm2);
        check_query("datetime-covers('0' '100')");

        check_query("datetime-intersects('-10' '39')", gqm1, gqm2);
        check_query("datetime-intersects('3' '19')", gqm1);
        check_query("datetime-intersects('21' '30')");
        check_query("datetime-intersects('18' '21')", gqm2);
    }

    public void testNestedOperations() throws Exception {
        GQM gqm1 = new GQM("test:cow");
        gqm1.getIntervals().add(new DateTimeInterval(0, 10));

        GQM gqm2 = new GQM("test:cow2");
        gqm2.getIntervals().add(new DateTimeInterval(20, 21));

        index(gqm1, gqm2);

        check_query(
                "(entity-id('test:cow') & datetime-covered-by('-10' '100'))",
                gqm1);
        check_query(
                "(entity-id('test:moo') | datetime-covered-by('-10' '100'))",
                gqm1, gqm2);
        check_query(
                "((entity-id('test:moo') & datetime-covered-by('-10' '100')) | entity-id('test:cow'))",
                gqm1);
        check_query(
                "((entity-id('test:cow2') & datetime-covered-by('-10' '100')) | entity-id('test:cow'))",
                gqm1, gqm2);

        check_query(
                "(entity-id('test:cow') & ((entity-id('test:cow') & entity-id('test:cow')) & entity-id('test:cow')))",
                gqm1);
    }

    public void testGeospatialPolygons() throws Exception {
        GQM gqm1 = new GQM("test:pasture1");
        GQM gqm2 = new GQM("test:pasture2");

        URI srid = SpatialReferenceSystem.forEPSG(4326);
        gqm1.getLocations().add(
                new Location(new Geometry(Geometry.Type.POLYGON,
                        new Point(0, 0), new Point(0, 20), new Point(20, 20),
                        new Point(20, 0)), srid));
        gqm2.getLocations().add(
                new Location(new Geometry(Geometry.Type.POLYGON, new Point(15,
                        15), new Point(15, 30), new Point(30, 30), new Point(
                        30, 15)), srid));

        index(gqm1, gqm2);

        check_query("intersects([polygon 'EPSG:4326' 5 5, 10 5, 10 30, 5 30])",
                gqm1);
        check_query("intersects([polygon 'EPSG:4326' 5 5, 50 5, 50 50, 5 50])",
                gqm1, gqm2);
        check_query("intersects([polygon 'EPSG:4326' 0 30, 5 30, 5 40, 0 40])");
        check_query(
                "intersects([polygon 'EPSG:4326' 28 28, 30 28, 30 30, 28 30])",
                gqm2);

        // within argument in degrees
        check_query("within([point 'EPSG:4326' 10 10] '30')", gqm1, gqm2);
        check_query("within([point 'EPSG:4326' 0 0] '5')", gqm1);
        check_query(
                "within([polygon 'EPSG:4326' 25 25, 30 25, 30 30, 25 30] '30')",
                gqm1, gqm2);

        check_query("covers([polygon 'EPSG:4326' 3 3, 8 3,  8 8, 3 8])", gqm1);
        check_query("covers([polygon 'EPSG:4326' 18 18, 20 18, 20 20, 18 20])",
                gqm1, gqm2);
        check_query("covers([polygon 'EPSG:4326' 25 25, 30 25, 30 80, 25 80])");

        check_query("covered-by([polygon 'EPSG:4326' 0 0, 40 0, 40 40, 0 35])",
                gqm1, gqm2);
        check_query("covered-by([polygon 'EPSG:4326' 0 0, 10 0, 10 10, 0 10])");
        check_query("covered-by([polygon 'EPSG:4326' 0 0, 25 0, 25 25, 0 25])",
                gqm1);
        check_query(
                "covered-by([polygon 'EPSG:4326' 10 10, 45 10, 45 45, 10 45])",
                gqm2);
    }

    public void testGeospatialPointsAndLines() throws Exception {
        GQM point = new GQM("test:cow");
        GQM line = new GQM("test:trail");

        URI srid = SpatialReferenceSystem.forEPSG(4326);
        point.getLocations().add(
                new Location(
                        new Geometry(Geometry.Type.POINT, new Point(0, 0)),
                        srid));
        line.getLocations().add(
                new Location(new Geometry(Geometry.Type.LINE, new Point(0, 0),
                        new Point(10, 10), new Point(20, 15)), srid));

        index(point, line);

        check_query("intersects([point 'EPSG:4326' 8 0])");
        check_query("intersects([point 'EPSG:4326' 0 0])", point, line);
        check_query("intersects([point 'EPSG:4326' 5 5])", line);

        check_query("intersects([polygon 'EPSG:4326' 5 5, 10 5, 10 30, 5 30])",
                line);
        check_query(
                "intersects([polygon 'EPSG:4326' -5 -5, 50 -5, 50 50, 5 50])",
                point, line);
        check_query("intersects([line 'EPSG:4326' 5 5, 10 -5, 20 20])", line);

        // within argument in degrees
        check_query(
                "within([polygon 'EPSG:4326' 25 25, 30 25, 30 30, 25 30] '40')",
                point, line);
        check_query("within([point 'EPSG:4326' 20 20] '8')", line);

        check_query("covers([point 'EPSG:4326' 0 0])", point, line);
        check_query("covers([point 'EPSG:4326' 10 10])", line);
        check_query("covers([polygon 'EPSG:4326' -5 -5, 20 18, 20 20, 18 20])");
        check_query("covers([line 'EPSG:4326' 10 10, 20 15])", line);

        check_query("covered-by([point 'EPSG:4326' 0 0])", point);
        check_query("covered-by([polygon 'EPSG:4326' 0 0, 10 0, 10 10, 0 10])",
                point);
        check_query("covered-by([polygon 'EPSG:4326' 0 0, 25 0, 25 25, 0 25])",
                line, point);
        check_query("covered-by([polygon 'EPSG:4326' 10 10, 45 10, 45 45, 10 15])");
    }

    public void testCelestialPointsAndLines() throws Exception {
        GQM point = new GQM("test:starcow");
        GQM line = new GQM("test:startrail");

        URI srid = SpatialReferenceSystem
                .forEquatorialCoordinateSystem("J2000");
        point.getLocations().add(
                new Location(
                        new Geometry(Geometry.Type.POINT, new Point(0, 0)),
                        srid));
        line.getLocations().add(
                new Location(new Geometry(Geometry.Type.LINE, new Point(0, 0),
                        new Point(10, 10), new Point(20, 15)), srid));

        index(point, line);

        check_query("covers([polygon 'ECS:J2000' 0 0, 20 18, 20 20, 18 20])");

        check_query("covers([point 'ECS:J2000' 0 0])", point, line);
        check_query("covers([point 'ECS:J2000' 10 10])", line);

        // line covers line not supported
        // check_query("covers([line 'ECS:J2000' 10 10, 20 15])", line);

        check_query("covered-by([point 'ECS:J2000' 0 0])", point);
        check_query("covered-by([polygon 'ECS:J2000' 0 0, 10 0, 10 10, 0 10])",
                point);
        check_query("covered-by([polygon 'ECS:J2000' 0 0, 25 0, 25 25, 0 25])",
                line, point);
        check_query("covered-by([polygon 'ECS:J2000' 10 10, 45 10, 45 45, 10 15])");

        check_query("intersects([point 'ECS:J2000' 8 0])");
        check_query("intersects([point 'ECS:J2000' 0 0])", point, line);
        check_query("intersects([point 'ECS:J2000' 10 10])", line);

        check_query("intersects([polygon 'ECS:J2000' 5 5, 10 5, 10 30, 5 30])",
                line);
        check_query(
                "intersects([polygon 'ECS:J2000' 0 0, 50 -5, 50 50, 5 50])",
                point, line);
        check_query("intersects([line 'ECS:J2000' 5 5, 10 -5, 20 20])", line);

        // within argument in degrees
        check_query("within([point 'ECS:J2000' 1 1] '4')", point);

        check_query("within([point 'ECS:J2000' 45 35] '4')");

        // line tests are not well supported
        // check_query("within([line 'ECS:J2000' 2 2, 4, 3] '5')", line);

        // polygon test are not supported
        // check_query(
        // "within([polygon 'ECS:J2000' 25 25, 30 25, 30 30, 25 30] '40')",
        // point, line);
    }

    public void testCelestialPolygons() throws Exception {
        GQM gqm1 = new GQM("test:pasture_constellation1");
        GQM gqm2 = new GQM("test:pasture_constellation2");

        URI srid = SpatialReferenceSystem
                .forEquatorialCoordinateSystem("J2000");
        gqm1.getLocations().add(
                new Location(new Geometry(Geometry.Type.POLYGON,
                        new Point(0, 0), new Point(0, 20), new Point(20, 20),
                        new Point(20, 0)), srid));
        gqm2.getLocations().add(
                new Location(new Geometry(Geometry.Type.POLYGON, new Point(15,
                        15), new Point(15, 30), new Point(30, 30), new Point(
                        30, 15)), srid));

        index(gqm1, gqm2);

        check_query("intersects([polygon 'ECS:J2000' 5 5, 10 5, 10 30, 5 30])",
                gqm1);
        check_query("intersects([polygon 'ECS:J2000' 5 5, 50 5, 50 50, 5 50])",
                gqm1, gqm2);
        check_query("intersects([polygon 'ECS:J2000' 0 30, 5 30, 5 40, 0 40])");
        check_query(
                "intersects([polygon 'ECS:J2000' 28 28, 30 28, 30 30, 28 30])",
                gqm2);

        // within argument in degrees
        // polygons not handled by pgsphere distance check
        // check_query("within([point 'ECS:J2000' 10 10] '30')", gqm1, gqm2);
        // check_query("within([point 'ECS:J2000' 0 0] '5')", gqm1);
        // check_query(
        // "within([polygon 'ECS:J2000' 25 25, 30 25, 30 30, 25 30] '30')",
        // gqm1, gqm2);

        check_query("covers([polygon 'ECS:J2000' 3 3, 8 3,  8 8, 3 8])", gqm1);
        check_query("covers([polygon 'ECS:J2000' 18 18, 20 18, 20 20, 18 20])",
                gqm1, gqm2);
        check_query("covers([polygon 'ECS:J2000' 25 25, 30 25, 30 80, 25 80])");

        check_query("covered-by([polygon 'ECS:J2000' 0 0, 40 0, 40 40, 0 35])",
                gqm1, gqm2);
        check_query("covered-by([polygon 'ECS:J2000' 0 0, 10 0, 10 10, 0 10])");
        check_query("covered-by([polygon 'ECS:J2000' 0 0, 25 0, 25 25, 0 25])",
                gqm1);
        check_query(
                "covered-by([polygon 'ECS:J2000' 10 10, 45 10, 45 45, 10 45])",
                gqm2);
    }

    public void testPaging() throws Exception {
        GQM[] gqms = new GQM[100];

        for (int i = 0; i < gqms.length; i++) {
            GQM gqm = new GQM("" + i);

            gqms[i] = gqm;

            gqm.getRelations().add(
                    new Relation(URI.create("evenodd"), (i & 1) == 0 ? "even"
                            : "odd"));
            gqm.getRelations().add(new Relation(URI.create("type"), "gorilla"));

        }

        index(gqms);

        QueryResult<GQM> result = query_service.query(
                "relation('type' 'gorilla')", 0, -1);

        assertEquals(0, result.getOffset());
        assertEquals(100, result.getTotal());
        assertEquals(100, result.getMatches().size());

        for (int i = 0; i < 100; i++) {
            assertEquals(gqms[i], result.getMatches().get(i).getObject());
        }

        result = query_service.query("relation('type' 'gorilla')", 0, 10);

        assertEquals(0, result.getOffset());
        assertEquals(100, result.getTotal());
        assertEquals(10, result.getMatches().size());

        for (int i = 0; i < 10; i++) {
            assertEquals(gqms[i], result.getMatches().get(i).getObject());
        }

        result = query_service.query("relation('type' 'gorilla')", 10, 10);

        assertEquals(10, result.getOffset());
        assertEquals(100, result.getTotal());
        assertEquals(10, result.getMatches().size());

        for (int i = 0; i < 10; i++) {
            assertEquals(gqms[i + 10], result.getMatches().get(i).getObject());
        }

        result = query_service.query("relation('type' 'gorilla')", 1000, -1);

        assertEquals(100, result.getOffset());
        assertEquals(100, result.getTotal());
        assertEquals(0, result.getMatches().size());

        result = query_service.query("relation('evenodd' 'odd')", 20, 5);

        assertEquals(20, result.getOffset());
        assertEquals(50, result.getTotal());
        assertEquals(5, result.getMatches().size());

        for (int i = 0; i < 5; i++) {
            assertEquals(gqms[((i + 20) * 2) + 1], result.getMatches().get(i)
                    .getObject());
        }
    }
}
