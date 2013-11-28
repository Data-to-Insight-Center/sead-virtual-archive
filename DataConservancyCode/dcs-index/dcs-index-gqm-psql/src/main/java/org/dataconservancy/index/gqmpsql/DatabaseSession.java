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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;

/**
 * A DatabaseSession represents one thread interacting with a GQM database. It
 * is not thread safe, but multiple instances may simultaneously insert data and
 * read data. Accessing the database while simultaneously dropping or clearing
 * tables will result in undefined behavior.
 * 
 * See https://wiki.library.jhu.edu/x/rIQXAQ for the database schema.
 */

public class DatabaseSession {
    public static final int POSTGIS_SRID = 4326; // SRID of postgis table - WGS
                                                 // 84

    private final Connection con;
    private Map<String, Integer> postgis_srid_cache; // auth_name + auth_srid ->
                                                     // postgis srid

    public DatabaseSession(Connection con) {
        this.con = con;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return con.prepareStatement(sql);
    }

    /**
     * Create tables and indexes in an empty database.
     * 
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        Statement stat = con.createStatement();

        try {
            // standard postgresql tables

            stat.executeUpdate("CREATE TABLE gqm(gqm_id BIGSERIAL, entity_id CHARACTER VARYING NOT NULL, PRIMARY KEY (gqm_id))");
            stat.executeUpdate("CREATE INDEX gqm_index ON gqm (entity_id)");

            stat.executeUpdate("CREATE TABLE relation(gqm_id BIGINT NOT NULL, pred CHARACTER VARYING NOT NULL,obj CHARACTER VARYING NOT NULL)");
            stat.executeUpdate("CREATE INDEX relation_index ON relation (gqm_id,pred,obj);");

            stat.executeUpdate("CREATE TABLE location(gqm_id BIGINT NOT NULL, type INT NOT NULL, points TEXT NOT NULL, srsid CHARACTER VARYING NOT NULL)");
            stat.executeUpdate("CREATE INDEX location_index ON location (gqm_id)");

            stat.executeUpdate("CREATE TABLE time_interval(gqm_id BIGINT NOT NULL, time_start BIGINT NOT NULL, time_end BIGINT NOT NULL)");
            stat.executeUpdate("CREATE INDEX time_interval_index ON time_interval (gqm_id,time_start,time_end)");

            // postgis tables

            // Cannot use GEOGRAPHY type because of limited support
            // Only 2 dimensions supported.

            // stat.executeUpdate("CREATE TABLE postgis (gqm_id BIGINT NOT NULL, loc GEOGRAPHY(GEOMETRY, 4326))");

            stat.executeUpdate("CREATE TABLE postgis (gqm_id BIGINT NOT NULL)");
            stat.execute("SELECT AddGeometryColumn('postgis', 'loc', "
                    + POSTGIS_SRID + ", 'GEOMETRY', 2)");
            stat.executeUpdate("CREATE INDEX postgis_index ON postgis USING GIST (loc)");

            // ALTER TABLE postgis
            // ADD CONSTRAINT geometry_valid_check
            // CHECK (ST_IsValid(loc));

            // pgsphere tables

            stat.executeUpdate("CREATE TABLE pgsphere (gqm_id BIGINT NOT NULL, point SPOINT, line SPATH, poly SPOLY)");
            stat.executeUpdate("CREATE INDEX pgsphere_index ON pgsphere USING GIST (point, line, poly)");
        } finally {
            stat.close();
        }
    }

    /**
     * @param auth_name
     * @param auth_srid
     * @return internal postgis srid from spatial_ref_sys table
     * @throws SQLException
     */
    public int lookupPostgisSrid(String auth_name, int auth_srid)
            throws SQLException {

        if (postgis_srid_cache == null) {
            postgis_srid_cache = new HashMap<String, Integer>();
        }

        String key = auth_name + auth_srid;
        Integer srid = postgis_srid_cache.get(key);

        if (srid != null) {
            return srid;
        }

        PreparedStatement stat = prepareStatement("SELECT srid FROM spatial_ref_sys WHERE auth_name = ? AND auth_srid = ?");

        try {
            stat.setString(1, auth_name);
            stat.setInt(2, auth_srid);

            ResultSet rs = stat.executeQuery();

            if (rs.next()) {
                int result = rs.getInt(1);
                postgis_srid_cache.put(key, result);

                return result;
            } else {
                postgis_srid_cache.put(key, -1);

                return -1;
            }

        } finally {
            stat.close();
        }
    }

