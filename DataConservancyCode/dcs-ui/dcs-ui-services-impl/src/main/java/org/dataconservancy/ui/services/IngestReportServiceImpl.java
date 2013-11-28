/*
 * Copyright 2013 Johns Hopkins University
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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.builder.IngestReportBuilder;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Collection;

public class IngestReportServiceImpl implements IngestReportService {
    
    private IngestReportBuilder ingestReportBuilder;
    private IdService idService;
    private CollectionBizService collectionBizService;
    
    public IngestReportServiceImpl(IngestReportBuilder ingestReportBuilder) {
        if (ingestReportBuilder == null) {
            throw new RuntimeException("ingestReportBuilder cannot be null!");
        }
        this.ingestReportBuilder = ingestReportBuilder;
    }

    @Override
    public IngestReport buildIngestReport(InputStream is) throws InvalidXmlException {
        return ingestReportBuilder.buildIngestReport(is);
    }
    

    @Override
    public Map<String, Integer> getDataItemsPerCollectionCount(IngestReport ingestReport) {
        Map<String, Integer> dataItemsPerCollectionCount = new HashMap<String, Integer>();
        for (String key : ingestReport.getDataItemsPerCollectionCount().keySet()) {
            if (verifyCollectionId(key)) {
                // Need to lookup the existing collection via its ID.
                try {
                    Collection collection = retrieveCollection(key);
                    if (collection != null) {
                        dataItemsPerCollectionCount.put("'" + collection.getTitle() + "' (Existing)", ingestReport
                                .getDataItemsPerCollectionCount().get(key));
                    }
                }
                catch (BizPolicyException e) {
                    // do nothing
                }
                catch (BizInternalException e) {
                    // do nothing
                }
            }
            else {
                // New collection
                dataItemsPerCollectionCount.put("'" + key + "' (New)", ingestReport.getDataItemsPerCollectionCount()
                        .get(key));
            }
        }
        if (dataItemsPerCollectionCount.size() > 0) {
            return dataItemsPerCollectionCount;
        }
        else {
            return null;
        }
    }
    
    private Collection retrieveCollection(String id) throws BizPolicyException, BizInternalException {
        return collectionBizService.getCollection(id);
    }

    private boolean verifyCollectionId(String collection) {
        try {
            if (idService.fromUrl(new URL(collection)) != null) {
                // it's an ID, return true;
                return true;
            }
            else {
                return false;
            }
        }
        // return false in case of any exceptions as it means the ID doesn't exist.
        catch (MalformedURLException e) {
            return false;
        }
        catch (IdentifierNotFoundException e) {
            return false;
        }
    }

    /**
     * @param idService the idService to set
     */
    public void setIdService(IdService idService) {
        this.idService = idService;
    }

    /**
     * @param collectionBizService
     *            the collectionBizService to set
     */
    public void setCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }

}
