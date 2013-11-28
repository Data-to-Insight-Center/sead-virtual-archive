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

package org.dataconservancy.business.client;

import java.util.List;

/** Business object oriented interface for interacting with an archive. */
public interface ArchiveService {

    /**
     * Deposit a new or updated business object into the archive.
     * <p>
     * As the archival process may be asynchronous, this method merely initiates
     * a deposit. Listening or polling via {@link #getDepositInfo(String)} will
     * be necessary in order to determine if or when the object has been
     * successfully archived.
     * 
     * @param businessObject
     *        The business object to deposit.
     * @param businessClass
     *        The business object class.
     * @return {@link DEPOSIT_ID} of the deposit attempt.
     */
    public <T extends BusinessObject> String deposit(T businessObject,
                                                     Class<T> businessClass);

    /**
     * Deposit a business object that explicitly represents an update of a
     * specific version.
     * <p>
     * If the archive cannot honor the request to create an updated version of
     * the archival object identified by the given predecessor archive id, the
     * deposit will fail.
     * </p>
     * 
     * @param businessObject
     *        The business object to deposit.
     * @param predecesorArchiveId
     *        {@link ARCHIVE_ID} identifying the archival version being updated.
     * @param businessClass
     *        The business object class
     * @return
     */
    public <T extends BusinessObject> String update(T businessObject,
                                                    String predecesorArchiveId,
                                                    Class<T> businessClass);

    /**
     * Get information relating to the state of a deposit.
     * <p>
     * Archives may have their own policy determining the availability of
     * information relating to completed deposits.
     * </p>
     * 
     * @param depositId
     *        {@link DEPOSIT_ID} identifying the deposit.
     * @return DepositInfo describing the state of the given deposit.
     */
    public DepositInfo getDepositInfo(String depositId);

    /**
     * Register a global listener for deposits from this client instance.
     * <p>
     * The listener will be notified on every state change all deposits through
     * this instance of the archive service.
     * </p>
     * 
     * @param listener
     */
    public void addListener(DepositListener listener);

    /**
     * Retrieves a specific business object version from the archive.
     * 
     * @param archiveId
     *        Archival identifier corresponding to an archived version of a
     *        business object.
     * @param businessClass
     *        The business object class.
     * @return An instance of the business object, or NULL if not found.
     */
    <T extends BusinessObject> T retrieve(String archiveId,
                                          Class<T> businessClass);

    /**
     * Looks up the archival IDs of all versions of the given object.
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
     * deposit action identified by the given deposit id. If it is found, then a
     * list of the matching archival object and all predecessors and successors
     * will be returned.</dd>
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

    /**
     * Enumerates the different kinds of identifiers used by the ArchiveService}
     */
    public enum IdentifierType {

        /** An identifier representing the identity of an archival object. */
        ARCHIVE_ID,

        /** An identifier representing the identity of a business object */
        BUSINESS_ID,

        /** An identifier representing a specific deposit attempt. */
        DEPOSIT_ID
    }
}
