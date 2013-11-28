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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Geometry.Type;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.index.gqmpsql.DatabaseSession;
import org.dataconservancy.index.gqmpsql.PgsphereUtil;
import org.dataconservancy.index.gqmpsql.PostgisUtil;
import org.dataconservancy.index.gqmpsql.SpatialReferenceSystem;
import org.dataconservancy.query.gqmpsql.lang.model.Query;
import org.dataconservancy.query.gqmpsql.lang.model.Query.Operation;

/**
 * Transform a query into a SQL statement. Not thread safe. Uses
 * PreparedStatement to guard against SQL injection.
 */

public class SqlQueryTransformer {
    private final static String FROM_EXPR = "gqm LEFT JOIN relation USING (gqm_id) LEFT JOIN location USING (gqm_id) LEFT JOIN time_interval USING (gqm_id) LEFT JOIN postgis USING (gqm_id) LEFT JOIN pgsphere USING (gqm_id)";

    private final StringBuilder sql; // conditionals
    private final List<Object> sql_args;
    private final DatabaseSession db;

    public SqlQueryTransformer(DatabaseSession db) {
        this.db = db;
        this.sql = new StringBuilder();
        this.sql_args = new ArrayList<Object>();
    }

    private String asSqlQuery(long offset, int limit) {
        return "SELECT DISTINCT gqm.gqm_id FROM " + FROM_EXPR + " WHERE " + sql
                + " ORDER BY gqm.gqm_id LIMIT " + limit + " OFFSET " + offset;
    }

    private String asSqlQueryCount() {
        return "SELECT COUNT(DISTINCT gqm.gqm_id) FROM " + FROM_EXPR
                + " WHERE " + sql;
    }

    public PreparedStatement asQueryCountStatement(DatabaseSession db)
            throws SQLException {
        PreparedStatement stat = db.prepareStatement(asSqlQueryCount());
        marshall(db, stat, sql_args);

        return stat;
    }

    public PreparedStatement asQueryStatement(DatabaseSession db, long offset,
            int matches) throws SQLException {
        PreparedStatement stat = db
                .prepareStatement(asSqlQuery(offset, matches));
        marshall(db, stat, sql_args);

//        System.out.println(asSqlQuery(offset, matches));
//        System.out.println(sql_args);

        return stat;
    }

    private static void marshall(DatabaseSession db, PreparedStatement stat,
            List<Object> args) throws SQLException {
        int i = 1;

        for (Object arg : args) {
            if (arg instanceof String) {
                stat.setString(i, (String) arg);
            } else if (arg instanceof Long) {
                stat.setLong(i, (Long) arg);
            } else if (arg instanceof Integer) {
                stat.setInt(i, (Integer) arg);
            } else if (arg instanceof Double) {
                stat.setDouble(i, (Double) arg);
            } else {
                throw new IllegalStateException("Unknown type of argument: "
                        + arg);
            }

            i++;
        }
    }

    // Return a point geometry from a generic geometry
    private static Geometry asPoint(Geometry g) {
        if (g.getType() == Geometry.Type.POINT) {
            return g;
        }

        // pick a middle point

        int middle = g.getPoints().length / 2;

        return new Geometry(Type.POINT, g.getPoints()[middle]);
    }

