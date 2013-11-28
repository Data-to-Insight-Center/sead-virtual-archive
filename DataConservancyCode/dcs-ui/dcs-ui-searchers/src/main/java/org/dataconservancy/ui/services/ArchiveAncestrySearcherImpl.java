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
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.ui.util.SolrQueryUtil;

import java.util.Collection;

/**
 * Implementation of Ancestry Searcher which uses the DCS Connector and Solr queries to obtain ancestors.
 */
public class ArchiveAncestrySearcherImpl extends BaseArchiveSearcher implements AncestrySearcher {

    ArchiveAncestrySearcherImpl(DcsConnector connector) {
        super(connector);
    }

    @Override
    public Collection<DcsEntity> getAncestorsOf(String entityId, boolean inclusive) {
        // TODO: Refactor this out into a "QuerySource" interface or some such.
        final String query;

        if (inclusive) {
            query = SolrQueryUtil.createLiteralQuery("OR", "id", entityId, "ancestry", entityId);
        } else {
            query = SolrQueryUtil.createLiteralQuery("ancestry", entityId);
        }

        return performSearch(query);
    }

}
