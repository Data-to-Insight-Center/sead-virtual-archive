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
package org.dcs.clients.business.impl.dcp;

import org.dataconservancy.business.client.impl.CorrelationService;
import org.dataconservancy.model.dcp.Dcp;


/** Produces a correlated, deposit-ready DCP.
 */
public class DcpCorrelationService
        implements CorrelationService<Dcp> {

    @Override
    public Dcp correlate(Dcp archivalObject, String predecesorArchiveId) {
        // TODO Auto-generated method stub
        return null;
    }

}