    /**
     * @param gqm
     * @return identifier for the inserted GQM
     * @throws SQLException
     */
    public long insert(GQM gqm) throws SQLException {
        PreparedStatement insert_gqm = con.prepareStatement(
                "INSERT INTO gqm (entity_id) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS);
        PreparedStatement insert_relation = con
                .prepareStatement("INSERT INTO relation VALUES (?, ?, ?)");
        PreparedStatement insert_location = con
                .prepareStatement("INSERT INTO location VALUES (?, ?, ?, ?)");
        PreparedStatement insert_time_interval = con
                .prepareStatement("INSERT INTO time_interval VALUES (?, ?, ?)");
        PreparedStatement insert_location_postgis = con
                .prepareStatement("INSERT INTO postgis VALUES (?, ST_Transform(ST_GeomFromEWKT(?), "
                        + POSTGIS_SRID + "))");
        PreparedStatement insert_location_pgsphere_point = con
                .prepareStatement("INSERT INTO pgsphere VALUES (?, spoint(?), NULL, NULL)");
        PreparedStatement insert_location_pgsphere_line = con
                .prepareStatement("INSERT INTO pgsphere VALUES (?, NULL, spath(?), NULL)");
        PreparedStatement insert_location_pgsphere_polygon = con
                .prepareStatement("INSERT INTO pgsphere VALUES (?, NULL, NULL, spoly(?))");

        long gqm_id;

        try {
            insert_gqm.setString(1, gqm.getEntityId());
            insert_gqm.executeUpdate();
            ResultSet keys = insert_gqm.getGeneratedKeys();

            if (!keys.next()) {
                throw new SQLException("Failed to retrieve gqm_id");
            }

            gqm_id = keys.getLong(1);
            insert_gqm.close();

            for (Relation rel : gqm.getRelations()) {
                insert_relation.setLong(1, gqm_id);
                insert_relation.setString(2, rel.getPredicate().toString());
                insert_relation.setString(3, rel.getObject().toString());
                insert_relation.addBatch();
            }

            for (Location loc : gqm.getLocations()) {
                Geometry g = loc.getGeometry();

                insert_location.setLong(1, gqm_id);
                insert_location.setInt(2, g.getType().ordinal());
                insert_location.setString(3, points_to_string(g.getPoints()));
                insert_location.setString(4, loc.getSrid().toString());
                insert_location.addBatch();
            }

            for (DateTimeInterval dti : gqm.getIntervals()) {
                insert_time_interval.setLong(1, gqm_id);
                insert_time_interval.setLong(2, dti.getStart());
                insert_time_interval.setLong(3, dti.getEnd());
                insert_time_interval.addBatch();
            }

            for (Location loc : gqm.getLocations()) {
                SpatialReferenceSystem srs = SpatialReferenceSystem.lookup(loc
                        .getSrid());

                if (srs.isGeospatialCoordinateSystem()) {
                    PreparedStatement stat = insert_location_postgis;

                    int srid = lookupPostgisSrid(srs.authorityName(),
                            srs.authorityId());

                    if (srid == -1) {
                        throw new SQLException("No postgis srid for "
                                + loc.getSrid());
                    }

                    stat.setLong(1, gqm_id);
                    stat.setString(2, PostgisUtil.convertGeometryToEwkt(
                            loc.getGeometry(), srid));
                    stat.addBatch();
                } else if (srs.isEquatorialCoordinateSystem()) {
                    PreparedStatement stat;

                    Geometry.Type type = loc.getGeometry().getType();
                    String geom = PgsphereUtil.convertGeometry(loc
                            .getGeometry());

                    if (type == Geometry.Type.POINT) {
                        stat = insert_location_pgsphere_point;
                    } else if (type == Geometry.Type.LINE) {
                        stat = insert_location_pgsphere_line;
                    } else if (type == Geometry.Type.POLYGON) {
                        stat = insert_location_pgsphere_polygon;
                    } else {
                        throw new SQLException("Unhandled geometry type "
                                + type);
                    }

                    stat.setLong(1, gqm_id);
                    stat.setString(2, geom);

                    stat.addBatch();
                } else {
                    throw new SQLException(
                            "Unhandled spatial reference system: "
                                    + loc.getSrid());
                }
            }

            run_batch(insert_relation);
            run_batch(insert_location);
            run_batch(insert_time_interval);
            run_batch(insert_location_postgis);
            run_batch(insert_location_pgsphere_point);
            run_batch(insert_location_pgsphere_line);
            run_batch(insert_location_pgsphere_polygon);

            con.commit();
        } finally {
            con.rollback();
        }

        return gqm_id;
    }

    private static void run_batch(Statement stat) throws SQLException {
        try {
            int[] batch_results = stat.executeBatch();

            for (int result : batch_results) {
                if (result == Statement.EXECUTE_FAILED) {
                    throw new SQLException("Batch execution failed");
                }
            }
        } finally {
            stat.close();
        }
    }

    /**
     * @param gqm_id
     * @return GQM for given id or null
     * @throws SQLException
     */
    public GQM lookup(long gqm_id) throws SQLException {
        PreparedStatement stat = con
                .prepareStatement("SELECT * FROM gqm WHERE gqm_id = ?");

        stat.setLong(1, gqm_id);

        ResultSet rs = stat.executeQuery();

        if (!rs.next()) {
            stat.close();
            return null;
        }

        GQM gqm = new GQM(rs.getString(2));

        stat.close();

        // relations

        stat = con.prepareStatement("SELECT * FROM relation WHERE gqm_id = ?");
        stat.setLong(1, gqm_id);

        rs = stat.executeQuery();

        while (rs.next()) {
            gqm.getRelations().add(
                    new Relation(URI.create(rs.getString(2)), rs.getString(3)));
        }

        stat.close();

        // time intervals

        stat = con
                .prepareStatement("SELECT * FROM time_interval WHERE gqm_id = ?");
        stat.setLong(1, gqm_id);

        rs = stat.executeQuery();

        while (rs.next()) {
            gqm.getIntervals().add(
                    new DateTimeInterval(rs.getLong(2), rs.getLong(3)));
        }

        stat.close();

        // locations

        stat = con.prepareStatement("SELECT * FROM location WHERE gqm_id = ?");
        stat.setLong(1, gqm_id);
        rs = stat.executeQuery();

        while (rs.next()) {
            Geometry g = new Geometry(Geometry.Type.values()[rs.getInt(2)],
                    points_from_string(rs.getString(3)));
            Location loc = new Location(g, URI.create(rs.getString(4)));

            gqm.getLocations().add(loc);
        }

        stat.close();

        return gqm;
    }

    private String points_to_string(Point[] points) {
        StringBuilder sb = new StringBuilder(16);
        sb.append('{');

        for (int i = 0; i < points.length; i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append('{');

            double[] coords = points[i].getCoordinates();

            for (int j = 0; j < coords.length; j++) {
                if (j > 0) {
                    sb.append(',');
                }

                sb.append(coords[j]);
            }

            sb.append('}');
        }

        sb.append('}');
        return sb.toString();
    }

    private Point[] points_from_string(String s) throws SQLException {
        ArrayList<Point> result = new ArrayList<Point>(2);

        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');

        if (start == -1 || end == -1) {
            throw new SQLException("Unbalanced {} in points: " + s);
        }

        start++;

        for (;;) {
            start = s.indexOf('{', start);

            if (start == -1) {
                break;
            }

            start++;
            end = s.indexOf('}', start);

            if (end == -1) {
                throw new SQLException("Unbalanced {} in points: " + s);
            }

            String[] parts = s.substring(start, end).split(",");

            double[] d = new double[parts.length];

            for (int i = 0; i < parts.length; i++) {
                try {
                    d[i] = Double.parseDouble(parts[i]);
                } catch (NumberFormatException e) {
                    throw new SQLException("Error parsing points: " + s, e);
                }
            }

            result.add(new Point(d));
        }

        return result.toArray(new Point[] {});
    }

    public void close() throws SQLException {
        try {
            con.commit();
        } finally {
            con.close();
        }
    }

    /**
     * Run analyze on the database. Should be done after a large number of
     * changes.
     * 
     * @throws SQLException
     */
    public void analyze() throws SQLException {
        Statement stat = con.createStatement();

        try {
            stat.executeUpdate("ANALYZE");
        } finally {
            stat.close();
        }
    }

    /**
     * @return count of GQM instances in database
     * @throws SQLException
     */
    public long size() throws SQLException {
        Statement stat = con.createStatement();

        try {
            ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM gqm");
            rs.next();
            return rs.getLong(1);

        } finally {
            stat.close();
        }
    }

    /**
     * @return whether or not tables have been created
     * @throws SQLException
     */
    public boolean hasTables() throws SQLException {
        Statement stat = con.createStatement();

        try {
            ResultSet rs = stat
                    .executeQuery("SELECT COUNT (relname) FROM pg_class WHERE relname = 'gqm'");
            rs.next();
            return rs.getInt(1) > 0;
        } finally {
            stat.close();
        }
    }

    /**
     * Clear all the tables in the database.
     * 
     * @throws SQLException
     */
    public void clearTables() throws SQLException {
        Statement stat = con.createStatement();

        try {
            stat.executeUpdate("DELETE FROM gqm");
            stat.executeUpdate("DELETE FROM relation");
            stat.executeUpdate("DELETE FROM location");
            stat.executeUpdate("DELETE FROM time_interval");
            stat.executeUpdate("DELETE FROM postgis");
            stat.executeUpdate("DELETE FROM pgsphere");
        } finally {
            stat.close();
        }
    }

    /**
     * Drop all the tables in the database.
     * 
     * @throws SQLException
     */
    public void dropTables() throws SQLException {
        Statement stat = con.createStatement();

        try {
            stat.executeUpdate("DROP TABLE IF EXISTS gqm");
            stat.executeUpdate("DROP TABLE IF EXISTS relation");
            stat.executeUpdate("DROP TABLE IF EXISTS location");
            stat.executeUpdate("DROP TABLE IF EXISTS time_interval");
            stat.executeUpdate("DROP TABLE IF EXISTS postgis");
            stat.executeUpdate("DROP TABLE IF EXISTS pgsphere");
        } finally {
            stat.close();
        }
    }

    // TODO also delete entries from other tables with same gqm_id

    public void deleteByEntity(String entity_id) throws SQLException {
        PreparedStatement stat = con
                .prepareStatement("DELETE FROM gqm WHERE entity_id = ?");
        stat.setString(1, entity_id);

        try {
            stat.executeUpdate();
            con.commit();
        } finally {
            con.rollback();
            stat.close();
        }
    }
}
