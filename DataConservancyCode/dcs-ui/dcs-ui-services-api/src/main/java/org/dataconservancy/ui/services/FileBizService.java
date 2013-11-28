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
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.Person;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * Business Service for interacting with {@code File} objects. Currently only implemented method is getting a file by its ID. 
 *
 */
public interface FileBizService {
    
    /**
     * Retrieves a {@code File} matching the provided id. 
     * @param id The id of the file to retrieve
     * @param user The user retrieving the file. Authentication is dependent on the implementation of this interface.
     * @return The matching file assuming one can be found, null otherwise. 
     */
    public DataFile getFile(String id, Person user) throws ArchiveServiceException, RelationshipConstraintException, BizPolicyException;
    
    /**
     * Retrieves the last modified date of the {@code File} matching the provided id. 
     * @param id The id of the file to retrieve
     * @return The {@code DateTime} of the archive date when data set containing the file was last modified. Null if it cannot be found or a data file matching the id can't be found.
     */
    public DateTime getLastModifiedDate(String id) throws RelationshipConstraintException;

    /**
     * Retrieves a mime type for the {@code File}
     * @param file
     * @return a {@code String} representing the mime type of the supplied {@code File}
     * @throws IOException
     */
    public String getMimeType(File file) throws IOException;

}