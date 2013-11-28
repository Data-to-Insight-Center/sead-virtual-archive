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
package org.dataconservancy.archive.impl.fcrepo.mapper;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.impl.fcrepo.PIDMapper;
import org.dataconservancy.archive.impl.fcrepo.dto.FedoraDigitalObject;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;


public abstract class DCSMapper {
    
    // DCP AIP being processed.  The whole AIP may be needed to resolve
    // cross references.
    Dcp aip;
    
    // Generates a hash that maps the DCS ID to a PID.
    // This is shareable and thread safe.
    static PIDMapper mapper = new PIDMapper("dcs");
    
    public DCSMapper(Dcp aip) {
        this.aip = aip;
    }

    /**
     * Map the DCS entity in its data model to an equivalent Fedora Digital Object.
     * 
     * @param in the entity to map
     * @return a object suitable for ingest into the Fedora repository
     * @throws AIPFormatException
     */
    public abstract FedoraDigitalObject map(DcsEntity in) throws AIPFormatException;
    
}
