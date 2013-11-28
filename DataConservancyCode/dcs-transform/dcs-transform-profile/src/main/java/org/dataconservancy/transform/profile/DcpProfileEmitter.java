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
package org.dataconservancy.transform.profile;

import java.util.Map;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

/**
 * Emits instances of Dcp profiles present in a given Dcp package.
 * <p>
 * A Dcp profile (could also be called a content model) is a particular named
 * combination DeliverableUnits, Manifestations, Files, and associated
 * properties. Profiles label the many ways in which data may be represented in
 * the DCS, and may be representative of data source or capability.
 * </p>
 * <p>
 * Given a Dcp, this class will emit a mapping of DCPs to their profile URIs.
 * ProfileURIs are emitted as keys, Dcps as values.
 * <p>
 */
public class DcpProfileEmitter
        implements Mapping<Object, Dcp, String, Dcp> {

    private Map<String, DcpProfileDetector> detectorMap;

    public void setDetectorMap(Map<String, DcpProfileDetector> detectors) {
        detectorMap = detectors;
    }

    /**
     * Given a Dcp, emits a format URI for each detected format.
     * 
     * @param key
     *        Arbitrary. Will be ignored.
     * @param dcp
     *        Dcp containing at least one DeliverableUnit and any ancestor nodes
     *        (Manifestation, File)
     * @param writer
     *        Output to the writer will contain a profileURI key and Dcp value.
     *        The Dcp value will be some subset of the input dcp.
     */
    public void map(Object key, Dcp dcp, Output<String, Dcp> writer) {
        for (Map.Entry<String, DcpProfileDetector> detector : detectorMap
                .entrySet()) {
            if (detector.getValue().hasFormat(dcp)) {
                writer.write(detector.getKey(), dcp);
            }
        }
    }
}
