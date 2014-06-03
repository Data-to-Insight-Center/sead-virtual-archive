/*
 * Copyright 2014 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.data.lifecycle.service;

import com.google.gson.GsonBuilder;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import edu.indiana.d2i.komadu.axis2.client.KomaduServiceStub;
import edu.indiana.d2i.komadu.query.*;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.seadva.data.lifecycle.support.KomaduIngester;
import org.seadva.data.lifecycle.support.model.Entity;
import org.seadva.data.lifecycle.support.model.ROMetadata;
import org.seadva.data.lifecycle.service.util.Util;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.mapper.OreDBMapper;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.*;

/**
 * REST interface for RO subsystem which is an abstraction on VA registry and Komadu Provenance system
 */

@Path("/resource")
public class ResearchObjectService {

    private static String komaduServiceUrl;
    private static String registryServiceUrl;

    @Required
    public void setKomaduServiceUrl(String komaduServiceUrl){
        this.komaduServiceUrl = komaduServiceUrl;
    }

    @Required
    public void setRegistryServiceUrl(String registryServiceUrl){
        this.registryServiceUrl = registryServiceUrl;
    }


    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";

     @GET
     @Path("/ro/{entityId}")
     public Response getResearchObject( @PathParam("entityId") String roIdentifier) throws Exception {

         String resourceMapXml = "";
         ORESerialiser serial = ORESerialiserFactory.getInstance(RESOURCE_MAP_SERIALIZATION_FORMAT);
         ResourceMapDocument doc = serial.serialise(new OreDBMapper(registryServiceUrl).toORE(roIdentifier));
         resourceMapXml = doc.toString();
         return Response.ok(  resourceMapXml
         ).build();
     }

