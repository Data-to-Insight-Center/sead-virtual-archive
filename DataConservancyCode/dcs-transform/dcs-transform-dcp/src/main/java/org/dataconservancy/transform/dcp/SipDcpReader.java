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
package org.dataconservancy.transform.dcp;

import java.util.HashMap;

import org.dataconservancy.model.dcp.Dcp;

/**
 * Simply reads a single given sip
 * <p>
 * The unmodified sip is returned as the read value, while the number of
 * DeliverableUnits contained within the sip is returned as the key.
 * </p>
 */
public class SipDcpReader
        extends AbstractReader {

    /**
     * Constructor - create a SipDcpReader to read and repackage the incoming
     * Dcp
     * 
     * @param sourceDcp
     */
    public SipDcpReader(Dcp sourceDcp) {

        resultMap = new HashMap<String, Dcp>();
        resultMap.put(Integer.toString(sourceDcp.getDeliverableUnits().size()),
                      sourceDcp);
        keyIterator = resultMap.keySet().iterator();
    }
}
