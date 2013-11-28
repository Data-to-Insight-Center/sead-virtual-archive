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
package org.dataconservancy.dcs.index.dcpsolr;

import org.apache.solr.client.solrj.SolrServerException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.seadva.model.pack.ResearchObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Index packages of DCS entities. If an ArchiveStore is not provided than the
 * contents of DcsFile entities will not be indexed.
 */

public class ROIndexService implements IndexService<ResearchObject>{


    private final SolrService solr;
    private final ArchiveStore store; // needed to retrieve DcsFile datastreams

    public ROIndexService(ArchiveStore store) throws IndexServiceException {
        try {
            this.solr = new SolrService();
            this.store = store;
        } catch (IOException e) {
            throw new IndexServiceException(e);
        } catch (ParserConfigurationException e) {
            throw new IndexServiceException(e);
        } catch (SAXException e) {
            throw new IndexServiceException(e);
        }
    }

    public ROIndexService(ArchiveStore store, SolrService solr) {
        this.store = store;
        this.solr = solr;
    }

    public ROIndexService(SolrService solr) {
        this.store = null;
        this.solr = solr;
    }

    public BatchIndexer<ResearchObject> index() throws IndexServiceException {
        return new ROBatchIndexer(solr, store);
    }

    public void clear() throws IndexServiceException {
        try {
            solr.server().deleteByQuery("*:*");
            solr.server().commit();
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        }
    }

    public void optimize() throws IndexServiceException {
        try {
            solr.server().optimize();
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        }
    }

    public long size() throws IndexServiceException {
        try {
            return solr.size();
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        }
    }

    public void shutdown() {
        solr.shutdown();
    }

    /**
     * @param id
     * @return Entity with the given identifier or null if the entity is not in
     *         the index.
     * @throws IndexServiceException
     */
    public DcsEntity lookupEntity(String id) throws IndexServiceException {
        try {
            return solr.lookupEntity(id);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        }
    }

    /**
     * Return the date of the last event targeting this entity.
     *
     * @param id
     *            entity id
     * @return ingest date or -1
     * @throws IndexServiceException
     */
    public long lookupEntityLastModified(String id)
            throws IndexServiceException {
        try {
            return solr.lookupEntityLastModified(id);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        }
    }
}
