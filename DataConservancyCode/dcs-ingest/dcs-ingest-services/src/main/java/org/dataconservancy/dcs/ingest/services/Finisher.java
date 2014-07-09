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
package org.dataconservancy.dcs.ingest.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Verifies SIP in archive and index, issues an ingest success event.
 * <p>
 * This should occur at the end of ingest, after a SIP has been archived. It
 * will verify that the sip is completely accessible from the archive (this can
 * be disabled), confirm that the entities in the sip are present in the index (this
 * can be disabled) and will create an {@link Events#INGEST_SUCCESS} event. If this
 * event is to be archived, it will archive it.
 * </p>
 *
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>. Contains the sipStager from which the sip will be
 * pulled, and an event manager into which to create the archive event.</dd>
 * <dt>{@link #setArchiveStore(ArchiveStore)}</dt>
 * <dd><b>Required</b>. Specifies the archive store in which to archive ingest
 * success event.</dd>
 * <dt>{@link #setModelBuilder(DcsModelBuilder)}</dt>
 * <dd><b>Required</b>. Serializes the SIP into the archive format (DCP)</dd>
 * <dt>{@link #setVerifyArchiveEntities(boolean)}</dt>
 * <dd>Optional. If false, the finisher will not attempt to verify that all SIP
 * components are successfully retrievable from the archive. Default is true.</dd>
 * <dt>{@link #setConfirmIndexEntities(boolean)}</dt>
 * <dd>Optional.  If false, the finisher will not attempt to confirm that the entities in the
 * SIP are retrievable from the index.  Default is true.</dd>
 * <dt>{@link #setLookupQueryService(org.dataconservancy.dcs.query.api.LookupQueryService)}</dt>
 * <dd><b>Required</b>.  Used to communicate with the index.</dd>
 * <dt>{@link #setMaxPollTimeMillis(long)}</dt>
 * <dd>Optional.  Set the maximum amount of time to poll for entities in the index before giving up.  Default 2 minutes.</dd>
 * <td>{@link #setPollIntervalMillis(long)}</td>
 * <dd>Optional.  Set the time to wait between polling the index.  Default 5 seconds.</dd>
 * </dl>
 */
public class Finisher
        extends IngestServiceBase
        implements IngestService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private DcsModelBuilder builder;

    private ArchiveStore archive;

    private LookupQueryService<DcsEntity> lookupQueryService;

    private boolean verifyArchiveEntities = true;

    private boolean confirmIndexEntities = true;

    private long maxPollTimeMillis = 1000*60*2; // default 2 minutes

    private long pollIntervalMillis = 1000*5; // default 5 seconds

    @Required
    public void setModelBuilder(DcsModelBuilder mb) {
        builder = mb;
    }

    @Required
    public void setArchiveStore(ArchiveStore store) {
        archive = store;
    }

    public void setVerifyArchiveEntities(boolean verify) {
        verifyArchiveEntities = verify;
    }

    @Required
    public void setLookupQueryService(LookupQueryService<DcsEntity> lookupQueryService) {
        this.lookupQueryService = lookupQueryService;
    }

    public void setMaxPollTimeMillis(long maxPollTimeMillis) {
        if (maxPollTimeMillis < 1) {
            throw new IllegalArgumentException("Maximum polling time must be a positive integer");
        }
        this.maxPollTimeMillis = maxPollTimeMillis;

        if (this.pollIntervalMillis > maxPollTimeMillis) {
            this.pollIntervalMillis = maxPollTimeMillis;
        }
    }

    public long getMaxPollTimeMillis() {
        return maxPollTimeMillis;
    }

    public void setConfirmIndexEntities(boolean confirmIndexEntities) {
        this.confirmIndexEntities = confirmIndexEntities;
    }

    public void setPollIntervalMillis(long pollIntervalMillis) {
        if (pollIntervalMillis < 1) {
            throw new IllegalArgumentException("Poll interval must be a positive integer");
        }
        if (pollIntervalMillis > maxPollTimeMillis) {
            this.maxPollTimeMillis = pollIntervalMillis;
        }
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public long getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;
        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

       /* if (verifyArchiveEntities) {
            checkArchive(dcp);
        }

        if (confirmIndexEntities) {
            if (lookupQueryService == null) {
                throw new IllegalStateException("Lookup query service was null; cannot confirm that the entities" +
                        "have been indexed.  Either set a Lookup Query Service, or disable index confirmation.");
            }
            checkIndex(dcp);
        }
*/
        addSuccessEvent(sipRef, dcp);

        dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

        for (DcsEvent e : dcp.getEvents()) {
            if (Events.INGEST_SUCCESS.equals(e.getEventType())) {
                archiveEvent(e);
            }
        }
    }

    private void checkArchive(Dcp dcp) {
        for (DcsEntity e : getEntities(dcp)) {
            try {
                InputStream i = archive.getPackage(e.getId());
                verify(e, i);
                try {
                    i.close();
                } catch (IOException x) {

                }
            } catch (EntityNotFoundException x) {
                throw new RuntimeException("Entity " + x.getEntityId()
                        + " not found");
            }
        }
    }

    private void checkIndex(Dcp dcp) {
        List<String> entityIds = new ArrayList<String>();
        for (DcsEntity e : dcp) {
            entityIds.add(e.getId());
        }
        
        if (entityIds.isEmpty()) {
            return;
        }

        long elapsed = 0;
        do {
            Iterator<String> entityItr = entityIds.iterator();
            try {
                while (entityItr.hasNext()) {
                    final String id = entityItr.next();
                    if (lookupQueryService.lookup(id) != null) {
                        entityItr.remove();
                    }
                }
                Thread.sleep(pollIntervalMillis);
                elapsed = elapsed + pollIntervalMillis;
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (QueryServiceException e) {
                log.debug("Error communicating with the query service: " + e.getMessage(), e);
            }
        } while (elapsed < maxPollTimeMillis && entityIds.size() > 0);

        if (!entityIds.isEmpty()) {
            StringBuilder msg = new StringBuilder("Unable to confirm the presence of the following entities in the " +
                    "index after polling " + elapsed + "ms:");
            for (String id : entityIds) {
                msg.append(" [").append(id).append("]");
            }
            throw new RuntimeException(msg.toString());
        }
    }

    private void addSuccessEvent(String id, Dcp dcp) {
        DcsEvent success =
                ingest.getEventManager().newEvent(Events.INGEST_SUCCESS);
        for (DcsEntity e : getEntities(dcp)) {
            success.addTargets(new DcsEntityReference(e.getId()));
        }

        success.setOutcome(id);
        success.setDetail("Successfully completed ingest " + id);

        ingest.getEventManager().addEvent(id, success);
    }

    private void archiveEvent(DcsEvent e) {
        ResearchObject dcp = new ResearchObject();
        dcp.addEvent(e);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildSip(dcp, out);

        try {
            archive.putPackage(new ByteArrayInputStream(out.toByteArray()));
        } catch (AIPFormatException x) {
            throw new RuntimeException("Could not add ingest success event", x);
        }
    }

    private List<DcsEntity> getEntities(Dcp dcp) {
        List<DcsEntity> entities = new ArrayList<DcsEntity>();
        entities.addAll(dcp.getCollections());
        entities.addAll(dcp.getDeliverableUnits());
        entities.addAll(dcp.getEvents());
        entities.addAll(dcp.getFiles());
        entities.addAll(dcp.getManifestations());
        return entities;
    }

    private void verify(DcsEntity e, InputStream i) {
        try {
            Dcp archived = builder.buildSip(i);

            final List<DcsEntity> archivedEntities = getEntities(archived);
            if (archivedEntities.size() == 0) {
                throw new RuntimeException("Entity was not archived: " + e.getId() + " [" + e + "]");
            }
            if (!e.equals(archivedEntities.get(0))) {
                throw new RuntimeException("Archived entity does not match SIP: "
                        + e.getId());
            }
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }
}
