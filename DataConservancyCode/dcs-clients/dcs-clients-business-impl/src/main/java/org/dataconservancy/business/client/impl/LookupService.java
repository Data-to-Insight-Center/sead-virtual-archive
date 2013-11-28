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

import java.util.List;

import org.dataconservancy.business.client.ArchiveService.IdentifierType;

/** Looks up archival entity IDs from the archive. */
public interface LookupService {

    /**
     * Performs a search of the archive to find the archival IDs of all versions
     * of the given object.
     * <p>
     * For a given archival, business, or deposit id, this method will return
     * the identity of all archived objects that correspond to it, in reverse
     * order (i.e. most recent first). If an optional limit N is specified, then
     * only the most recent N archival identifiers are returned.
     * </p>
     * <p>
     * A given id "corresponds" to a lineage of archived objects in the
     * following way, based on id type:
     * <dl>
     * <dt>ARCHIVE_ID</dt>
     * <dd>Returns the archived object, and all predecessors and successors
     * found in the archive. If the given archive id is not found (i.e. it does
     * not match an archive object), the return list will be empty.</dd>
     * <dt>BUSINESS_ID</dt>
     * <dd>A lookup will be performed to match the given business ID to all
     * corresponding archived objects associated with that with that business
     * ID. If no archived arbjects are found, the return list will be empty</dd>
     * <dt>DEPOSIT_ID</dt>
     * <dd>Will attempt to lookup the archival id of object deposited in the
     * deposit action identified by the given deposit id, if that is archived.
     * If it is found, then a list of the matching archival object and all
     * predecessors and successors will be returned.  <b><em>Because deposit id may or may
     * not be archived depending on policy, this might never return a matching result if
     * deposit IDs are not archived!</em></b></dd>
     * </dl>
     * </p>
     * <p>
     * <h4>Example</h4> Use lookup and retrieve to get the latest version of a
     * business object: <br />
     * <code>
     * String latestArchiveId = archive.lookup("12345", ARCHIVE_ID, 1).get(0);
     * BO bussinessObj = archive.retrieve(latestArchiveId, BO.class);
     * </code>
     * </p>
     * 
     * @param id
     *        Archival, Business, or Deposit identifier
     * @param type
     *        Type of identifier
     * @param limit
     *        Optional limit to the number of results returned.
     * @return List of ARCHIVE_ID of all archival object versions of the
     *         matching object. This list is empty if there is no match.
     */
    List<String> lookup(String id, IdentifierType type, int... limit);
}
