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

import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.model.gqm.GQM;

/**
 * The index service provides a mechanism to index instances of the General
 * Query Model with a database. A session pool is used to manage sessions with
 * the underlying database. If the database is empty, the needed tables and
 * indexed are created automatically.
 */
public class GqmIndexService implements IndexService<GQM> {
    private final DatabaseSessionPool pool;

    public GqmIndexService(DatabaseSessionPool pool)
            throws IndexServiceException {
        this.pool = pool;
        setup();
    }

    public GqmIndexService(String db_url, String db_driver_class,
            String db_user, String db_pass) throws IndexServiceException {
        try {
            this.pool = new DatabaseSessionPool(db_url, db_driver_class,
                    db_user, db_pass);
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        }

        setup();
    }

    public GqmIndexService() throws IndexServiceException {
        try {
            this.pool = new DatabaseSessionPool();
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        }

        setup();
    }

    public BatchIndexer<GQM> index() throws IndexServiceException {
        try {
            return new GqmBatchIndexer(pool.connect());
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        }
    }

    public void clear() throws IndexServiceException {
        DatabaseSession db = null;

        try {
            db = pool.connect();
            db.clearTables();
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (SQLException e) {
                    throw new IndexServiceException(e);
                }
            }
        }
    }

    // TODO There is a race condition with multiple sessions trying to create tables at the same time
    private void setup() throws IndexServiceException {
        DatabaseSession db = null;

        try {
            db = pool.connect();

            if (!db.hasTables()) {
                db.createTables();
            }
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (SQLException e) {
                    throw new IndexServiceException(e);
                }
            }
        }
    }

    public void optimize() throws IndexServiceException {
        DatabaseSession db = null;

        try {
            db = pool.connect();
            db.analyze();
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (SQLException e) {
                    throw new IndexServiceException(e);
                }
            }
        }

    }

    public long size() throws IndexServiceException {
        DatabaseSession db = null;

        try {
            db = pool.connect();
            return db.size();
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (SQLException e) {
                    throw new IndexServiceException(e);
                }
            }
        }
    }

    public void shutdown() throws IndexServiceException {
    }
}
