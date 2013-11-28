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
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.DataItemTransport;
import org.dataconservancy.ui.util.ArchiveSearchResult;

import java.util.List;

/**
 * Service for {@code DataItem} objects package with their deposit information into DataItemTransport objects.
 */
public interface DataItemTransportService {

    /**
     * Retrieve all versions of {@code DataItem} with the given id in reverse
     * chronological order by deposit date.
     * 
     * @param currentCollectionId
     *            The ID of the collection for which to retrieve data items.
     * @param maxResultsPerPage
     *            The maximum number of data items to return.
     * @param offset
     *            The section of list to retrieve
     *            
     * @return A list of data item transports encapsulating the data items associated with the collection.
     *
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    public List<DataItemTransport> retrieveDataItemTransportList(String currentCollectionId, int maxResultsPerPage, int offset)
            throws ArchiveServiceException, BizPolicyException;

    /**
     * Retrieve the latest versions of each {@code DataItem} in the list.
     * 
     * @param dataItemList
     *            The list of data item IDs to retrieve.
     *
     * @return A list of data item transports encapsulating the latest versions of data items.
     * 
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    public List<DataItemTransport> retrieveDataItemTransportList(ArchiveSearchResult<DataItem> dataItemList)
            throws ArchiveServiceException, BizPolicyException;

    /**
     * Attempt to retrieve a {@code DataItem} with the given id.
     * 
     * @param dataItemId
     *            The ID of the data item to get the latest version from the repository.
     *
     * @return DataItem matching the given id or null if the DataItem cannot be
     *         found.
     *
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    public DataItemTransport retrieveDataItemTransport(String dataItemId)
            throws ArchiveServiceException, BizPolicyException;

    /**
     * Retrieve the deposit ID corresponding to the object ID.
     *
     * @param object_id
     *            The ID of the object.
     *
     * @return The deposit ID matching the given successfully deposited object id
     *         or null if the deposit ID for the object cannot be found.
     */
    public String getDepositId(String object_id);
}