    @GET
    @Path("/agentGraph/{agentId}")
    @Produces("application/json")
    public Response getAgentGraph(@PathParam("agentId") String agentId){
        try {
            GetAgentGraphRequestDocument agentGraphRequest = GetAgentGraphRequestDocument.Factory.newInstance();
            GetAgentGraphRequestType agentRequestType = GetAgentGraphRequestType.Factory.newInstance();
            agentRequestType.setAgentID(agentId);
            agentGraphRequest.setGetAgentGraphRequest(agentRequestType);
            KomaduServiceStub serviceStub = new KomaduServiceStub(
                    komaduServiceUrl
            );
            GetAgentGraphResponseDocument agentResponse = serviceStub.getAgentGraph(agentGraphRequest);
            JSONObject xmlJSONObj = XML.toJSONObject(agentResponse.getGetAgentGraphResponse().getDocument().toString());
            String jsonPrettyPrintString = xmlJSONObj.toString(4);
            return Response.ok(
                           jsonPrettyPrintString ).build();
        } catch (RemoteException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (JSONException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


    @GET
    @Path("/lineage/{entityId}")
    @Produces("application/json")
    public Response getLineage( @PathParam("entityId") String roIdentifier) throws Exception {

        GetEntityGraphRequestDocument entityGraphRequest = GetEntityGraphRequestDocument.Factory.newInstance();
        GetEntityGraphRequestType entityRequestType = GetEntityGraphRequestType.Factory.newInstance();
        entityRequestType.setEntityURI(roIdentifier);
        entityRequestType.setEntityType(edu.indiana.d2i.komadu.query.EntityEnumType.COLLECTION);
        entityGraphRequest.setGetEntityGraphRequest(entityRequestType);

        KomaduServiceStub serviceStub = new KomaduServiceStub(
                komaduServiceUrl
        );
        GetEntityGraphResponseDocument entityResponse = serviceStub.getEntityGraph(entityGraphRequest);

        Util.pullParse(new ByteArrayInputStream(entityResponse.getGetEntityGraphResponse().getDocument().toString().getBytes(StandardCharsets.UTF_8)), "");

        Iterator iterator = Util.getGenUsed().entrySet().iterator();
        Map<String, List<String>> genUsedUrl = new HashMap<String, List<String>>();
        while (iterator.hasNext()){
            Map.Entry<String,String> pair = (Map.Entry<String, String>) iterator.next();
            List<String> tempList = new ArrayList<String>();
            if(genUsedUrl.containsKey(Util.getEntityMap().get(pair.getValue()).getUrl())) //parent
                tempList = genUsedUrl.get(Util.getEntityMap().get(pair.getValue()).getUrl());


            if(!tempList.contains(Util.getEntityMap().get(pair.getKey()).getUrl())) //child
                tempList.add( Util.getEntityMap().get(pair.getKey()).getUrl());

            genUsedUrl.put(Util.getEntityMap().get(pair.getValue()).getUrl(),
                    tempList);
        }


        Iterator iterator2 = genUsedUrl.entrySet().iterator();

        List<Entity> newList = new ArrayList<Entity>();
        while(iterator2.hasNext()){
            Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) iterator2.next();
            Entity entity = Util.getEntityUrlMap().get(pair.getKey());
            List<Entity> temp = new ArrayList<Entity>();
            for(String child:pair.getValue()){
                if(Util.getEntityUrlMap().containsKey(child))
                    temp.add(Util.getEntityUrlMap().get(child));
            }
            entity.setChildren(temp);
            newList.add(entity);
            iterator2.remove();
        }

        String json =  new GsonBuilder().create().toJson(newList);
        return Response.ok(
               json
        ).build();
    }


    @GET
    @Path("listPO")
    public Response getAllPublishedObjects() throws IOException, URISyntaxException, OREException, ClassNotFoundException {

        List<ROMetadata> roList = new ArrayList<ROMetadata>();

        List<org.seadva.registry.database.model.obj.vaRegistry.Collection> collections = new RegistryClient(registryServiceUrl).getCollections("PublishedObject");


        for(org.seadva.registry.database.model.obj.vaRegistry.Collection collection:collections){
            ROMetadata ro = new ROMetadata();
            ro.setIdentifier(collection.getId());
            ro.setName(collection.getEntityName());
            ro.setType(collection.getState().getStateName());
            ro.setUpdatedDate(collection.getEntityLastUpdatedTime().toString());
            roList.add(ro);
        }

        return Response.ok(new GsonBuilder().create().toJson(roList)).build();
    }

    @GET
    @Path("listCO")
    public Response getAllCurationObjects() throws IOException, URISyntaxException, OREException, ClassNotFoundException {

        List<ROMetadata> roList = new ArrayList<ROMetadata>();

        List<org.seadva.registry.database.model.obj.vaRegistry.Collection> collections = new RegistryClient(registryServiceUrl).getCollections("CurationObject");


        for(org.seadva.registry.database.model.obj.vaRegistry.Collection collection:collections){
            ROMetadata ro = new ROMetadata();
            ro.setIdentifier(collection.getId());
            ro.setName(collection.getEntityName());
            ro.setType(collection.getState().getStateName());
            ro.setUpdatedDate(collection.getEntityLastUpdatedTime().toString());
            roList.add(ro);
        }


        return Response.ok(new GsonBuilder().create().toJson(roList)).build();
    }


    /**
     * POST Methods
     */


    @POST
    @Path("/putro")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response putResearchObject(
            @FormDataParam("file") InputStream resourceMapStream,
            @FormDataParam("file") FormDataContentDisposition resourceMapDetail
    ) throws Exception {


        String directory =
                        System.getProperty("java.io.tmpdir");
        String oreFilePath = directory+"/_"+ UUID.randomUUID().toString()+".xml";
        IOUtils.copy(resourceMapStream, new FileOutputStream(oreFilePath));

        InputStream input = new FileInputStream(oreFilePath);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap resourceMap = parser.parse(input);

        new OreDBMapper(registryServiceUrl).mapfromOre(resourceMap);

        Map<String, List<String>> metadataMap = new ProvenanceAnalyzer().retrieveProv(resourceMap);

        Predicate DC_TERMS_TITLE = new Predicate();
        String titleTerm = "http://purl.org/dc/terms/title";
        DC_TERMS_TITLE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TITLE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TITLE.setName("title");
        DC_TERMS_TITLE.setURI(new URI(titleTerm));

        String thisEntityId = resourceMap.getAggregation().getURI().toString();
        String title = null;
        TripleSelector titleSelector = new TripleSelector();
        titleSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        titleSelector.setPredicate(DC_TERMS_TITLE);
        List<Triple> titleTriples = resourceMap.getAggregation().listAllTriples(titleSelector);

        if(titleTriples.size()>0){
            title = titleTriples.get(0).getObjectLiteral();
        }

        Iterator iterator = metadataMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,List<String>> pair = (Map.Entry<String, List<String>>) iterator.next();
            if(pair.getKey().contains("Revision")){
                for(String relatedEntityId: pair.getValue()){
                    Entity relatedEntity = new Entity();
                    relatedEntity.setId(relatedEntityId);

                    Entity thisEntity = new Entity();
                    thisEntity.setId(thisEntityId);
                    thisEntity.setName(title);

                    for(AggregatedResource resource:resourceMap.getAggregatedResources())
                    {
                        Entity memberEntity = new Entity();
                        memberEntity.setId(resource.getURI().toString());
                        titleTriples = resource.listAllTriples(titleSelector);
                        if(titleTriples.size()>0)
                            memberEntity.setName(titleTriples.get(0).getObjectLiteral());
                        thisEntity.addChild(memberEntity);
                    }
                    new KomaduIngester(komaduServiceUrl).trackRevision(relatedEntity,
                            thisEntity
                    );
                }
            }

            if(pair.getKey().contains("Derivation")){
                for(String relatedEntityId: pair.getValue()){
                    Entity relatedEntity = new Entity();
                    relatedEntity.setId(relatedEntityId);

                    Entity thisEntity = new Entity();
                    thisEntity.setId(thisEntityId);
                    thisEntity.setName(title);

                    for(AggregatedResource resource:resourceMap.getAggregatedResources())
                    {
                        Entity memberEntity = new Entity();
                        memberEntity.setId(resource.getURI().toString());
                        titleTriples = resource.listAllTriples(titleSelector);
                        if(titleTriples.size()>0)
                            memberEntity.setName(titleTriples.get(0).getObjectLiteral());
                        thisEntity.addChild(memberEntity);
                    }
                    new KomaduIngester(komaduServiceUrl).trackDerivation(relatedEntity,
                            thisEntity
                    );
                }
            }
            iterator.remove();
        }
        return Response.ok().build();
    }
}