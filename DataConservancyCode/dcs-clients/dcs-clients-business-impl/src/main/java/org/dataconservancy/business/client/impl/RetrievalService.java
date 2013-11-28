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

package org.dataconservancy.business.client.impl;

import org.dataconservancy.business.client.ArchiveService.IdentifierType;

/**
 * Retrieves instances of archival object from an archive.
 * 
 * @param <A>
 *        Archival object type.
 */
public interface RetrievalService<A> {

    /**
     * Retrieve an instance of an archival object from an archive.
     * 
     * @param archiveId
     *        {@link IdentifierType#ARCHIVE_ID ARCHIVE_ID} of the object to
     *        retrieve.
     * @return Archived object, or null if not present.
     */
    public A retrieve(String archiveId);
}
