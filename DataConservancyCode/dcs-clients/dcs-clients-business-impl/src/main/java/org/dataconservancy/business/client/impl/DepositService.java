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
import org.dataconservancy.business.client.DepositInfo;
import org.dataconservancy.business.client.DepositListener;

/**
 * Deposits an object into an archive, asynchronously
 * 
 * @param <A>
 *        Archival type.
 */
public interface DepositService<A> {

    /**
     * Initiate a deposit of an archival object.
     * <p>
     * The archival object is expected to be in ready-to-deposit-form. Any
     * identifier resolution, archive correlation, or modifications of any kind
     * are assumed to have been done already. The object is deposited as-is.
     * This method starts an asynchronous deposit process, and returns a
     * {@link IdentifierType#DEPOSIT_ID DEPOSIT_ID} used for querying its status.
     * </p>
     * 
     * @param archivalObject
     *        Archival object to deposit.
     * @return
     */
    public String deposit(A archivalObject);

    /**
     * Register a global listener for deposits from this client instance.
     * <p>
     * The listener will be notified on every state change all deposits through
     * this instance of the deposit service.
     * </p>
     * 
     * @param listener
     *        Lister to receive callbacks
     */
    public void addListener(DepositListener listener);

    /**
     * Get information relating to the state of a deposit.
     * <p>
     * May have its oown policy determining the availability of information
     * relating to completed deposits.
     * </p>
     * 
     * @param depositId
     *        {@link IdentifierType#DEPOSIT_ID DEPOSIT_ID} identifying the
     *        deposit.
     * @return DepositInfo describing the state of the given deposit.
     */
    public DepositInfo getDepositInfo(String depositId);
}