    public void transform(Query query) throws QueryServiceException {
        if (query.isOperation()) {
            Query[] children = query.children();
            sql.append('(');

            for (int i = 0; i < children.length; i++) {
                if (i > 0) {
                    if (query.operation() == Operation.AND) {
                        sql.append(" AND ");
                    } else if (query.operation() == Operation.OR) {
                        sql.append(" OR ");
                    } else {
                        throw new QueryServiceException("Unknown operation: "
                                + query.operation());
                    }
                }

                transform(children[i]);
            }

            sql.append(')');
        } else {
            String name = query.function().name();
            Object[] args = query.function().arguments();

            sql.append('(');

            if (name.equals("entity-id")) {
                check_num_args(args, 1, name);
                sql.append("gqm.entity_id = ?");
                add_string_arg(args[0], name);
            } else if (name.equals("relation")) {
                check_num_args(args, 2, name);

                sql.append("(relation.pred = ?) AND (relation.obj = ?)");

                add_string_arg(args[0], name);
                add_string_arg(args[1], name);
            } else if (name.equals("covers")) {
                check_num_args(args, 1, name);
                SpatialReferenceSystem srs = get_srs(args[0], name);
                Location loc = (Location) args[0];

                if (srs.isGeospatialCoordinateSystem()) {
                    sql.append("ST_Covers(ST_Transform(postgis.loc, ?), ST_GeomFromEWKT(?))");

                    add_postgis_srid_location_args(srs, loc, name);
                } else if (srs.isEquatorialCoordinateSystem()) {
                    Geometry.Type type = loc.getGeometry().getType();
                    String geom = PgsphereUtil.convertGeometry(loc
                            .getGeometry());

                    if (type == Geometry.Type.POINT) {
                        sql.append("((pgsphere.point = spoint(?)) OR (pgsphere.line ~ spoint(?)) OR (pgsphere.poly ~ spoint(?)))");

                        sql_args.add(geom);
                        sql_args.add(geom);
                        sql_args.add(geom);
                    } else if (type == Geometry.Type.LINE) {
                        // TODO: Cannot check path covering path

                        sql.append("((pgsphere.line = spath(?)) OR (pgsphere.poly ~ spath(?)))");

                        sql_args.add(geom);
                        sql_args.add(geom);
                    } else if (type == Geometry.Type.POLYGON) {
                        sql.append("(pgsphere.poly ~ spoly(?))");

                        sql_args.add(geom);
                    }
                } else {

                }
            } else if (name.equals("covered-by")) {
                check_num_args(args, 1, name);
                SpatialReferenceSystem srs = get_srs(args[0], name);
                Location loc = (Location) args[0];

                if (srs.isGeospatialCoordinateSystem()) {
                    sql.append("ST_CoveredBy(ST_Transform(postgis.loc, ?), ST_GeomFromEWKT(?))");

                    add_postgis_srid_location_args(srs, loc, name);
                } else if (srs.isEquatorialCoordinateSystem()) {
                    Geometry.Type type = loc.getGeometry().getType();
                    String geom = PgsphereUtil.convertGeometry(loc
                            .getGeometry());

                    if (type == Geometry.Type.POINT) {
                        sql.append("(pgsphere.point = spoint(?))");

                        sql_args.add(geom);
                    } else if (type == Geometry.Type.LINE) {
                        sql.append("((pgsphere.point @ spath(?)) OR (pgsphere.line = spath(?))) ");

                        sql_args.add(geom);
                        sql_args.add(geom);
                    } else if (type == Geometry.Type.POLYGON) {
                        sql.append("((pgsphere.point @ spoly(?)) OR (pgsphere.line @ spoly(?)) OR (pgsphere.poly @ spoly(?)))");

                        sql_args.add(geom);
                        sql_args.add(geom);
                        sql_args.add(geom);
                    }
                }
            } else if (name.equals("intersects")) {
                check_num_args(args, 1, name);
                SpatialReferenceSystem srs = get_srs(args[0], name);
                Location loc = (Location) args[0];

                if (srs.isGeospatialCoordinateSystem()) {
                    sql.append("ST_Intersects(ST_Transform(postgis.loc, ?), ST_GeomFromEWKT(?))");

                    add_postgis_srid_location_args(srs, loc, name);
                } else if (srs.isEquatorialCoordinateSystem()) {
                    Geometry.Type type = loc.getGeometry().getType();
                    String geom = PgsphereUtil.convertGeometry(loc
                            .getGeometry());

                    if (type == Geometry.Type.POINT) {
                        sql.append("((pgsphere.point = spoint(?)) OR (pgsphere.line ~ spoint(?)) OR (pgsphere.poly ~ spoint(?)))");

                        sql_args.add(geom);
                        sql_args.add(geom);
                        sql_args.add(geom);
                    } else if (type == Geometry.Type.LINE) {
                        sql.append("((pgsphere.point @ spath(?)) OR (pgsphere.line && spath(?)) OR (pgsphere.poly && spath(?)))");

                        sql_args.add(geom);
                        sql_args.add(geom);
                        sql_args.add(geom);
                    } else if (type == Geometry.Type.POLYGON) {
                        sql.append("((pgsphere.point @ spoly(?)) OR (pgsphere.line && spoly(?)) OR (pgsphere.poly && spoly(?)))");

                        sql_args.add(geom);
                        sql_args.add(geom);
                        sql_args.add(geom);
                    }
                }
            } else if (name.equals("within")) {
                check_num_args(args, 2, name);
                SpatialReferenceSystem srs = get_srs(args[0], name);
                Location loc = (Location) args[0];

                if (srs.isGeospatialCoordinateSystem()) {
                    sql.append("ST_DWithin(ST_Transform(postgis.loc, ?), ST_GeomFromEWKT(?), ?)");

                    add_postgis_srid_location_args(srs, loc, name);
                    add_string_double_arg(args[1], name);
                } else if (srs.isEquatorialCoordinateSystem()) {
                    
                    // PGsphere only supports testing distances between points
                    // As a hack always treat argument geometry as the first
                    // point. Treat indexed lines as the first point.
                    // No way to handle indexed polygons in a simple way.

                    String geom = PgsphereUtil.convertGeometry(asPoint(loc
                            .getGeometry()));

                    sql.append("(((pgsphere.point <-> spoint(?)) < ?) OR ((spoint(pgsphere.line, 0) <-> spoint(?)) < ?))");

                    sql_args.add(geom);
                    add_string_degrees_as_radians_arg(args[1], name);
                    sql_args.add(geom);
                    add_string_degrees_as_radians_arg(args[1], name);
                }
            } else if (name.equals("datetime-intersects")) {
                check_num_args(args, 2, name);

                sql.append("((? <= time_interval.time_start) AND (? > time_interval.time_start)) OR ((? < time_interval.time_end) AND (? > time_interval.time_start))");

                add_string_long_arg(args[0], name);
                add_string_long_arg(args[1], name);
                add_string_long_arg(args[0], name);
                add_string_long_arg(args[1], name);
            } else if (name.equals("datetime-covered-by")) {
                check_num_args(args, 2, name);

                sql.append("(time_interval.time_start >= ?) AND (time_interval.time_end <= ?)");

                add_string_long_arg(args[0], name);
                add_string_long_arg(args[1], name);
            } else if (name.equals("datetime-covers")) {
                check_num_args(args, 2, name);

                sql.append("(time_interval.time_start <= ?) AND (time_interval.time_end >= ?)");

                add_string_long_arg(args[0], name);
                add_string_long_arg(args[1], name);
            } else {
                throw new QueryServiceException("Unknown function: " + name);
            }

            sql.append(')');
        }
    }

