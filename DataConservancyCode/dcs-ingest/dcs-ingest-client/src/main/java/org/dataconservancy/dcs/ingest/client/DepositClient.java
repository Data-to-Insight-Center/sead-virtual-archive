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

import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.model.dcp.Dcp;

/**
 * Produces SipDeposit instances for individual deposits.
 */
public interface DepositClient {

    /**
     * Create a SipDeposit instance for building and depositing a sip.
     * <p>
     * Contents of the given Dcp packages will be copied to provide the initial
     * content to build upon. It is expected that the sip will be populated with
     * content as necessary, then ultimately deposited through
     * {@link DepositBuilder#execute()}
     * </p>
     * 
     * @param template
     *        Initial sip content to copy. If nothing is provided, the initial
     *        sip will be empty.
     * @return initially populated SipDeposit.
     */
    public DepositBuilder buildDeposit(Dcp... template);

    public String doDeposit(byte[] content);

    /**
     * Get the status information of an existing deposit.
     * <p>
     * The deposit information returned should not be relied upon to update
     * itself. Polling for ingest updates should be achieved through repeated
     * calls to {@link DepositClient#getDepositInfo(String)}
     * </p>
     * 
     * @param depositId
     *        String identifier identifying the deposit. Values from
     *        {@link DepositBuilder#execute()} are appropriate to use as
     *        arguments here.
     * @return DepositInfo for this particular deposit.
     */
    public DepositInfo getDepositInfo(String depositId);

}
