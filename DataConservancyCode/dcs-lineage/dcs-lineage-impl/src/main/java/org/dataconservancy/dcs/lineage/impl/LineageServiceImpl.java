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
package org.dataconservancy.dcs.lineage.impl;


import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LineageServiceImpl implements LineageService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final static int MAX_MATCHES = 10000;
    SolrService solrService;
    IdService idService;

    public LineageServiceImpl(SolrService solrService, IdService idService) {
        if (solrService == null) {
            throw new IllegalArgumentException("SolrService must not be null.");
        }
        if (idService == null) {
            throw new IllegalArgumentException("IdService must not be null.");
        }
        this.solrService = solrService;
        this.idService = idService;
    }

    public Lineage getLineage(String id) {
        Identifier identifier = null;
        String lineageId;
        Lineage lineage = null;

        try {
            identifier = idService.fromUrl(new URL(id));
        } catch (IdentifierNotFoundException e) {
            log.error("Unable to obtain identifier from the IdService for URL " + id);
            return null;
        } catch (MalformedURLException e) {
            try {
                identifier = idService.fromUid(id);
            } catch (IdentifierNotFoundException e1) {
                log.error("Unable to obtain identifier from the IdService for string " + id);
                return null;
            }
        }
        
        if (Types.DELIVERABLE_UNIT.getTypeName().equals(identifier.getType())) {
            String type = identifier.getType();
            DcsEntity entity = null;
            try {
                entity = solrService.lookupEntity(id);
            } catch (IOException e) {
                log.error("IO error looking up entity for id " + id, e);
            } catch (SolrServerException e) {
                log.error("SolrService error looking up  entity for id " + id, e);
            }
            if (entity instanceof DcsDeliverableUnit) {// just in case
                DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
                lineageId = du.getLineageId();
                lineage = getLineageForLineageId(lineageId);
            }
        } else if (Types.LINEAGE.getTypeName().equals(identifier.getType())) {
            lineage = getLineageForLineageId(id);
        } else {
            lineage = null;
        }
        return lineage;
    }

    public Lineage getLineageForEntityRange(String first_entity_id, String second_entity_id) {
        Lineage lineage;
        if (null != first_entity_id) {
            lineage = getLineage(first_entity_id);
        } else if (null != second_entity_id) {
            lineage = getLineage(second_entity_id);
        } else {
            lineage = null;
        }

        if (null != lineage) {
            if (first_entity_id == null) {
                first_entity_id = lineage.getOldest().getEntityId();
            }
            if (second_entity_id == null) {
                second_entity_id = lineage.getNewest().getEntityId();
            }
            Iterator iter = lineage.iterator();
            List<LineageEntry> entryList = new ArrayList<LineageEntry>();
            boolean haveSecondEntity = false;
            while (iter.hasNext()) {
                LineageEntry entry = (LineageEntry) iter.next();
                if (entry.getEntityId().equals(second_entity_id)) {
                    haveSecondEntity = true;
                }
                if (haveSecondEntity == true) {
                    entryList.add(entry);
                }
                if (entry.getEntityId().equals(first_entity_id)) {
                    if (entryList.size() > 0) {//we have both entities
                        Lineage entityLineage = new LineageImpl(entryList);
                        return entityLineage;
                    }
                    break;
                }

            }

        }
        //return null if: the original lineage was null
        //                there is nothing in the entryList, which happens when the entities are in the wrong order
        //                both entities are not in the same lineage
        return null;
    }

    public Lineage getLineageForDateRange(String entity_id, long startDate, long endDate) {
        Lineage lineage = getLineage(entity_id);
        if (null != lineage) {
            if (startDate < 0) {
                startDate = lineage.getOldest().getEntryTimestamp();
            }
            if (endDate < 0) {
                endDate = lineage.getNewest().getEntryTimestamp();
            }
            if (endDate >= startDate) {//"normal" case - have a lineage, startDate <= endDate; return a non-null Lineage
                Iterator iter = lineage.iterator();
                List<LineageEntry> entryList = new ArrayList<LineageEntry>();
                while (iter.hasNext()) {
                    LineageEntry entry = (LineageEntry) iter.next();
                    if ((endDate >= entry.getEntryTimestamp()) && (startDate <= entry.getEntryTimestamp())) {
                        entryList.add(entry);
                    }
                }
                Lineage dateLineage = new LineageImpl(entryList);
                return dateLineage;   //includes empty lineage case for date range
            } else { //end date < start date, return null
                return null;
            }
        }
        //have a null lineage, return null
        return null;
    }

    public LineageEntry getLatest(String entity_id) {
        Lineage lineage = getLineage(entity_id);
        if (null != lineage) {
            return lineage.getNewest();
        } else {
            return null;
        }
    }

    public LineageEntry getOriginal(String entity_id) {
        Lineage lineage = getLineage(entity_id);
        if (null != lineage) {
            return lineage.getOldest();
        } else {
            return null;
        }
    }


    public LineageEntry getEntryForDate(String entity_id, long date) {
        Lineage lineage = getLineage(entity_id);
        LineageEntry dateEntry = null;
        if (null != lineage) {
            Iterator iter = lineage.iterator();
            while (iter.hasNext()) {
                LineageEntry entry = (LineageEntry) iter.next();
                if (entry.getEntryTimestamp() <= date) { // starting from newest to oldest
                    dateEntry = entry;
                    break;
                }
            }
        }
        return dateEntry;
    }

    public boolean isLatest(String entity_id) {
        Lineage lineage = getLineage(entity_id);
        if (null != lineage && lineage.getNewest().getEntityId().equals(entity_id)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOriginal(String entity_id) {
        Lineage lineage = getLineage(entity_id);
        if (null != lineage && lineage.getOldest().getEntityId().equals(entity_id)) {
            return true;
        } else {
            return false;
        }
    }

    private Lineage getLineageForLineageId(String lineage_id) {
        Lineage lineage;
        QueryResponse response;
        SolrDocumentList docs = new SolrDocumentList();
        List<LineageEntry> entryList = new ArrayList<LineageEntry>();

        String query = SolrQueryUtil.createLiteralQuery(DcsSolrField.DeliverableUnitField.LINEAGE.solrName(), lineage_id);
        // we must anticipate having very large result sets
        // have tested this code with several small values of MAX_MATCHES
        try {
            int chunkSize = MAX_MATCHES;
            int offset = 0;
            String[] params = {"sort", DcsSolrField.EventField.DYNAMIC_DATE_TYPE_PREFIX.solrName() +
                    "ingest.complete" +
                    " desc"};  //get results in descending order time-wise (latest first)
            while (chunkSize == MAX_MATCHES) {
                response = solrService.search(query, offset, MAX_MATCHES, params);
                SolrDocumentList chunk = response.getResults();
                docs.addAll(chunk);
                offset += MAX_MATCHES;
                chunkSize = chunk.size();
            }
        } catch (SolrServerException e) {
            log.error("SolrService search error for query " + query, e);
        }

        //have query response
        if (!docs.isEmpty()) {
            for (SolrDocument doc : docs) {
                try {
                    DcsEntity entity = solrService.asEntity(doc);
                    if (entity instanceof DcsDeliverableUnit) {
                        DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
                        String duId = du.getId();
                        long timestamp = solrService.lookupEntityLastModified(duId);
                        LineageEntry lineageEntry = new LineageEntryImpl(duId, lineage_id, timestamp);
                        entryList.add(lineageEntry);
                    }
                } catch (IOException e) {
                    log.error("IO error using SolrService", e);
                } catch (SolrServerException e) {
                    log.error("SolrService error", e);
                }
            }
            lineage = (entryList.isEmpty()) ? null : new LineageImpl(entryList);
        } else {
            lineage = null;
        }
        return lineage;
    }

} 
