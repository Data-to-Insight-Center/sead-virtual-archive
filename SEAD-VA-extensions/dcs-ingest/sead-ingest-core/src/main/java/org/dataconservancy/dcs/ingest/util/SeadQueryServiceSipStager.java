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
package org.dataconservancy.dcs.ingest.util;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.seadva.ingest.SeadSipStager;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.util.Set;

import static org.dataconservancy.dcs.index.solr.support.SolrQueryUtil.createLiteralQuery;

/**
 * Reads and re-builds sips using the access service index.
 * <p>
 * Read-only sip stager that builds sips by searching for all entities that
 structed.</dd>
 * </dl>
 * </p>
 */
public class SeadQueryServiceSipStager
        implements SipStager {

    private QueryService<DcsEntity> access;

    @Required
    public void setAccessService(QueryService<DcsEntity> as) {
        access = as;
    }

    public String addSIP(ResearchObject sip) {
        /* We use the access service for reads only */
        throw new UnsupportedOperationException();
    }

    public Set<String> getKeys() {
        /* This will be a very large set, punting */
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSIP(Dcp sip, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String addSIP(Dcp sip) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a Dcp of entities having an INGEST_SUCCESS event with an outcome =
     * $id
     */
    public ResearchObject getSIP(String id) {
        try {
            QueryResult<DcsEntity> matches =
                    access.query(createLiteralQuery("AND",
                                                    "eventType",
                                                    Events.INGEST_SUCCESS,
                                                    "eventOutcome",
                                                    id),
                                 0,
                                 1024);
            if (matches.getTotal() < 1) {
                return null; /* no results */
            }

            ResearchObject result = new ResearchObject();

            for (QueryMatch<DcsEntity> m : matches.getMatches()) {
                DcsEntity entity = m.getObject();

                /* It would be nice if we didn't have to know the type */
                if (entity instanceof DcsCollection) {
                    result.addCollection((DcsCollection) entity);
                } else if (entity instanceof DcsDeliverableUnit) {
                    result.addDeliverableUnit((DcsDeliverableUnit) entity);
                } else if (entity instanceof DcsEvent) {
                    result.addEvent((DcsEvent) entity);
                } else if (entity instanceof DcsFile) {
                    result.addFile((DcsFile) entity);
                } else if (entity instanceof DcsManifestation) {
                    result.addManifestation((DcsManifestation) entity);
                }
            }
            return result;
        } catch (QueryServiceException e) {
            throw new RuntimeException("Error executing search", e);
        }
    }

    public void removeSIP(String id) {
        /* We use the access service for reads only */
        throw new UnsupportedOperationException();
    }

    public void retire(String id) {
        /* We use the access service for reads only */
        throw new UnsupportedOperationException();
    }

    public void updateSIP(ResearchObject sip, String id) {
        /* We use the access service for reads only */
        throw new UnsupportedOperationException();
    }
}
