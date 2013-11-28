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

/**
 * Repesents the state of a given deposit.
 * <p>
 * TODO: Add additional methods (e.g. deposit date, etc).
 * <p>
 */
public interface DepositInfo {

    /**
     * Returns the archival identifier of a successfully deposited item.
     * 
     * @return Archival ID if the deposit is successful, NULL otherwise.
     */
    public String getArchiveId();

    /** Returns the current state of the deposit.
     * 
     * @return Current state.
     */
    public String getDepositState();

    public enum State {
        PENDING, SUCCESS, FAIL
    }
}
