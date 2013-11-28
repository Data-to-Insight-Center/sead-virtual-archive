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
package org.dataconservancy.dcs.ingest.sip;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.deposit.AtomEventStatusDocument;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;

import static org.dataconservancy.dcs.ingest.Events.INGEST_FAIL;
import static org.dataconservancy.dcs.ingest.Events.INGEST_SUCCESS;

/**
 * Ingest snapshot for a dcp SIP.
 * <p>
 * Contains a point-in-time representation of the current content and status of
 * a given sip in the ingest process.
 * </p>
 */
public class SipIngestInfo
        implements DepositInfo {

    private final Dcp sip;

    private final String sipRef;

    private final String mgrId;

    private final EventManager eventMgr;

    private final Map<String, String> metadata = new HashMap<String, String>();

    private Collection<DcsEvent> events;

    public SipIngestInfo(Dcp dcp, EventManager em, String sipId, String mid) {
        sip = dcp;
        sipRef = sipId;
        mgrId = mid;
        eventMgr = em;
    }

    public DepositDocument getDepositContent() {
        return new DcpSipContentDocument(sip, sipRef, eventMgr);
    }

    public String getDepositID() {
        return sipRef;
    }

    public DepositDocument getDepositStatus() {
        return new AtomEventStatusDocument(sipRef, eventMgr);
    }

    public String getManagerID() {
        return mgrId;
    }

    public String getSummary() {
        for (DcsEvent e : getEvents()) {
            if (e.getEventType().equals(Events.INGEST_SUCCESS)) {
                return "complete: success";
            } else if (e.getEventType().equals(Events.INGEST_FAIL)) {
                return "complete: fail";
            }
        }
        return "ingesting";
    }

    public boolean hasCompleted() {
        for (DcsEvent e : getEvents()) {
            if (INGEST_FAIL.equals(e.getEventType())
                    || INGEST_SUCCESS.equals(e.getEventType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuccessful() {
        for (DcsEvent e : getEvents()) {
            if (INGEST_SUCCESS.equals(e.getEventType())) {
                return true;
            }
        }
        return false;
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

    private Collection<DcsEvent> getEvents() {
        if (events == null) {
            events = eventMgr.getEvents(sipRef);
        }
        return events;
    }
}
