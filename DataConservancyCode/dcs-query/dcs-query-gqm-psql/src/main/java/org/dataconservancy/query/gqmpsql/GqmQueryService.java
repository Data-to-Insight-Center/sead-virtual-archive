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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.index.gqmpsql.DatabaseSession;
import org.dataconservancy.index.gqmpsql.DatabaseSessionPool;
import org.dataconservancy.query.gqmpsql.lang.model.Query;
import org.dataconservancy.query.gqmpsql.lang.parser.ParseException;
import org.dataconservancy.query.gqmpsql.lang.parser.Parser;

/**
 * Service that queries instances of the General Query Model indexed by a
 * postgres backend.
 * 
 * See https://wiki.library.jhu.edu/x/awHy for more information.
 */
public class GqmQueryService implements QueryService<GQM> {
    private static final int MAX_MATCHES = 10000;
    private final DatabaseSessionPool pool;

    public GqmQueryService(DatabaseSessionPool pool)
            throws QueryServiceException {
        this.pool = pool;
    }

    public GqmQueryService(String db_url, String db_driver_class,
            String db_user, String db_pass) throws QueryServiceException {
        try {
            this.pool = new DatabaseSessionPool(db_url, db_driver_class,
                    db_user, db_pass);
        } catch (SQLException e) {
            throw new QueryServiceException(e);
        }
    }

    public GqmQueryService() throws QueryServiceException {
        try {
            this.pool = new DatabaseSessionPool();
        } catch (SQLException e) {
            throw new QueryServiceException(e);
        }
    }

    public QueryResult<GQM> query(String query, long offset, int matches,
            String... params) throws QueryServiceException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if (offset > Integer.MAX_VALUE) {
            throw new QueryServiceException("offset too large");
        }

        if ((params.length & 1) > 0) {
            throw new IllegalArgumentException("parameter name without value");
        }

        if (matches <= 0 || matches > MAX_MATCHES) {
            matches = MAX_MATCHES;
        }

        DatabaseSession db = null;
        PreparedStatement stat = null;

        try {
            Query q = new Parser(query).parseQuery();

            db = pool.connect();

            SqlQueryTransformer tr = new SqlQueryTransformer(db);
            tr.transform(q);

            // Get the total number of matches

            stat = tr.asQueryCountStatement(db);
            ResultSet rs = stat.executeQuery();

            if (!rs.next()) {
                throw new QueryServiceException(
                        "Unable to count query matches.");
            }

            long total = rs.getLong(1);
            stat.close();

            if (offset > total) {
                offset = total;
            }

            // Execute the actual query

            QueryResult<GQM> result = new QueryResult<GQM>(offset, total, query, params);

            stat = tr.asQueryStatement(db, offset, matches);
            rs = stat.executeQuery();

            while (rs.next()) {
                GQM gqm = db.lookup(rs.getLong(1));
                result.getMatches().add(new QueryMatch<GQM>(gqm, null));
            }

            return result;
        } catch (ParseException e) {
            throw new QueryServiceException(e);
        } catch (SQLException e) {
            throw new QueryServiceException(e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (SQLException e) {
                    throw new QueryServiceException(e);
                }
            }

            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    throw new QueryServiceException(e);
                }
            }
        }
    }

    public void shutdown() throws QueryServiceException {
    }
}
