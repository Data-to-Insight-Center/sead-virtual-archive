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
package org.dataconservancy.deposit;

import java.util.Map;

/**
 * Represents instantaneous snapshot information related to particular deposit.
 * <p>
 * May contain a single point-in-time representation of deposit workflow. An
 * instance of {@linkplain DepositInfo} should be read once and discarded, as it
 * should not update itself if deposit status changes.
 * </p>
 */
public interface DepositInfo {

    /**
     * Get an identifier naming a particular deposit.
     * 
     * @return opaque String uniquely identifying a particular deposit within
     *         the scope of its {@link DepositManager}
     */
    public String getDepositID();

    /**
     * Get the identifier naming this deposit's originating
     * {@link DepositManager}.
     * 
     * @return Opaque string equivalent to {@link DepositManager#getManagerID()}
     *         from the manager of this deposit.
     */
    public String getManagerID();

    /**
     * Return a document representing deposited content.
     * <p>
     * Contains some representation of the deposited content. It is up to the
     * implementing class to determine the presence, form, and semantics of this
     * document.
     * </p>
     * <p>
     * As an example, a deposit document may potentially contain:
     * <ul>
     * <li>The original deposited content</li>
     * <li>The state of deposited content as it is processed in deposit workflow
     * </li>
     * <li>Metadata describing deposited content</li>
     * </ul>
     * </p>
     * 
     * @return DepositDocument representing deposited content, or null if not
     *         applicable.
     */
    public DepositDocument getDepositContent();

    /**
     * Return a document representing the status of content in a deposit
     * workflow.
     * <p>
     * If a deposit implies a complex workflow process, it may be useful to
     * convey the state or progress of a deposited item as it evolves. Thus,
     * this document contains some representation of workflow status for the
     * deposited content.
     * </p>
     * 
     * @return DepositDocument containing workflow status, or null if not
     *         applicable.
     */
    public DepositDocument getDepositStatus();

    /**
     * Get a quick human-readable summary of the deposit status.
     * 
     * @return String containing a short summary
     */
    public String getSummary();

    /**
     * Quickly determine if a deposit has completed.
     * 
     * @return true if the deposit workflow is considered to have been
     *         completed.
     */
    public boolean hasCompleted();

    /**
     * Quickly determine if a deposit has completed successfully.
     * 
     * @return true if and only if the deposit (a) has completed and (b) has
     *         been successfully processed/accepted.
     */
    public boolean isSuccessful();

    /**
     * Get any metadata associated with this depositInfo, in http header form.
     * 
     * @return Map containing keys and values in http header form.
     */
    public Map<String, String> getMetadata();
}
