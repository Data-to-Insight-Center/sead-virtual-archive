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

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.DataItemTransport;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the DataItemTransport business service which retrieves DataItemTransports
 * from an archive.
 */
public class DataItemTransportServiceImpl implements DataItemTransportService {
    private final ArchiveService archiveService;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public DataItemTransportServiceImpl(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @Override
    public List<DataItemTransport> retrieveDataItemTransportList(String currentCollectionId, int maxResultsPerPage, int offset)
            throws ArchiveServiceException, BizPolicyException {
        archiveService.pollArchive();
        ArchiveSearchResult<DataItem> results =
                archiveService
                        .retrieveDataSetsForCollection(getDepositId(currentCollectionId),
                                                       maxResultsPerPage,
                                                       offset);
       return this.retrieveDataItemTransportList(results);
    }

    public List<DataItemTransport> retrieveDataItemTransportList(ArchiveSearchResult<DataItem> dataItemList)
            throws ArchiveServiceException, BizPolicyException {
        //we now have a list of all DataItems which have been successfully deposited at some point
        List<DataItemTransport> dataItemTransportList = new ArrayList<DataItemTransport>();
        for (DataItem dataItem : dataItemList.getResults()) {
            DataItemTransport dataItemTransport = this.createDataItemTransport(dataItem);
            dataItemTransportList.add(dataItemTransport);
        }
        return dataItemTransportList;
    }

    @Override
    public DataItemTransport retrieveDataItemTransport(String dataItemId)
            throws ArchiveServiceException, BizPolicyException {
        DataItemTransport dataItemTransport = null;
        this.archiveService.pollArchive();
        ArchiveSearchResult<DataItem> dataItemSearchResult = this.archiveService.retrieveDataSet(this.getDepositId(dataItemId));
        if (dataItemSearchResult != null && !dataItemSearchResult.getResults().isEmpty()) {
            DataItem dataItem = (DataItem) dataItemSearchResult.getResults().toArray()[0];
            dataItemTransport = this.createDataItemTransport(dataItem);
        }
        return dataItemTransport;
    }

    @Override
    public String getDepositId(String object_id) {
        if (object_id == null || object_id.isEmpty()) {
            return null;
        }

        List<ArchiveDepositInfo> infoList =
                archiveService
                        .listDepositInfo(object_id, // null);
                                         ArchiveDepositInfo.Status.DEPOSITED);

        if (infoList == null || infoList.isEmpty()) {
            return null;
        }

        return infoList.get(0).getDepositId();
    }

    private DateTime retrieveInitialDepositDate(DataItem dataItem) {
        if (dataItem == null) {
            return null;
        }
        List<ArchiveDepositInfo> infoList =
                archiveService
                        .listDepositInfo(dataItem.getId(),
                                         ArchiveDepositInfo.Status.DEPOSITED);
        if (infoList == null || infoList.isEmpty()) {
            return null;
        }
        return infoList.get(infoList.size() - 1).getDepositDateTime();
    }

    private ArchiveDepositInfo.Status retrieveDepositStatus(DataItem dataItem)
            throws ArchiveServiceException {
        if (dataItem == null) {
            return null;
        }
        archiveService.pollArchive();
        List<ArchiveDepositInfo> infoList =
                archiveService.listDepositInfo(dataItem.getId(), null);
        if (infoList == null || infoList.isEmpty()) {
            return null;
        }
        return infoList.get(0).getDepositStatus();
    }


    private DataItemTransport createDataItemTransport(DataItem dataItem) throws ArchiveServiceException, BizPolicyException {
        DataItemTransport dataItemTransport = new DataItemTransport();
        dataItemTransport.setDataItem(dataItem);
        //we get the status of the most recent deposit attempt
        dataItemTransport
                .setDepositStatus(retrieveDepositStatus(dataItem));
        //find the initial successful deposit date
        dataItemTransport
                .setInitialDepositDate(retrieveInitialDepositDate(dataItem));
        return dataItemTransport;
    }
}
