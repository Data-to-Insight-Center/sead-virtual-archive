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
package org.dataconservancy.dcs.ingest.client;

import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;

/**
 * Interface for building and depositing a single sip.
 * <p>
 * Provides convenient methods for attaching files appropriately in the sip,
 * performing the deposit, and determining its eventual success or failure.
 * </p>
 */
public interface DepositBuilder {

    /**
     * Get the active sip associated with this deposit.
     * <p>
     * Returns a live sip which may be modified at will to add/modify/update
     * data to be deposited. This is a pointer to the sip being mutated by the
     * convenience methods presented in this class, so any changes directly
     * modify the sip that will be submitted upon execute().
     * </p>
     * 
     * @return The active sip associated with this deposit.
     */
    public Dcp getSip();

    /*
     * Add a new entity to the sip, performing any necessary id
     * assignment/checking. <p> Adds an entity to the internal sip, and either
     * assigns an id if not assigned, or verifies that the id is acceptable
     * (e.g. no other entity uses the same id). It is entirely appropriate, and
     * perhaps preferrable, to use as follows: <pre> DcsDeliverableUnit du =
     * builder.add(new DcsDeliverableUnit()); </pre> </p>
     * @param <T> Entity type
     * @param entity Entity to add. Can be completely uninitialized.
     * @return the entity, with the id field populated or verified.
     */
    //public <T extends DcsEntity> T add(T entity);

    /**
     * Deposits or attaches a file, and includes it as a manifestationFile in
     * one or more manifestations.
     * <p>
     * If there is only one manifestation present in the sip, and no
     * manifestation ids have been specified, then this file will automatically
     * be added to it as a manifestationFile. Otherwise, the file will be added
     * as a manifestationFile to all specified manifestations.
     * </p>
     * 
     * @param path
     *        Path to the file on the filesystem.
     * @param manifestations
     *        Ids of all manifestations in which to associate this file as a
     *        manifestationFile. If not set, it will default to any solitary
     *        manifestation in the sip.
     * @return live DcsFile reference. Subsequent to this DcsFile will propagate
     *         to the underlying sip, but not necessarily the other way around.
     */
    public DcsFile uploadFile(String path, String... manifestations);

    /**
     * Includes a file by reference as a manifestationFile in one or more
     * manifestations.
     * <p>
     * Behaves the same as {@link #uploadFile(String, String...)}, except that
     * it does not actually upload content, but merely includes the path by
     * reference. Importantly, extant is set to 'true' - the the dcs will try to
     * acquire its content for preservation.
     * </p>
     * 
     * @param path
     *        Path to the file. Typically, this would be a http or file uri.
     * @param manifestations
     *        Ids of all manifestations in which to associate this file as a
     *        manifestationFile. If not set, it will default to any solitary
     *        manifestation in the sip.
     * @return live DcsFile reference. Subsequent to this DcsFile will propagate
     *         to the underlying sip, but not necessarily the other way around.
     */
    public DcsFile referenceFile(String path, String... manifestations);

    /**
     * Deposits or attaches a file, and includes it as metadata for one or more
     * objects.
     * 
     * @param path
     *        Path to the file on the filesystem.
     * @param targets
     *        All objects for which this file should be included as metadata.
     * @return live DcsFile reference. CSubsequent to this DcsFile will
     *         propagate to the underlying sip, but not necessarily the other
     *         way around.
     */
    public DcsFile uploadMetadata(String path, String... targets);

    /**
     * Includes a file by reference as metadata for one or more objects.
     * <p>
     * The sip deposit impl will take care of content and link accounting.
     * </p>
     * 
     * @param path
     *        Path to the file. Typically, this would be a http or file uri.
     * @param targets
     *        All objects for which this file should be included as metadata.
     * @return live DcsFile reference representing the metadata file. Subsequent
     *         to this DcsFile will propagate to the underlying sip, but not
     *         necessarily the other way around.
     */
    public DcsFile referenceMetadata(String path, String... targets);

    /**
     * Deposit the sip.
     * <p>
     * Deposits the sip obtainable by getSip(), and returns a depositID. This id
     * can be later used to determine success or failure from a deposit manager
     * via {@link DepositManager#getDepositInfo(String)}.
     * </p>
     * 
     * @return Identity of current deposit operation.
     */
    public String execute();

}
