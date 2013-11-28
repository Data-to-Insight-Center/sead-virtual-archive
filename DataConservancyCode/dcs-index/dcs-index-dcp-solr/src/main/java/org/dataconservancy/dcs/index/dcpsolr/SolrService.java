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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;

import org.xml.sax.SAXException;

import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for providing access to a SolrServer. It includes helper functions for
 * accessing the index. The shutdown method must be called when the SolrServer
 * is no longer needed.
 * <p/>
 * Note that {@link org.dataconservancy.dcs.index.dcpsolr.SolrService#SolrService()} and
 * {@link SolrService#SolrService(org.apache.solr.client.solrj.SolrServer)} require
 * the system property <code>solr.solr.home</code> to be set by the client, otherwise an
 * <code>IllegalStateException</code> is thrown.  The remaining constructor, {@link SolrService#SolrService(java.io.File)},
 * will set the <code>solr.solr.home</code> system property to the supplied <code>File</code>, overwriting existing
 * values for <code>solr.solr.home</code>.
 */

public class SolrService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Create a local Solr install in the given directory.
     *
     * @throws IOException
     */
    public static void createSolrInstall(File solrhome) throws IOException {
        // Cannot list resources so have keep names here
        String[] filenames = new String[] { "elevate.xml",
                "mapping-ISOLatin1Accent.txt", "protwords.txt",
                "solrconfig.xml", "spellings.txt", "stopwords.txt",
                "synonyms.txt", "schema.xml" };

        File confdir = new File(solrhome, "default/conf");
        confdir.mkdirs();

        for (String name : filenames) {
            OutputStream os = new FileOutputStream(new File(confdir, name));
            InputStream is = SolrService.class
                    .getResourceAsStream("default/conf/" + name);
            FileUtil.copy(is, os);
            is.close();
            os.close();
        }

        OutputStream os =
                FileUtils.openOutputStream(new File(solrhome, "solr.xml"));
        InputStream is = SolrService.class.getResourceAsStream("solr.xml");
        FileUtil.copy(is, os);
        is.close();
        os.close();

    }

    private final CoreContainer container; // only used by embedded
    private final SolrServer server;

    /**
     * Returns an SolrService attached to the SolrServer specified by the system
     * property solr.solr.home. If solr home is a full url, it connects to the
     * solr server running on that url. Otherwise solr home is treated as a file
     * path and an embedded solr server is started.
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IllegalStateException if the <code>solr.solr.home</code> System property is not set
     */
    public SolrService() throws IOException, ParserConfigurationException,
            SAXException {
        String solrhome = System.getProperty("solr.solr.home");

        if (solrhome == null) {
            throw new IllegalStateException(
                    "System property solr.solr.home must be set to a url or local path");
        }

        boolean embedded = true;

        try {
            if (new URL(solrhome).getHost() != null) {
                embedded = false;
            }
        } catch (MalformedURLException e) {
        }

        if (embedded) {
            final File solrXml = new File(solrhome, "solr.xml");
            log.debug("Instantiating new SolrService (EmbeddedSolrServer) with solr.solr.home '{}', solr.xml '{}'", solrhome, solrXml);
            this.container = new CoreContainer(solrhome, solrXml);
            this.server = new EmbeddedSolrServer(container, "default");
        } else {
            this.container = null;
            log.debug("Instantiating new SolrService (CommonsHttpSolrServer) with solr.solr.home '{}'", solrhome);
            CommonsHttpSolrServer httpserv = new CommonsHttpSolrServer(solrhome);
            httpserv.setAllowCompression(true);
            this.server = httpserv;
        }
    }

    /**
     * Constructs a SolrService backed by the supplied <code>server</code>.
     *
     * @param server the Solr server
     */
    public SolrService(SolrServer server) {
        log.debug("Instantiating new SolrService ({})", server);
        this.server = server;
        this.container = null;
    }

    /**
     * Instantiates a SolrService backed by an EmbeddedSolrServer.  Unlike the other constructors for SolrService,
     * this one does not expect a <code>solr.solr.home</code> system property to be set; it will set the property.
     * The provided <code>solrhome</code> is used to set the system property <code>solr.solr.home</code>.  Existing
     * (if any) values for the property <code>solr.solr.home</code> are overwritten.
     *
     * @param solrhome the path to the Solr home directory
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public SolrService(File solrhome) throws IOException,
            ParserConfigurationException, SAXException {

        /* Not strictly necessary... */
        System.setProperty("solr.solr.home", solrhome.getCanonicalPath());

        final File solrXml = new File(solrhome, "solr.xml");
        log.debug("Instantiating new SolrService (EmbeddedSolrServer) with solr.solr.home '{}', solr.xml '{}'", solrhome, solrXml);
        this.container = new CoreContainer(solrhome.getCanonicalPath(), solrXml);
        this.server = new EmbeddedSolrServer(container, "default");
    }

    public SolrServer server() {
        return server;
    }

    /**
     * @param id
     * @return SolrDocument with the given identifier or null if the
     *         SolrDocument is not in the index.
     * @throws SolrServerException
     * @throws IOException
     */
    public SolrDocument lookupSolrDocument(String id) throws IOException,
            SolrServerException {
        if (id == null) {
            return null;
        }

        SolrQuery q = new SolrQuery(SolrQueryUtil.createLiteralQuery(
                EntityField.ID.solrName(), id));

        QueryResponse resp = server.query(q);

        SolrDocumentList result = resp.getResults();

        if (result.getNumFound() == 0) {
            return null;
        } else if (result.getNumFound() > 1) {
            throw new IOException("More than one entity with id: " + id);
        } else {
            return result.get(0);
        }
    }

    /**
     * @param id
     * @return Entity with the given identifier or null if the entity is not in
     *         the index.
     * @throws SolrServerException
     * @throws IOException
     */
    public DcsEntity lookupEntity(String id) throws IOException,
            SolrServerException {
        SolrDocument doc = lookupSolrDocument(id);

        if (doc == null) {
            return null;
        }
        return DcsSolrMapper.fromSolr(doc);
    }

    /**
     * Return the date of the last event targeting this entity.
     *
     * @param id
     *            entity id
     * @return ingest date or -1
     * @throws IOException
     * @throws SolrServerException
     */
    public long lookupEntityLastModified(String id) throws IOException,
            SolrServerException {
        SolrDocument doc = lookupSolrDocument(id);

        if (doc == null) {
            return -1;
        }

        Collection<Object> dates = doc
                .getFieldValues(DcsSolrField.EventField.DYNAMIC_DATE_TYPE_PREFIX
                        .solrName() + "ingest.complete");

        Date last = null;

        if (dates != null) {
            for (Object o : dates) {
                last = (Date) o;
            }
        }
        return last == null ? -1 : last.getTime();
    }

    /**
     * @return number of documents in the index
     * @throws SolrServerException
     */
    public long size() throws SolrServerException {
        SolrQuery q = new SolrQuery("*:*");
        q.setRows(0);

        return server().query(q).getResults().getNumFound();
    }

    public DcsEntity asEntity(SolrDocument doc) throws IOException {
        return DcsSolrMapper.fromSolr(doc);
    }

    /**
     * @param query
     * @param offset
     * @param matches
     * @param params
     *            name,value pairs of Solr params
     * @return
     * @throws SolrServerException
     */
    public QueryResponse search(String query, int offset, int matches,
            String... params) throws SolrServerException {
        SolrQuery q = new SolrQuery(query);

        q.setStart(offset);
        q.setRows(matches);

        for (int i = 0; i < params.length;) {
            String name = params[i++];
            String val = params[i++];

            q.setParam(name, val);
        }

        return server.query(q);
    }

    public void shutdown() {
        if (container != null) {
            container.shutdown();
        }
    }
}
