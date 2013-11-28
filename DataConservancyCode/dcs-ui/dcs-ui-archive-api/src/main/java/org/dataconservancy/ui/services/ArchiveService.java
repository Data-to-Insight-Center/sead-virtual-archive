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

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.util.ArchiveSearchResult;

/**
 * Deposit Collections, DataSets and MetadatFiles in an archive and retrieve them from the DCS archive.
 * 
 * The interface is based on per deposit identifiers. A deposit identifier corresponds to a particular deposit attempt.
 */
public interface ArchiveService {
    /**
     * For each pending deposit, check the archive and update the status.
     * 
     * @throws ArchiveServiceException
     */
    void pollArchive() throws ArchiveServiceException;

    /**
     * Deposit a collection into the archive. If the collection shares the same id as a collection
     * already deposited in the archive, this collection becomes a new version of the already deposited collection.
     * If a MetadataFile included in the collection has the same business id as a MetadataFile already deposited
     * in the archive, then the newly deposited collection will reference the already deposited MetadataFile and
     * the other attributes of the MetadataFile will be ignored. The exception is that the path must be set the
     * same as the previous version.
     * 
     * If a collection has a parent attribute set, it will be added as a child collection. This relationship persists
     * across any version updates.
     * 
     * @param col
     * @return identifier for deposit
     */
    String deposit(Collection col) throws ArchiveServiceException;

    /**
     * Deposit a data set into an archived collection. If the data item shares the same id as a DataItem
     * already deposited in the archive, this data item becomes a new version of the already deposited DataItem.
     * If a DataFile included in the DataItem has the same business id as a DataFile already deposited in the
     * archive, then the newly deposited DataItem will reference the already deposited DataFile and the other
     * attributes of the DataFile will be ignored. The exception is that the path must be set the
     * same as the previous version.
     * 
     * @param deposit_id
     *            of collection
     * @param ds
     * @return identifier for deposit
     */
    String deposit(String deposit_id, DataItem ds)
            throws ArchiveServiceException;
    
    /**
     * Deposit a metadata file into an archived collection. If the metadata file shares the same id as a Metadata File
     * already deposited in the archive, this metadata file becomes a new version of the already deposited MetadataFile.
     * If the file referenced in the MetadataFile has the same business id as a file already deposited in the
     * archive, then the newly deposited MetadataFile will reference the already deposited file and the other
     * attributes of the file will be ignored. The exception is that the path must be set the
     * same as the previous version. Note that if a new metadata file is added the owning collection must be updated. 
     * 
     * @param deposit_id
     *            of collection
     * @param ds
     * @return identifier for deposit
     */
    String deposit(String deposit_id, MetadataFile mf)
            throws ArchiveServiceException;
    
    /**
     * Deposits a metadata format registry entry into the archive. Registry entries currently don't have a concept of versions,
     * so each registry entry deposited will be a new entry in the registry. 
     * @param formatRegistryEntry RegistryEntry representing the metadata format to deposit.
     * @return identifier for deposit
     */
    String deposit(RegistryEntry<DcsMetadataFormat> formatRegistryEntry) 
            throws ArchiveServiceException;

    /**
     * Check deposit status.
     * 
     * @param deposit_id
     * @return status of a deposit.
     */
    Status getDepositStatus(String deposit_id);

    /**
     * Retrieve deposit identifiers for all collection deposits which have the specified deposit status.
     * 
     * @param status
     *            null to match any status
     * @return deposit identifiers for Collections with given status
     */
    List<String> listCollections(Status status);

    /**
     * Retrieve a collection from the archive. Return null if the collection has
     * not yet been deposited. Any children of the collection will be set in the children ids attribute.
     * 
     * @param deposit_id
     *            for collection
     * @return a collection from the archive
     * @throws ArchiveServiceException
     */
    public ArchiveSearchResult<Collection> retrieveCollection(String deposit_id)
            throws ArchiveServiceException;

    /**
     * Retrieve deposit identifiers for all data set deposits which have the specified deposit status.
     * 
     * @param status
     * 
     *            null to match any status
     * @return deposit identifiers for data sets with given status
     */
    public List<String> listDataSets(Status status);

    /**
     * Retrieve a data set from the archive. Return null if the data set has not
     * yet been deposited.
     * 
     * @param deposit_id
     *            for data set
     * @return a dataset from the archive.
     */
    public ArchiveSearchResult<DataItem> retrieveDataSet(String deposit_id)
            throws ArchiveServiceException;
    
    /**
     * Retrieve deposit identifiers for all metadata file deposits which have the specified deposit status.
     * 
     * @param status
     * 
     *            null to match any status
     * @return deposit identifiers for data sets with given status
     */
    public List<String> listMetadataFiles(Status status);

    /**
     * Retrieve a data set from the archive. Return null if the data set has not
     * yet been deposited.
     * 
     * @param deposit_id
     *            for metadata file
     * @return a metadata file from the archive.
     */
    public ArchiveSearchResult<MetadataFile> retrieveMetadataFile(String deposit_id)
            throws ArchiveServiceException;
    
    /**
     * Returns list of info about deposit attempts for objects with the given id and status.  The returned list
     * will be sorted in reverse chronological order, with the most recent ArchiveDepositInfo object at the head
     * of the list, and the oldest ArchiveDepositInfo object at the tail.
     * 
     * @param object_id the business id of the object
     * @param status or null for any status
     * @return list of deposit info matching status
     */
    public List<ArchiveDepositInfo> listDepositInfo(String object_id, Status status);
    
    /**
     * Obtain {@code DataItem} objects that belong to the collection specified by {@code collection_deposit_id}.
     * Implementations must impose a <em>total ordering</em> over the search results based on the DataItem deposit date.
     * The most recently deposited DataItem must appear at the head of the search results, and the least recently
     * deposited DataItem must appear at the tail of the search results.
     * <p/>
     * "Total ordering" means that the search results must be sorted before applying any constraints such as the number
     * of results to be returned, or an offset into the search results.
     *
     * @param collection_deposit_id The deposit identifier of the collection whose datasets are to be returned.
     * @param numberOfResults The number of results desired to be returned, or -1 if they should all be returned.
     * @param offset the offset into the <em>total</em> number of results
     * @throws ArchiveServiceException When the specified collection could not be found.
     * @return A ordered set of DataItem objects with the result. Or an empty set if no results were found.
     */
    public ArchiveSearchResult<DataItem> retrieveDataSetsForCollection(String collection_deposit_id, int numberOfResults, int offset)
            throws ArchiveServiceException;    
    
}
