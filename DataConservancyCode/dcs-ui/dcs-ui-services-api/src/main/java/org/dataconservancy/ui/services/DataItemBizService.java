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

import java.util.List;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;

/**
 * Business Service for interacting with {@code DataItem} objects.
 */
public interface DataItemBizService {

    /**
     * Retrieve all versions of {@code DataItem} with the given id in reverse
     * chronological order by deposit date.
     * 
     * @param business_id
     *            The id of the file to retrieve.
     * @param user
     *            The user retrieving the file. Authentication is dependent on
     *            the implementation of this interface.
     * @return Versions of the dataset.
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * @throws BizPolicyException
     */
    public List<DataItem> getDataItemVersions(String business_id, Person user)
            throws ArchiveServiceException, BizPolicyException;

    /**
     * Attempt to retrieve a DataItem with the given id.
     * 
     * @param business_id
     * @param user
     *            The user retrieving the file. Authentication is dependent on
     *            the implementation of this interface.
     * @return DataItem matching the given id or null if the DataItem cannot be
     *         found.
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * @throws BizPolicyException
     */
    public DataItem getDataItem(String business_id, Person user)
            throws ArchiveServiceException, BizPolicyException;
    
    /**
     * Attempt to retrieve a DataItem with the given id, no user constraints.
     * 
     * @param businessId
     * @return DataItem matching the given id or null if the DataItem cannot be found.
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * @throws BizPolicyException
     */
    public DataItem getDataItem(String businessId) throws ArchiveServiceException, BizPolicyException;
    
    /**
     * If correct permissions are in place, adds a new Data Item to the archive, and establishes required relationships in the UI. 
     * @param item The data item to be added.
     * @param collectionId The id the collection the data item is being added to. 
     * @param user The user who is attempting to add a data item to the system.
     * @return The id of the created data item.
     * @throws ArchiveServiceException If there was an issue depositing the data item into the archive.
     * @throws BizPolicyException If the user doesn't have proper permissions to deposit the Data Item into the provided collection;
     * @throws BizInternalException If the supplied collection can't be found.
     */
    public String addDataItem(DataItem item, String collectionId, Person user) throws ArchiveServiceException, BizPolicyException, BizInternalException;
    
    /**
     * Retrieves a DataFile given the provided business object id. 
     * @param businessId The id of the file to return. 
     * @return The DataFile referenced by the identifier or null if it can not be found.
     */
    public DataFile getDataFile(String businessId) throws ArchiveServiceException, BizInternalException;
}