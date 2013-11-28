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
package org.dataconservancy.dcs.ingest.file.impl;

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.deposit.AtomEventStatusDocument;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;

public class FileDepositInfo
        implements DepositInfo {

    private final Dcp sipContainer;

    private final DepositManager depositManager;

    private final EventManager eventManager;

    private final String sipId;

    private final Map<String, String> metadata = new HashMap<String, String>();

    public FileDepositInfo(Dcp sip,
                           String sipRef,
                           DepositManager mgr,
                           EventManager eventmgr) {
        sipContainer = sip;
        sipId = sipRef;
        depositManager = mgr;
        eventManager = eventmgr;
    }

    public DepositDocument getDepositContent() {
        return new FileContentDocument(sipContainer, sipId, eventManager);
    }

    public String getDepositID() {
        return sipId;
    }

    public DepositDocument getDepositStatus() {

        return new AtomEventStatusDocument(sipId, eventManager);
    }

    public String getManagerID() {
        return depositManager.getManagerID();
    }

    public String getSummary() {
        for (DcsEvent e : eventManager.getEvents(sipId)) {
            if (e.getEventType().equals(Events.INGEST_SUCCESS)) {
                return "complete: success";
            } else if (e.getEventType().equals(Events.INGEST_FAIL)) {
                return "complete: fail";
            }
        }
        return "ingesting";
    }

    public boolean hasCompleted() {
        DcsEvent success =
                eventManager.getEventByType(sipId, Events.INGEST_SUCCESS);
        DcsEvent fail = eventManager.getEventByType(sipId, Events.INGEST_FAIL);
        return (success != null || fail != null);
    }

    public boolean isSuccessful() {
        return eventManager.getEventByType(sipId, Events.INGEST_SUCCESS) != null;
    }

    /**
     * Get a live handle to the metadata/header map.
     * <p>
     * External modifications (additions, subtractions) to this map will be
     * persistent.
     * </p> {@inheritDoc}
     */
    public Map<String, String> getMetadata() {

        return metadata;
    }

}
