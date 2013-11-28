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
package org.dataconservancy.transform.dcp;

import java.util.Iterator;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.transform.Reader;

public class ArchiveServiceIdReader
        implements Reader<String, String> {

    private final Iterator<String> idIterator;

    private String currentId;

    public ArchiveServiceIdReader(ArchiveStore arch) {
        idIterator = arch.listEntities(null);
    }

    public ArchiveServiceIdReader(ArchiveStore arch, EntityType type) {
        idIterator = arch.listEntities(type);
    }

    /**
     * Contains the ID of the contained entity.
     */
    public String getCurrentKey() {
        return currentId;
    }

    /** Contains a Dcp containing the entity. */
    public String getCurrentValue() {
        return currentId;
    }

    /** Returns true if there is another entity available from the ArchiveStore. */
    public boolean nextKeyValue() {
        if (idIterator.hasNext()) {
            currentId = idIterator.next();
            return true;
        } else {
            return false;
        }
    }

    public void close() {
        /* Nothing to do */
    }

}
