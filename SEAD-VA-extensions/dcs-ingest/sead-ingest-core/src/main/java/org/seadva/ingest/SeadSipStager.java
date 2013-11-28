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
package org.seadva.ingest;

import org.dataconservancy.dcs.ingest.SipStager;
import org.seadva.model.pack.ResearchObject;

import java.util.Set;

/**
 * Provides access to SIPs throughout an ingest process.
 * <p>
 * Durably or semi-durably persists sips from ingest start through ingest
 * finish. It is intended that staged sips are removed after they are no longer
 * necessary (i.e. an ingest has completed)
 * </p>
 */
public interface SeadSipStager extends SipStager{

    public String addSIP(ResearchObject sip);

    /**
     * Retrieve a sip from the stager.
     * <p>
     * Reflects the state of the sip as last updated.
     * </p>
     * 
     * @param id
     *        Sip identifier.
     * @return Popilated Dcp containing sip content.
     */
    public ResearchObject getSIP(String id);

    /**
     * Retrieved the identifiers of all staged Sips.
     * 
     * @return Set of all staged sip identifiers.
     */
    public Set<String> getKeys();

    /**
     * Persist changes to a sip.
     * <p>
     * Staged sip content will be replaced by the given Dcp sip.
     * </p>
     * 
     * @param sip
     *        Dcp package containing the current state of the sip.
     * @param id
     *        Sip identifier.
     */
    public void updateSIP(ResearchObject sip, String id);

    /**
     * Remove a sip from the stager.
     * <p>
     * Requests that a staged sip be immediately removed from the stager. It is
     * intended to be used for exceptional conditions such as ingest failure.
     * 
     * @param id
     *        Sip identifier.
     */
    public void removeSIP(String id);

    /**
     * Indicate that an ingest has completed, and the Sip is no longer
     * necessary.
     * <p>
     * When invoked, the stager will free staged sip resources at its
     * convenience, or according to a policy.
     * </p>
     * 
     * @param id
     *        Sip identifier.
     */
    public void retire(String id);
}
