/*
#
# Copyright 2014 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: dcs-ingest-services
# File:  SeadRegistryIngester.java
# Description:  SeadRegistryIngester ingests metadata into Registry
#
# -----------------------------------------------------------------
#
*/

package org.dataconservancy.dcs.ingest.services;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.mapper.DcsDBMapper;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SeadRegistryIngester
        extends IngestServiceBase
        implements IngestService {

    String registryUrl;

    @Required
    public void setRegistryUrl(String registryUrl)
    {
        this.registryUrl = registryUrl;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;

        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

        try {
            System.out.println("SeadRegistryIngester.java: ");
            System.out.println(dcp.toString());
            new DcsDBMapper(this.registryUrl).mapfromSip(dcp);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        addRegisterEvent(sipRef);
    }

    private void addRegisterEvent(String sipRef) {
        DcsEvent archiveEvent =
                ingest.getEventManager().newEvent(Events.REGISTRY_INGEST);

        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        Set<DcsEntityReference> entities = getEntities(dcp);

        archiveEvent.setOutcome(Integer.toString(entities.size()));
        archiveEvent.setDetail("Stored in registry: " + entities.size());
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
