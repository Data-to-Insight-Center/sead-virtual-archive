package org.dataconservancy.dcs.ingest.services;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.seadva.archive.SeadArchiveStore;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadRepository;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayInputStream;
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
    private String id;
    private String communityUrl;

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
    public void setId(String id)
    {
        this.id = id;
    }

    @Required
    public void setCommunityUrl(String communityUrl)
    {
        this.communityUrl = communityUrl;
    }

    @Required
    public void setArchiveStore(SeadArchiveStore store) {
        archive = store;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;

        ResearchObject dcp = (ResearchObject)this.ingest.getSipStager().getSIP(sipRef);
        Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
        String roType = null;
        for(DcsDeliverableUnit du:dus){
            if(du.getParents()==null||du.getParents().size()==0){
                roType = du.getType();
                if(du.getType().equalsIgnoreCase("CurationObject")){
                    SeadDataLocation dataLocation = new SeadDataLocation();
                    dataLocation.setName(this.name);
                    dataLocation.setLocation(this.communityUrl);//you can even store filePath/temporary location
                    dataLocation.setType(this.type);
                    ((SeadDeliverableUnit)du).setPrimaryLocation(dataLocation);
                    break;
                }
                else{
                    ((SeadDeliverableUnit)du).setPrimaryLocation(new SeadDataLocation());
                    SeadRepository repository = new SeadRepository();
                    repository.setType(this.type);
                    repository.setName(this.name);
                    repository.setIrId(this.id);
                    repository.setUrl(this.communityUrl);
                    dcp.addRepository(repository);
                }
                break;
            }
        }

        dcp.setDeliverableUnits(dus);
        ingest.getSipStager().updateSIP(dcp, sipRef);

        if(roType!=null&& (roType.equalsIgnoreCase("PublishedObject"))){

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
