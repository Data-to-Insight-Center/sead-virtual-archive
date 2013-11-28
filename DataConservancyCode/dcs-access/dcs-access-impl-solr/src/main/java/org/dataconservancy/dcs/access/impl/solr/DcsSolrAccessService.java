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
package org.dataconservancy.dcs.access.impl.solr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.core.CoreContainer;

import org.xml.sax.SAXException;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.dcs.access.api.AccessService;
import org.dataconservancy.dcs.access.api.IndexWriter;
import org.dataconservancy.dcs.access.api.SearchResult;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr.DcpIndexService}
 */

@Deprecated 
public class DcsSolrAccessService
        implements AccessService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DcsSolrIndex index;

    private final ArchiveStore store;

    // only used by embedded
    private final CoreContainer container;

    /**
     * Returns an AccessService attached to the SolrServer specified by the
     * system property solr.solr.home. If solr home is a full url, it connects
     * to the solr server running on that url. Otherwise solr home is treated as
     * a file path and an embedded solr server is started.
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public DcsSolrAccessService()
            throws IOException, ParserConfigurationException, SAXException {
        String solrhome = System.getProperty("solr.solr.home");

        if (solrhome == null) {
            throw new IOException("System property solr.solr.home must be set to a url or local path");
        }

        boolean embedded = true;

        try {
            if (new URL(solrhome).getHost() != null) {
                embedded = false;
            }
        } catch (MalformedURLException e) {
        }

        SolrServer server;

        if (embedded) {
            container = new CoreContainer.Initializer().initialize();
            server = new EmbeddedSolrServer(container, "");
        } else {
            container = null;

            CommonsHttpSolrServer httpserv =
                    new CommonsHttpSolrServer(solrhome);
            httpserv.setAllowCompression(true);
            server = httpserv;
        }

        this.index = new DcsSolrIndex(server);
        this.store = null;
    }

    public DcsSolrAccessService(SolrServer server, ArchiveStore store)
            throws IOException {

        this.index = new DcsSolrIndex(server);
        this.store = store;
        this.container = null;
    }

    public DcsSolrAccessService(File solrhome, ArchiveStore store)
            throws IOException, ParserConfigurationException, SAXException {
        System.setProperty("solr.solr.home", solrhome.getCanonicalPath());

        this.container = new CoreContainer.Initializer().initialize();
        this.index = new DcsSolrIndex(new EmbeddedSolrServer(container, ""));
        this.store = store;
    }

    public SearchResult search(String query,
                               int offset,
                               int maxmatches,
                               String... params) throws IOException {
        return index.search(query, offset, maxmatches, params);
    }

    public DcsEntity getEntity(String id) throws IOException {
        return index.getEntity(id);
    }

    public InputStream getDatastream(String id) {
        try {
            return store.getContent(id);
        } catch (EntityNotFoundException e) {
            return null;
        } catch (EntityTypeException e) {
            return null;
        }
    }

    public IndexWriter updateIndex() throws IOException {
        return index.update(store);
    }

    public void clearIndex() throws IOException {
        index.clear();
    }

    public void optimizeIndex() throws IOException {
        index.optimize();
    }

    // TODO doesn't really belong here, move to another class?

    /**
     * Add all of the entities in the archive to the index.
     * 
     * @throws IOException
     */
    public void indexArchive() throws IOException {
        IndexWriter writer = index.update(store);

        DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

        indexArchive(EntityType.COLLECTION, builder, writer);
        indexArchive(EntityType.EVENT, builder, writer);
        indexArchive(EntityType.DELIVERABLE_UNIT, builder, writer);
        indexArchive(EntityType.FILE, builder, writer);
        indexArchive(EntityType.MANIFESTATION, builder, writer);

        logger.info("Doing second pass");

        writer.close();
    }

    private void indexArchive(EntityType type,
                              DcsModelBuilder builder,
                              IndexWriter writer) throws IOException {
        logger.info("Indexing " + type);

        Iterator<String> ids = store.listEntities(type);

        while (ids.hasNext()) {
            String privateid = ids.next();

            try {
                InputStream is = store.getPackage(privateid);
                Dcp dcp = builder.buildSip(is);

                DcsEntity entity;

                if (type == EntityType.COLLECTION) {
                    entity = dcp.getCollections().iterator().next();
                } else if (type == EntityType.DELIVERABLE_UNIT) {
                    entity = dcp.getDeliverableUnits().iterator().next();
                } else if (type == EntityType.EVENT) {
                    entity = dcp.getEvents().iterator().next();
                } else if (type == EntityType.FILE) {
                    entity = dcp.getFiles().iterator().next();
                } else if (type == EntityType.MANIFESTATION) {
                    entity = dcp.getManifestations().iterator().next();
                } else {
                    throw new IOException("Unknown entity type " + type);
                }

                writer.add(entity);
            } catch (EntityNotFoundException e) {
                logger.error("Indexing " + privateid, e);
            } catch (InvalidXmlException e) {
                logger.error("Indexing " + privateid, e);
            }
        }
    }

    public void shutdown() {
        if (container != null) {
            container.shutdown();
        }
    }

    public long getEntityLastModified(String id) throws IOException {
        return index.getEntityLastModified(id);
    }
}
