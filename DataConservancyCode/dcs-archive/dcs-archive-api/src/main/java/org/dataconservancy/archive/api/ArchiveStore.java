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
package org.dataconservancy.archive.api;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Provides storage and access to DCS entities in the DCP Serialization
 * Format.
 * <p>
 * <h2>Thread Safety</h2>
 * Implementations are expected to ensure that a single instance can be used
 * safely by multiple concurrent threads.  It is therefore not necessary for
 * clients of this interface to construct multiple instances unless they
 * intend to work with multiple back-end archives.
 *
 * @see <a href="https://wiki.library.jhu.edu/x/DoKx">DCP Serialization Format</a>
 */
public interface ArchiveStore {

    /**
     * Lists the ids of entities in the archive, in no particular order.
     *
     * @param type the type of entity to limit the list to. If <code>null</code>,
     *             all entities will be listed.
     * @return an iterator of entity ids.
     */
    Iterator<String> listEntities(EntityType type);

    /**
     * Gets the content of the requested <code>File</code> entity.
     *
     * @param entityId the id of a File entity in the archive.
     * @return the associated content stream.
     * @throws EntityNotFoundException if the entity is not in the archive.
     * @throws EntityTypeException if the entity is not a File entity.
     */
    InputStream getContent(String entityId) throws EntityNotFoundException,
                                                   EntityTypeException;

    /**
     * Gets a DCP stream containing the requested entity only.
     *
     * @param entityId the id of an entity in the archive.
     * @return a stream in DCP format, with root element <code>dcp</code>.
     * @throws EntityNotFoundException if the entity is not in the archive.
     */
    InputStream getPackage(String entityId) throws EntityNotFoundException;

    /**
     * Gets a DCP stream containing the requested entity and all
     * <em>significantly related</em> entities. The definition of what is
     * significantly related depends on the type of entity being requested:
     * <p>
     * For <strong>any entity type</strong>:
     * <ul>
     *   <li> all <em>Files</em> acting as metadata for that entity are
     *        considered significantly related.</li>
     * </ul>
     * <p>
     * In addition:
     * <ul>
     *   <li>
     *     a <strong>Collection</strong>'s significantly related entities
     *     include:
     *     <ul>
     *       <li> all of that Collection's <em>Events</em> and all of their
     *            significantly related entities.</li>
     *     </ul>
     *   </li>
     *   <li>
     *     a <strong>Deliverable Unit</strong>'s significantly related entities
     *     include:
     *     <ul>
     *       <li> all of that Deliverable Unit's <em>Events</em> and all of
     *            their significantly related entities.</li>
     *       <li> all of that Deliverable Unit's <em>Manifestations</em> and
     *            all of their significantly related entities.</li>
     *     </ul>
     *   </li>
     *   <li>
     *     a <strong>Manifestation</strong>'s significantly related entities
     *     include:
     *     <ul>
     *       <li> all constituent <em>Files</em> and all of their
     *            significantly related entities.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param entityId the id of an entity in the archive.
     * @return a stream in DCP format, with root element <code>dcp</code>.
     * @throws EntityNotFoundException if the entity is not in the archive.
     */
    InputStream getFullPackage(String entityId) throws EntityNotFoundException;

    /**
     * Stores all entities in the given AIP.
     *
     * @param dcpStream a stream in DCP format, with root element <code>dcp</code>.
     * @throws AIPFormatException if the AIP is malformed.
     */
    void putPackage(InputStream dcpStream) throws AIPFormatException;

}