    public void clear() {
        sql.setLength(0);
        sql_args.clear();
    }

    private void check_num_args(Object[] args, int num, String func)
            throws QueryServiceException {
        if (args.length != num) {
            throw new QueryServiceException("Wrong # arguments to " + func
                    + " expecting " + num);
        }
    }

    private SpatialReferenceSystem get_srs(Object arg, String func)
            throws QueryServiceException {
        if (!(arg instanceof Location)) {
            throw new QueryServiceException("Expecting location argument to "
                    + func);
        }

        Location loc = (Location) arg;

        return SpatialReferenceSystem.lookup(loc.getSrid());
    }

    private void add_string_arg(Object arg, String func)
            throws QueryServiceException {
        if (!(arg instanceof String)) {
            throw new QueryServiceException("Expecting string argument to "
                    + func);
        }

        sql_args.add(arg);
    }

    private void add_string_double_arg(Object arg, String func)
            throws QueryServiceException {
        if (!(arg instanceof String)) {
            throw new QueryServiceException(
                    "Expecting string double argument to " + func);
        }

        try {
            sql_args.add(new Double((String) arg));
        } catch (NumberFormatException e) {
            throw new QueryServiceException(
                    "String argument must be a double: " + arg);
        }
    }

    private void add_string_degrees_as_radians_arg(Object arg, String func)
            throws QueryServiceException {
        if (!(arg instanceof String)) {
            throw new QueryServiceException(
                    "Expecting string double argument to " + func);
        }

        try {
            double value = new Double((String) arg);
            value = Math.toRadians(value);
            sql_args.add(value);
        } catch (NumberFormatException e) {
            throw new QueryServiceException(
                    "String argument must be a double: " + arg);
        }
    }

    // Add postgis srid and geometry arguments for a location
    private void add_postgis_srid_location_args(SpatialReferenceSystem srs,
            Location loc, String name) throws QueryServiceException {

        int srid;

        try {
            srid = db.lookupPostgisSrid(srs.authorityName(), srs.authorityId());
        } catch (SQLException e) {
            throw new QueryServiceException("Error looking up srid", e);
        }

        if (srid == -1) {
            throw new QueryServiceException("Could not find postgis srid for "
                    + loc.getSrid());
        }

        sql_args.add(srid);
        sql_args.add(PostgisUtil.convertGeometryToEwkt(loc.getGeometry(), srid));
    }

    private void add_string_long_arg(Object arg, String func)
            throws QueryServiceException {
        if (!(arg instanceof String)) {
            throw new QueryServiceException("Expecting string argument to "
                    + func);
        }

        try {
            sql_args.add(new Long((String) arg));
        } catch (NumberFormatException e) {
            throw new QueryServiceException("String argument must be a long: "
                    + arg);
        }
    }
}
