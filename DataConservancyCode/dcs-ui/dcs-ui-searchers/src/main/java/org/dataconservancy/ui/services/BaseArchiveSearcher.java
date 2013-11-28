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

import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.model.dcs.DcsEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Base class for {@code Archive*SearcherImpl}s.  Archive searchers that extend this class will receive a configured
 * instance of {@code DcsConnector} for communication with the archive.
 */
public abstract class BaseArchiveSearcher extends BaseSearcher {

    /**
     * The maximum number of results to obtain from the DCS data model query endpoint
     */
    static final int DEFAULT_MAX_RESULTS = Integer.MAX_VALUE;

    /**
     * The offset into the maximum number of results
     */
    static final int DEFAULT_OFFSET = 0;

    /**
     * A fully configured {@link DcsConnector} instance, ready to communicate with the DCS archive.  Meant to be used
     * by subclasses for performing searches.
     */
    final DcsConnector connector;

    BaseArchiveSearcher(DcsConnector connector) {
        this.connector = connector;
    }

    /**
     * Performs a search of the archive using the {@code DcsConnector} and the supplied {@code query} string.
     * Practically this means that the query is a Solr query, executed using the connector against the DCS data model
     * search endpoint.
     * <p/>
     * Note that the query is executed with an offset of {@link #DEFAULT_OFFSET} and a maximum number of results set
     * to {@link #DEFAULT_MAX_RESULTS}.
     *
     * @param query the query to execute
     * @return a {@code Collection} of DCS entities that match the query.  May be empty, but never {@code null}.
     */
    Collection<DcsEntity> performSearch(String query) {
        final CountableIterator<DcsEntity> itr;

        try {
            itr = connector.search(query, DEFAULT_MAX_RESULTS, DEFAULT_OFFSET);
        } catch (DcsConnectorFault e) {
            log.info(String.format(ERR_PERFORMING_SEARCH, query, e.getMessage()), e);
            return Collections.emptyList();
        }

        Collection<DcsEntity> results = new HashSet<DcsEntity>();

        while (itr.hasNext()) {
            results.add(itr.next());
        }

        return results;
    }
}
