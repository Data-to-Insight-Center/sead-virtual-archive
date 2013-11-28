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
package org.dataconservancy.dcs.ingest.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;
import org.springframework.beans.factory.annotation.Required;

import static org.dataconservancy.dcs.util.DateUtility.now;
import static org.dataconservancy.dcs.util.DateUtility.toIso8601;

/**
 * Implementation of EventManager which stores all Events inside a SIP.
 * <p>
 * Has No internal logic - simply stores <em>everything</em> within the
 * specified SIP.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIdService(BulkIdCreationService)}</dt>
 * <dd><b>Required</b>. An ID service is used to assign IDs to new events
 * created through {@link #newEvent(String)}.</dd>
 * <dt>{@link #setSipStager(SipStager)}</dt>
 * <dd><b>Required</b>. Used by {@link #findEventById(String)}. This impl simply
 * scans all sips for the requested event.</dd>
 * <dt>{@link #setIdBatchSize(int)}</dt>
 * <dd><b>Optional</b>.  The number of identifiers to request from the {@code BulkIdCreationService} at a time.</dd>
 * </dl>
 */
public class InlineEventManager
        implements EventManager {

    private BulkIdCreationService idService;

    private SipStager sipStager;

    private int idBatchSize = 1000;

    private Iterator<Identifier> idIterator;

    @Required
    public void setIdService(BulkIdCreationService ids) {
        idService = ids;
    }

    public BulkIdCreationService getIdService() {
        return idService;
    }

    @Required
    public void setSipStager(SipStager stager) {
        sipStager = stager;
    }

    public SipStager getSipStager() {
        return sipStager;
    }

    /**
     * The number of identifiers to request from the bulk id creation service at a time.
     *
     * @return the number of identifiers to request from the bulk id creation service at a time.
     */
    public int getIdBatchSize() {
        return idBatchSize;
    }

    /**
     * The number of identifiers to request from the bulk id creation service at a time.
     *
     * @param idBatchSize the number of identifiers to request from the bulk id creation service at a time
     */
    public void setIdBatchSize(int idBatchSize) {
        if (idBatchSize < 1) {
            throw new IllegalArgumentException("Batch size must be a positive integer.");
        }
        this.idBatchSize = idBatchSize;
    }

    public void addEvent(String id, DcsEvent event) {
        Dcp sip = getSip(id);

        if (sip == null) {
            throw new RuntimeException("Cannot add events to nonexistant sip "
                    + id);
        }

        sip.addEvent(event);
        sipStager.updateSIP(sip, id);
    }

    public void addEvents(String id, Collection<DcsEvent> events) {
        Dcp sip = getSip(id);

        if (sip == null) {
            throw new RuntimeException("Cannot add events to nonexistant sip "
                    + id);
        }

        for (DcsEvent e : events) {
            sip.addEvent(e);
        }

        sipStager.updateSIP(sip, id);
    }

    public Collection<DcsEvent> getEvents(String id, String... eventTypes) {
        Dcp sip = getSip(id);
        boolean hasRestriction = eventTypes.length > 0;
        List<String> desiredTypes = Arrays.asList(eventTypes);
        List<DcsEvent> events = new ArrayList<DcsEvent>();

        if (sip != null) {
            for (DcsEvent e : sip.getEvents()) {
                if (!hasRestriction || desiredTypes.contains(e.getEventType())) {
                    events.add(e);
                }
            }
        }
        return events;
    }

    public DcsEvent getEventByType(String id, String eventType) {
        DcsEvent match = null;
        for (DcsEvent event : getEvents(id, eventType)) {
            if (match != null) {
                throw new RuntimeException("Found more than one event "
                        + "of supposed singleton " + eventType);
            }
            match = event;
        }

        return match;
    }

    public DcsEvent newEvent(String eventType) {
        DcsEvent event = new DcsEvent();
        if (idIterator == null || !idIterator.hasNext()) {
            refreshIterator();
        }
        event.setId(idIterator.next().getUrl().toString());
        event.setEventType(eventType);
        event.setDate(toIso8601(now()));
        return event;
    }

    public DcsEvent findEventById(String eventId) {
        for (String key : sipStager.getKeys()) {
            Dcp dcp = sipStager.getSIP(key);

            if (dcp != null) {
                for (DcsEvent e : dcp.getEvents()) {
                    if (eventId.equals(e.getId())) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    private Dcp getSip(String id) {
        return sipStager.getSIP(id);
    }

    private void refreshIterator() {
        this.idIterator = idService.create(idBatchSize, Types.EVENT.getTypeName()).iterator();
    }
}
