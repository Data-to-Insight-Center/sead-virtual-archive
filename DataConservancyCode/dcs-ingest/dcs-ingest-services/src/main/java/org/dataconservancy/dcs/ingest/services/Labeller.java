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

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

/**
 * Assigns a DCS identity to each component in a SIP.
 */
public class Labeller
        extends IngestServiceBase
        implements IngestService {

    private IdService idService;

    private BulkIdCreationService bulkIdService;

    private String idPrefix = "http";

    @Required
    public void setIdentifierService(IdService ids) {
        idService = ids;
    }

    @Required
    public void setIdPrefix(String pfx) {
        idPrefix = pfx;
    }

    @Required
    public void setBulkIdService(BulkIdCreationService bulkIdService) {
        this.bulkIdService = bulkIdService;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;
        Dcp sip = ingest.getSipStager().getSIP(sipRef);

        Map<String, String> idMap = new HashMap<String, String>();
        List<DcsEvent> events = new ArrayList<DcsEvent>();
        Iterator<Identifier> idItr;
        System.out.println("---------------------" + ((SeadDeliverableUnit)((ResearchObject) sip).getDeliverableUnits().iterator().next()).getAbstrct()+"------------------------");

        Collection<DcsCollection> collections = sip.getCollections();
        if (collections.size() > 0) {
            idItr = idIteratorForType(collections.size(), Types.COLLECTION.getTypeName());
            for (DcsCollection c : collections) {
                events.addAll(assignId(c, idItr, idMap));
            }
            sip.setCollections(collections);
        }

        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        if (dus.size() > 0) {
            idItr = idIteratorForType(dus.size(), Types.DELIVERABLE_UNIT.getTypeName());
            for (DcsDeliverableUnit d : dus) {
                events.addAll(assignId(d, idItr, idMap));
            }
            sip.setDeliverableUnits(dus);
        }

        Collection<DcsManifestation> mans = sip.getManifestations();
        if (mans.size() > 0) {
            idItr = idIteratorForType(mans.size(), Types.MANIFESTATION.getTypeName());
            for (DcsManifestation m : mans) {
                events.addAll(assignId(m, idItr, idMap));
            }
            sip.setManifestations(mans);
        }

        Collection<DcsEvent> evs = sip.getEvents();
        if (evs.size() > 0) {
            idItr = idIteratorForType(evs.size(), Types.EVENT.getTypeName());
            for (DcsEvent e : evs) {
                events.addAll(assignId(e, idItr, idMap));
            }
            sip.setEvents(evs);
        }

        Collection<DcsFile> files = sip.getFiles();
        if (files.size() > 0) {
            idItr = idIteratorForType(files.size(), Types.FILE.getTypeName());
            for (DcsFile f : files) {
                events.addAll(assignId(f, idItr, idMap));
            }
            sip.setFiles(files);
        }

        /* save the SIP containing updated entities */
        ingest.getSipStager().updateSIP(sip, sipRef);

        /* persist the id assignment events */
        for (DcsEvent e : events) {
            ingest.getEventManager().addEvent(sipRef, e);
        }

        /* Update internal references, and save */
        sip = updateReferences(ingest.getSipStager().getSIP(sipRef), idMap);
        ingest.getSipStager().updateSIP((ResearchObject)sip, sipRef);

    }

    private Collection<DcsEvent> assignId(DcsEntity e,
                                          Iterator<Identifier> idItr,
                                          Map<String, String> idMap) {

        final List<DcsEvent> events = new ArrayList<DcsEvent>();
        final Identifier id = idItr.next();

        /*
         * If we encounter an identifier of a potentially-already-existing DC
         * object, do some investigation...
         */
        if (e.getId().startsWith(idPrefix)) {
            /* TODO: should really use access svc for this */
            try {
                Identifier assignedId = idService.fromUrl(new URL(e.getId()));
                if (assignedId.getType().equals(id.getType())) {
                    /* This id has already been assigned. Keep it */
                    return events;
                }
            } catch (Exception x) {
                /* Not a DC id. Make a new one */
            }
        }

        /* Map a new id */
        String idString = id.getUrl().toString();
        String oldid = e.getId();
        idMap.put(oldid, idString);
        e.setId(idString);

        /* Add an id assignment event */
        DcsEvent idAssign =
                ingest.getEventManager().newEvent(Events.ID_ASSIGNMENT);
        idAssign.setOutcome(String.format("%s to %s", oldid, idString));
        idAssign.setDetail("Assigned identifier '" + idString + "' to " + id.getType() + " '"
                + oldid + "'");
        idAssign.addTargets(new DcsEntityReference(idString));
        events.add(idAssign);

        return events;
    }

    private Dcp updateReferences(Dcp sip, Map<String, String> idMap) {

        Collection<DcsCollection> collections = sip.getCollections();
        for (DcsCollection c : collections) {

            Collection<DcsMetadataRef> mdRefs = c.getMetadataRef();
            for (DcsMetadataRef m : mdRefs) {
                updateReference(m, idMap);
            }
            c.setMetadataRef(mdRefs);

            if (c.getParent() != null) {
                c.setParent((DcsCollectionRef) updateReference(c.getParent(),
                                                               idMap));
            }
        }
        sip.setCollections(collections);

        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        for (DcsDeliverableUnit d : dus) {

            Collection<DcsMetadataRef> mdRefs = d.getMetadataRef();
            for (DcsMetadataRef m : mdRefs) {
                updateReference(m, idMap);
            }
            d.setMetadataRef(mdRefs);

            Collection<DcsDeliverableUnitRef> duRefs = d.getParents();
            for (DcsDeliverableUnitRef r : duRefs) {
                updateReference(r, idMap);
            }
            d.setParents(duRefs);

            Collection<DcsRelation> rels = d.getRelations();
            for (DcsRelation r : rels) {
                r.setRef(updateReference(r.getRef(), idMap));
            }
            d.setRelations(rels);

            Collection<DcsCollectionRef> cols = d.getCollections();
            for (DcsCollectionRef cr : cols) {
                updateReference(cr, idMap);
            }
            d.setCollections(cols);

            Collection<DcsDeliverableUnitRef> parents = d.getParents();
            for (DcsDeliverableUnitRef dur : parents) {
                updateReference(dur, idMap);
            }
            d.setParents(parents);
        }

        sip.setDeliverableUnits(dus);

        Collection<DcsManifestation> mans = sip.getManifestations();
        for (DcsManifestation m : mans) {

            Collection<DcsMetadataRef> mdRefs = m.getMetadataRef();
            for (DcsMetadataRef md : mdRefs) {
                updateReference(md, idMap);
            }
            m.setMetadataRef(mdRefs);

            Collection<DcsManifestationFile> mfs = m.getManifestationFiles();
            for (DcsManifestationFile mf : mfs) {
                mf.setRef((DcsFileRef) updateReference(mf.getRef(), idMap));
            }
            m.setManifestationFiles(mfs);

            /* XXX getDeliverableUnit() should really be a DcsEntityRef */
            if (idMap.containsKey(m.getDeliverableUnit())) {
                m.setDeliverableUnit(idMap.get(m.getDeliverableUnit()));
            }
        }
        sip.setManifestations(mans);

        Collection<DcsEvent> evs = sip.getEvents();
        for (DcsEvent e : evs) {
            Collection<DcsEntityReference> targets = e.getTargets();
            for (DcsEntityReference t : targets) {
                updateReference(t, idMap);
            }
            e.setTargets(targets);
        }
        sip.setEvents(evs);

        Collection<DcsFile> files = sip.getFiles();
        for (DcsFile f : files) {

            Collection<DcsMetadataRef> mdRefs = f.getMetadataRef();
            for (DcsMetadataRef m : mdRefs) {
                updateReference(m, idMap);
            }
            f.setMetadataRef(mdRefs);
        }
        sip.setFiles(files);
        return sip;
    }

    private DcsEntityReference updateReference(DcsEntityReference r,
                                               Map<String, String> idMap) {
        if (idMap.containsKey(r.getRef())) {
            r.setRef(idMap.get(r.getRef()));
        }
        return r;
    }

    private Iterator<Identifier> idIteratorForType(int count, String type) {
        return this.bulkIdService.create(count, type).iterator();
    }
}
