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
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

public class SeadSolrService extends SolrService{


    public SeadSolrService(SolrServer server) throws IOException, ParserConfigurationException, SAXException {
        super(server);
    }

    public SeadSolrService() throws IOException, SAXException, ParserConfigurationException {
      super();
    }

    public SeadSolrService(File solrhome) throws IOException, ParserConfigurationException, SAXException {
        super(solrhome);
    }

    @Override
    public DcsEntity lookupEntity(String id) throws IOException,
            SolrServerException {
        SolrDocument doc = lookupSolrDocument(id);
        if (doc == null) {
            return null;
        }
        return SeadSolrMapper.fromSolr(doc);
    }

    @Override
    public DcsEntity asEntity(SolrDocument doc) throws IOException {
        return SeadSolrMapper.fromSolr(doc);
    }

    @Override
    public QueryResponse search(String query, int offset, int matches,
            String... params) throws SolrServerException {
        SolrQuery q = new SolrQuery(query);

        q.setStart(offset);
        q.setRows(matches);

        for (int i = 0; i < params.length;) {
            System.out.println(i+" "+params[i]+" length="+params.length);

            if(params[i].equalsIgnoreCase("facet.field"))     //if condition checks and enables faceted search
            {
                i++;
                String[] facets = params[i++].split(",");
                for(int j=0;j<facets.length;j++) {
                    System.out.println(i+" "+j+" "+facets[j]);
                    q.addFacetField(facets[j]);
                }
            }
            if(i==params.length)
                break;
            if(params[i].equalsIgnoreCase("sort"))     //if condition checks and enables sort
            {
                i++;
                String[] sortFields = params[i++].split(",");
                for(int j=0;j<sortFields.length;j++){

                    j++;
                    if(sortFields[j].equalsIgnoreCase("asc"))
                        q.addSortField(sortFields[j-1], SolrQuery.ORDER.asc);
                    else
                        q.addSortField(sortFields[j-1], SolrQuery.ORDER.desc);
                }
            }
            else
            {
                String name = params[i++];
                String val = params[i++];

                q.setParam(name, val);
            }
        }
        QueryResponse resp = server().query(q);
        return resp;
    }


}
