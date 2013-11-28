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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.springframework.beans.factory.annotation.Required;

/**
 * Sends a SIP to the archive service.
 * <p>
 * Associates an 'archive' event with the SIP, creates a DCP serialization of a
 * SIP, and sumbits that stream to the archive service via
 * {@link ArchiveStore#putPackage(java.io.InputStream)}.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>. Contains the sipStager from which the sip will be
 * pulled, and an event manager into which to create the archive event.</dd>
 * <dt>{@link #setArchiveStore(ArchiveStore)}</dt>
 * <dd><b>Required</b>. Specifies the archive store in which to archive the SIP.
 * </dd>
 * <dt>{@link #setModelBuilder(DcsModelBuilder)}</dt>
 * <dd><b>Required</b>. Serializes the SIP into the archive format (DCP)</dd>
 * </dl>
 */
public class Archiver
        extends IngestServiceBase
        implements IngestService {

    private DcsModelBuilder builder;

    private ArchiveStore archive;

    @Required
    public void setModelBuilder(DcsModelBuilder mb) {
        builder = mb;
    }

    @Required
    public void setArchiveStore(ArchiveStore store) {
        archive = store;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;

        addArchiveEvent(sipRef);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        builder.buildSip(dcp, sink);

        try {
            archive.putPackage(new ByteArrayInputStream(sink.toByteArray()));
        } catch (AIPFormatException e) {
            throw new RuntimeException("Error storing to archive", e);
        }
    }

    private void addArchiveEvent(String sipRef) {
        DcsEvent archiveEvent =
                ingest.getEventManager().newEvent(Events.ARCHIVE);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        Set<DcsEntityReference> entities = getEntities(dcp);

        archiveEvent.setOutcome(Integer.toString(entities.size()));
        archiveEvent.setDetail("Archived " + entities.size() + " entities");
        archiveEvent.setTargets(entities);

        ingest.getEventManager().addEvent(sipRef, archiveEvent);
    }

    private Set<DcsEntityReference> getEntities(Dcp dcp) {
        Set<DcsEntityReference> entities = new HashSet<DcsEntityReference>();
        addRefs(entities, dcp.getCollections());
        addRefs(entities, dcp.getDeliverableUnits());
        addRefs(entities, dcp.getEvents());
        addRefs(entities, dcp.getFiles());
        addRefs(entities, dcp.getManifestations());

        return entities;
    }

    private void addRefs(Collection<DcsEntityReference> refs,
                         Collection<? extends DcsEntity> entities) {
        for (DcsEntity e : entities) {
            refs.add(new DcsEntityReference(e.getId()));
        }
    }

}
