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
package org.dataconservancy.ui.services;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.ui.util.SolrQueryUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Implementation of Parent Searcher which uses the DCS Connector and Solr queries to obtain parents.
 */
public class ArchiveParentSearcherImpl extends BaseArchiveSearcher implements ParentSearcher {

    ArchiveParentSearcherImpl(DcsConnector connector) {
        super(connector);
    }

    @Override
    public Collection<DcsEntity> getParentsOf(String entityId) {
        // TODO: Refactor this out into a "QuerySource" interface or some such.
        String query = SolrQueryUtil.createLiteralQuery("parent", entityId);

        return performSearch(query);
    }

    @Override
    public <T extends DcsEntity> Collection<T> getParentsOf(String entityId, Class<T> constraint) {
        final String query;

        Collection<DcsEntity> results;

        if (constraint == DcsDeliverableUnit.class) {
            query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "parent", entityId);
            results = performSearch(query);
        } else if (constraint == DcsCollection.class) {
            query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "Collection", "parent", entityId);
            results = performSearch(query);
        } else {
            results = Collections.emptyList();
        }

        Collection<T> constrainedResults = new HashSet<T>(results.size());

        for (DcsEntity e : results) {
            if (e.getClass() == constraint) {
                constrainedResults.add(constraint.cast(e));
            }
        }

        return constrainedResults;
    }
}
