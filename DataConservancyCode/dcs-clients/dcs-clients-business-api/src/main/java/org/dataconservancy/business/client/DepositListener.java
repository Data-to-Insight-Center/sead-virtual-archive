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

/** Listens for deposit state changes in an archive */
public interface DepositListener {

    /**
     * Callback function called whenever a deposit state change has occurred.
     * <p>
     * At a minumum, this is called when a deposit starts, and when state
     * changes from {@link DepositInfo.State#PENDING} to
     * {@link DepositInfo.State#SUCCESS} or {@link DepositInfo.State#FAIL}.
     * Archives may have their own notion of fine-grained state and additionally
     * invoke this method whenever that state changes.
     * </p>
     * 
     * @param info
     *        DepositInfo of a deposit whose status has changed.
     */
    public void onStatusChange(DepositInfo info);
}
