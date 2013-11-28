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
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * Service to handle metadata files in the UI. This service is the main entry point for validating, indexing and archiving metadata files.
 *
 */
public interface MetadataFileBizService {
    
    /**
     * Handles adding a new metadata file to the system. Will map and deposit file contents in archive the file.
     * Adding a new metadata file no longer requires also updating the collection that contains it
     * by calling updateCollection in {@code CollectionBizService}.  This relationship is kept in teh RelationshipService
     * @param file The metadata file to add
     * @param parent The business object this file is metadata for
     * @param user The user who is attempting to deposit this file
     */
    public void addNewMetadataFile(BusinessObject parent, MetadataFile file, Person user) throws BizPolicyException, BizInternalException;

    /**
     * Handles removing the relationship between a metadata file and its parent object in the RelationshipService.
     * @param parent
     * @param file
     * @param user
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    public void removeMetadataFile(BusinessObject parent, MetadataFile file, Person user) throws BizPolicyException, BizInternalException;
    
    /**
     * Given an id returns the {@code MetadataFile} representation of the metadata. 
     * @param id The id of the file you wish to retrieve.
     * @return The MetadataFile business object referenced by the id, or null if the file can not be found.
     */
    public MetadataFile retrieveMetadataFile(String id) throws ArchiveServiceException;
    
    /**
     * Given an id returns the {@code DateTime} representing the last time the metadata file was modified.
     * @param id The id of the {@code MetadataFile} to get the last modified date for.
     * @return The last modified date, or null if the Metadata id can not be resolved
     */
    public DateTime getLastModifiedDate(String id);

    /**
     * Retrieves a mime type for the {@code File}
     * @param file
     * @return a {@code String} representing the mime type of the supplied {@code File}
     * @throws IOException
     */
    public String getMimeType(File file) throws IOException;
    
}