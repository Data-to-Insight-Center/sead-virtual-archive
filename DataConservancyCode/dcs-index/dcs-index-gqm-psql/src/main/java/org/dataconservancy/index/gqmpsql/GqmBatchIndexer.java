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
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.model.gqm.GQM;

class GqmBatchIndexer implements BatchIndexer<GQM> {
    private final DatabaseSession db;

    public GqmBatchIndexer(DatabaseSession db) throws IndexServiceException {
        this.db = db;
    }

    /**
     * Removes GQM instances associated with the given entity.
     */
    public void remove(String entity_id) throws IndexServiceException,
            UnsupportedOperationException {
        try {
            db.deleteByEntity(entity_id);
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        }
    }

    public void add(GQM gqm) throws IndexServiceException {
        try {
            db.insert(gqm);
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        }
    }

    public void close() throws IndexServiceException {
        try {
            db.close();
        } catch (SQLException e) {
            throw new IndexServiceException(e);
        }
    }
}
