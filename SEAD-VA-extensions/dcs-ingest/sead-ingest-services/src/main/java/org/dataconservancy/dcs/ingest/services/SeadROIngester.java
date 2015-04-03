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
# File:  SeadROIngester.java
# Description:  SeadROIngester ingests Research Object into RO subsystem's Registry and Komadu
#
# -----------------------------------------------------------------
#
*/

package org.dataconservancy.dcs.ingest.services;

import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.services.runners.RulesExecutorBootstrap;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.util.Constants;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

public class SeadROIngester
        extends IngestServiceBase
        implements IngestService {

    private String roSubsystemUrl;

    @Required
    public void setRoSubsystemUrl(String roSubsystemUrl) {
        this.roSubsystemUrl = roSubsystemUrl;
    }



    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) return;

        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

        DcsDeliverableUnit rootDu = null;

        for (DcsDeliverableUnit du:dcp.getDeliverableUnits()){
            if(du.getParents()==null||du.getParents().size()==0){
                rootDu = du;
                break;
            }
        }
        File targetDir = new File(System.getProperty("java.io.tmpdir"));

        PackageDescriptor packageDescriptor = new PackageDescriptor(
                rootDu.getTitle(),null,targetDir.getAbsolutePath()
        );

        String directory =
                System.getProperty("java.io.tmpdir");

        String sipFilePath = directory+"/_"+ UUID.randomUUID().toString()+".xml";

        try {
            new SeadXstreamStaxModelBuilder().buildSip(dcp, new FileOutputStream(sipFilePath));

            packageDescriptor.setSipPath(
                    sipFilePath
            );
            new ConfigBootstrap().load();
            packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_SIP, packageDescriptor);
            packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);


        } catch (ClassNotFoundException e) {
            throw new IngestServiceException("Unable to generate ORE from SIP.");
        } catch (IllegalAccessException e) {
            throw new IngestServiceException("Unable to generate ORE from SIP.");
        } catch (InstantiationException e) {
            throw new IngestServiceException("Unable to generate ORE from SIP.");
        } catch (FileNotFoundException e) {
            throw new IngestServiceException("Unable to generate ORE from SIP.");
        }

        WebResource webResource = Client.create().resource(this.roSubsystemUrl);

        File file = new File(
                packageDescriptor.getOreFilePath()
        );
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource.path("resource")
                .path("putro")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);

        if(response.getStatus()!=200&&response.getStatus()!=202)
            throw new IngestServiceException("Unable to register in RO subsystem");


        // Setting 'Publication Date' and reset 'Proposed for Publication' flag in ACR
        if(rootDu.getType().equals("PublishedObject")){

            Collection<DcsMetadata> metadata = rootDu.getMetadata();
            String acrLocation = null;
            for(DcsMetadata data : metadata) {
                String meta = data.getMetadata();
                if(meta.contains("http://purl.org/dc/terms/Location") &&  meta.contains("ncsa")
                        && meta.contains("tag")){
                    String[] split = meta.split("<string>");
                    meta = split[2];
                    acrLocation = meta.substring(0, meta.indexOf("</string>"));
                    break;
                }

            }

            if(acrLocation != null) {
                String uri = acrLocation.split("#collection")[0];
                String id = acrLocation.split("#collection\\?uri=")[1];

                String username = "";
                String password = "";
                List<MediciInstance> mediciInstanceList = Constants.acrInstances;
                for(MediciInstance mediciInstance : mediciInstanceList){
                    if(uri.contains(mediciInstance.getUrl())){
                        username = mediciInstance.getUser();
                        password = mediciInstance.getPassword();
                    }

                }

                String update_url = uri + "resteasy/collections/" + URLEncoder.encode(id) + "/published";
                Client client = Client.create();
                client.addFilter(new HTTPBasicAuthFilter(username, password));
                WebResource service = client.resource(update_url);

                response = service
                        .type(MediaType.MULTIPART_FORM_DATA)
                        .put(ClientResponse.class, "");

                int response_code = response.getStatus() ;

                if(response_code == HttpURLConnection.HTTP_OK) {
                    System.out.println("Published Date successfully updated in ACR");
                } else if(response_code == HttpURLConnection.HTTP_FORBIDDEN){
                    System.out.println("Error updating Published Date in ACR : Do not have permission to edit metadata");
                } else if(response_code == HttpURLConnection.HTTP_CONFLICT){
                    System.out.println("Error updating Published Date in ACR : 'Proposed for Publication' flag is not set");
                }

            }

        }

        addRegisterEvent(sipRef, dcp);
    }

    private void addRegisterEvent(String sipRef, ResearchObject dcp) {
        DcsEvent archiveEvent =
                ingest.getEventManager().newEvent(Events.RO_INGEST);

        Set<DcsEntityReference> entities = getEntities(dcp);
        archiveEvent.setOutcome(Integer.toString(entities.size()));
        archiveEvent.setDetail("Stored in RO subsystem: " + entities.size());
        archiveEvent.setTargets(entities);

        ingest.getEventManager().addEvent(sipRef, archiveEvent);
        String agendId = null;
        String targetId = null;
        String eventType = null;
        for(DcsDeliverableUnit du:dcp.getDeliverableUnits()){
            if(du.getParents()==null||du.getParents().size()==0)
            {
                agendId = ((SeadDeliverableUnit)du).getSubmitter().getId();
                targetId = du.getId();
                if(du.getType()!=null){
                    if(du.getType().contains("Curation"))
                        eventType = "Curation-Workflow";
                    else
                        eventType = "Publish-Workflow";
                }
                else
                    eventType = "Publish-Workflow";
            }
        }
        try {
            trackEventinRO(archiveEvent, sipRef, agendId, targetId, eventType);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IngestServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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

    public void trackEventinRO(DcsEvent dcsEvent, String sipRef, String agentId, String targetId, String eventType) throws ParseException, IngestServiceException {
        org.seadva.data.lifecycle.support.model.Event event = new org.seadva.data.lifecycle.support.model.Event();
        event.setEventIdentifier(dcsEvent.getId());
        event.setLinkingAgentIdentifier(agentId);
        event.setTargetId(targetId);
        event.setWorkflowId(sipRef);
        event.setEventType(eventType);

        String eventJson = new GsonBuilder().create().toJson(event);

        WebResource webResource = Client.create().resource(
                this.roSubsystemUrl
        );

        ClientResponse response = webResource.path("resource")
                .path("putEvent")
                .queryParam("event", eventJson)
                .post(ClientResponse.class);

        if(response.getStatus()!=200&&response.getStatus()!=202)
            throw new IngestServiceException("Unable to track event in RO subsystem");
    }

}
