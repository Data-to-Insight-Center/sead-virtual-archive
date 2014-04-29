package org.dataconservancy.dcs.ingest.services;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.seadva.archive.SeadArchiveStore;
import org.seadva.model.SeadRepository;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SeadArchiver
        extends IngestServiceBase
        implements IngestService {

    private DcsModelBuilder builder;

    private SeadArchiveStore archive;
    private String type;

    private String name;

    @Required
    public void setModelBuilder(DcsModelBuilder mb) {
        builder = mb;
    }

    @Required
    public void setType(String type) {
        this.type = type;
    }

    @Required
    public void setName(String name)
    {
        this.name = name;
    }

    @Required
    public void setArchiveStore(SeadArchiveStore store) {
        archive = store;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;

        boolean matchesName = false;


        ResearchObject dcp = (ResearchObject)this.ingest.getSipStager().getSIP(sipRef);
        for (SeadRepository institionalRepository : dcp.getRepositories()) {
            if (institionalRepository.getType().equalsIgnoreCase(this.type))
            {
                String[] arr = this.name.split(";");
                for (int i = 0; i < arr.length; i++) {
                    if (institionalRepository.getName().equalsIgnoreCase(arr[i])) {
                        matchesName = true;
                    }
                }
            }
        }
        if (!matchesName) {
            return;
        }

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        SeadXstreamStaxModelBuilder builder = new SeadXstreamStaxModelBuilder();
        builder.buildSip((ResearchObject)dcp, sink);
        ResearchObject sip = null;
        try {
            sip = archive.putResearchPackage(new ByteArrayInputStream(sink.toByteArray()));
        } catch (AIPFormatException e) {
            throw new RuntimeException("Error depositing to repository", e);
        }
        ingest.getSipStager().updateSIP(sip,sipRef);
        addArchiveEvent(sipRef);
    }

    private void addArchiveEvent(String sipRef) {
        DcsEvent archiveEvent =
                ingest.getEventManager().newEvent(Events.ARCHIVE);

        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        Set<DcsEntityReference> entities = getEntities(dcp);

        archiveEvent.setOutcome(Integer.toString(entities.size()));
        archiveEvent.setDetail("Archived " + entities.size());// + " entities into "+ ((ResearchObject)dcp).getRepositories().iterator().next().getName());
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
