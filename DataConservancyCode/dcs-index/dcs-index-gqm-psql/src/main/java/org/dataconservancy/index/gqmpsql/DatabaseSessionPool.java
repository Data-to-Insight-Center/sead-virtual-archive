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

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a pool of database sessions using DBCP. Each session must be closed.
 */

public class DatabaseSessionPool {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSessionPool.class);
    private final DataSource ds;

    public DatabaseSessionPool(String db_uri, String db_driver_class,
            String db_user, String db_pass) throws SQLException {

        try {
            Class.forName(db_driver_class);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        this.ds = setupDataSource(db_uri, db_user, db_pass);
    }

    private static String get_property(String name, String default_value) {
        String value = System.getProperty(name);

        if (value == null) {
            return default_value;
        }

        return value;
    }

    /**
     * Use system properties to configure a session pool.
     * 
     * The system properties dc.gqm.db.uri, dc.gqm.db.user, dc.gqm.db.pass, and
     * dc.gqm.db.driver are used if set. The defaults are respectively
     * jdbc:postgresql://localhost/gqm, gqm, gqm, and org.postgresql.Driver.
     */
    public DatabaseSessionPool() throws SQLException {
        this(get_property("dc.gqm.db.uri", "jdbc:postgresql://localhost/gqm"),
                get_property("dc.gqm.db.driver", "org.postgresql.Driver"),
                get_property("dc.gqm.db.user", "gqm"), get_property(
                        "dc.gqm.db.pass", "gqm"));
    }

    // TODO tune pool parameters?
    private static DataSource setupDataSource(String uri, String user,
            String pass) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Creating database session pool: ")
                    .append("URL: {} User: {} Password: {}");
            LOG.debug(sb.toString(),
                    new Object[]{uri, user, (pass != null && pass.trim().length() != 0) ? "****" : pass});
        }
        ConnectionFactory cf = new DriverManagerConnectionFactory(uri, user,
                pass);

        ObjectPool pool = new GenericObjectPool();
        new PoolableConnectionFactory(cf, pool, null, null, false, false);

        return new PoolingDataSource(pool);
    }

    /**
     * @return a new session with the database that must eventually be closed.
     * @throws SQLException
     */
    public DatabaseSession connect() throws SQLException {
        return new DatabaseSession(ds.getConnection());
    }
}
