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

package org.seadva.registry.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.hibernate.Query;
import org.hibernate.Session;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.database.services.data.ApplicationContextHolder;
import org.seadva.registry.database.services.data.DataLayerVaRegistry;
import org.seadva.registry.database.services.data.DataLayerVaRegistryImpl;
import org.seadva.registry.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.sun.jersey.multipart.FormDataParam;

/**
 * REST interface
 */

@Component
@Configurable
@Path("/resource")
@TransactionConfiguration(defaultRollback=false)
public class ResourceService {

    static Gson gson;
    static DataLayerVaRegistry dataLayerVaRegistry;
    static final Session querySession;
    static {
        new DataLayerVaRegistryImpl().setApplicationContext(ApplicationContextHolder.getContext());
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        dataLayerVaRegistry = DataLayerVaRegistryImpl.getInstance();
        querySession  = dataLayerVaRegistry.createNewSession();
    }

     @GET
     @Path("/{entityId}")
     @Produces("application/json")
     public Response getEntity( @PathParam("entityId") String entityId) throws Exception {

         BaseEntity baseEntity = dataLayerVaRegistry.getBaseEntity(entityId);
         return Response.ok(gson.toJson(baseEntity)).build();
     }

    @GET
    @Path("/metadata/{typeId}")
    @Produces("application/json")
    public Response getType( @PathParam("typeId") String typeId) throws Exception {
        MetadataType metadataType = dataLayerVaRegistry.getMetadataType(typeId);

        return Response.ok(gson.toJson(metadataType)).build();
    }

    @GET
    @Path("/metadataType/{element}")
    @Produces("application/json")
    public Response getByType( @PathParam("element") String element) throws Exception {
        MetadataType metadataType;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT M from MetadataType M where M.metadataElement='"+element+"'");
            if(query.list().size()==0)
                throw new NotFoundException("Metadata type not found in metadata type registry");
            metadataType = (MetadataType) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(metadataType)).build();
    }

    @GET
    @Path("/identifiertype/{typename}")
    @Produces("application/json")
    public Response getIdentifierType( @PathParam("typename") String typeName) throws Exception {
        DataIdentifierType dataIdentifierType;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT D from DataIdentifierType D where D.dataIdentifierTypeName='"+typeName+"'");
            if(query.list().size()==0)
                throw new NotFoundException("DataIdentifer type not found in DataIdentifer type registry");
            dataIdentifierType = (DataIdentifierType) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(dataIdentifierType)).build();
    }


    @GET
    @Path("/relationType/{element}")
    @Produces("application/json")
    public Response getRelationByType( @PathParam("element") String element) throws Exception {
        RelationType relationType;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT R from RelationType R where R.relationElement='"+element+"'");
            if(query.list().size()==0)
                throw new NotFoundException("Relation type not found in relation type registry");
            relationType = (RelationType) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(relationType)).build();
    }

    @GET
    @Path("/roleType/{element}")
    @Produces("application/json")
    public Response getRoleByType( @PathParam("element") String element) throws Exception {
        RoleType roleType;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT R from RoleType R where R.roleTypeName='"+element+"'");
            if(query.list().size()==0)
                throw new NotFoundException("Role type not found in role type registry");
            roleType = (RoleType) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(roleType)).build();
    }

    @GET
    @Path("/profileType/{element}")
    @Produces("application/json")
    public Response getProfileByType( @PathParam("element") String element) throws Exception {
        ProfileType profileType;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT P from ProfileType P where P.profileTypeName='"+element+"'");
            if(query.list().size()==0)
                throw new NotFoundException("Profile type not found in role type registry");
            profileType = (ProfileType) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(profileType)).build();
    }

    @GET
    @Path("/repository/{name}")
    @Produces("application/json")
    public Response getRepositoryByName( @PathParam("name") String repoName) throws Exception {
        Repository repository;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT R from Repository R where R.repositoryName='"+repoName+"'");
            if(query.list().size()==0)
                throw new NotFoundException("No repository found matching the given name "+repoName+" in registry");
            repository = (Repository) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(repository)).build();
    }

    @GET
    @Path("/state/{name}")
    @Produces("application/json")
    public Response getStateByName( @PathParam("name") String stateName) throws Exception {
        State state;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT S from State S where S.stateType ='"+stateName+"'");
            if(query.list().size()==0)
                throw new NotFoundException("No state found matching the given name "+stateName+" in registry");
            state = (State) query.list().get(0);
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(state)).build();
    }

    @GET
    @Path("/aggregation/{entityId}")
    @Produces("application/json")
    public Response getAggregations( @PathParam("entityId") String entityId) throws Exception {
        List<AggregationWrapper> aggregationList;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT A from Aggregation A where A.id.parent='"+entityId+"'");
            aggregationList = new ArrayList<AggregationWrapper>();

            for(Object aggregation:query.list()){
                AggregationWrapper aggregationWrapper = new AggregationWrapper();
                BaseEntity child = ((Aggregation)aggregation).getId().getChild();
                BaseEntity parent = ((Aggregation)aggregation).getId().getParent();
                aggregationWrapper.setChild(child);
                aggregationWrapper.setParent(parent);
                aggregationWrapper.setChildType(child.getClassType().getName());
                aggregationWrapper.setParentType(parent.getClassType().getName());
                aggregationList.add(aggregationWrapper);
            }
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(aggregationList)).build();
    }


    @GET
    @Path("/listCollections/{type}")
    @Produces("application/json")
    public Response getAllCollections(@PathParam("type") String type,
                                      @QueryParam("submitterId") String submitterId, //Researcher who submitted Curation Object or Curator who submitted Published Object would be the submitters
                                      @QueryParam("creatorId") String creatorId,//Researcher who uploaded/created the data
                                      @QueryParam("repository") String repository, //Repository Name to which CurationObject is to be submitted or to which Published Object was already Published
                                      @QueryParam("fromDate") String fromDate,
                                      @QueryParam("toDate") String toDate) throws Exception {

        List<CollectionWrapper> finalCollectionWrappers;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();

            String queryStr = "SELECT C from Collection C";
            if(submitterId!=null||creatorId!=null)
                queryStr+=", Relation R";
            if(repository!=null)
                queryStr+=", DataLocation D";

            queryStr+=" where ";


            if(type!=null)
                queryStr += " C.state.stateType='"+type+"' ";


            if(submitterId!=null){
                if(type!=null)
                    queryStr+=" AND ";
                queryStr += " R.id.cause.id=C.id AND R.id.relationType.id='rl:3' AND R.id.effect.id='"+submitterId+"'"; //querying for submitter/publisher.
            }
            // Todo query from RelationType table instead of providing rl:2 as identifier

            if(creatorId!=null){
                if(type!=null)
                    queryStr+=" AND ";
                queryStr += " R.id.cause.id=C.id AND R.id.relationType.id='rl:2' AND R.id.effect.id='"+creatorId+"'"; //querying for submitter/publisher.
            }

            if(repository!=null) {
                if(type!=null || submitterId!=null)
                    queryStr+=" AND ";
                queryStr += "  C.id = D.id.entity.id AND D.id.locationType.repositoryName ='"+repository+"'";
            }

            Query query =  querySession.createQuery(queryStr);
            List<Collection> collectionsList = query.list();
            finalCollectionWrappers = new ArrayList<CollectionWrapper>();


            for(Collection collection:collectionsList){
                query =  querySession.createQuery("SELECT R from Relation R where R.id.cause.id='"+collection.getId()+"'");
                List<Relation> relationList = (List<Relation>)query.list();
                Set<Relation> newRelationList = new HashSet<Relation>();
                CollectionWrapper newCollectionWrapper = new CollectionWrapper(collection);
                newCollectionWrapper.setRelations(new HashSet<Relation>());

                for(Relation relation:relationList){
                    Relation newRelation = new Relation();
                    RelationPK newRelationPK = new RelationPK();

                    RelationPK relationPK = relation.getId();

                    RelationType relationType = new RelationType();
                    relationType.setId(relationPK.getRelationType().getId());
                    relationType.setRelationSchema(relationPK.getRelationType().getRelationSchema());
                    relationType.setRelationElement(relationPK.getRelationType().getRelationElement());
                    newRelationPK.setRelationType(relationType);

                    BaseEntity effectEntity = new BaseEntity();
                    effectEntity.setId(relationPK.getEffect().getId());
                    effectEntity.setEntityName(relationPK.getEffect().getEntityName());
                    effectEntity.setEntityCreatedTime(relationPK.getEffect().getEntityCreatedTime());
                    effectEntity.setEntityLastUpdatedTime(relationPK.getEffect().getEntityLastUpdatedTime());
                    newRelationPK.setEffect(effectEntity);

                    BaseEntity causeEntity = new BaseEntity();
                    causeEntity.setId(relationPK.getCause().getId());
                    causeEntity.setEntityName(relationPK.getCause().getEntityName());
                    causeEntity.setEntityCreatedTime(relationPK.getCause().getEntityCreatedTime());
                    causeEntity.setEntityLastUpdatedTime(relationPK.getCause().getEntityLastUpdatedTime());
                    newRelationPK.setCause(causeEntity);
                    RelationType rlType = relationPK.getRelationType();
                    newRelationPK.setRelationType(new RelationType(rlType.getId(), rlType.getRelationElement(), rlType.getRelationSchema()));
                    newRelation.setId(newRelationPK);

                    newRelationList.add(newRelation);
                }

                newCollectionWrapper.setRelations(newRelationList);
                finalCollectionWrappers.add(newCollectionWrapper);
            }
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(finalCollectionWrappers)).build();
    }


    @GET
    @Path("/fixity/{entityId}")
    @Produces("application/json")
    public Response getFixities( @PathParam("entityId") String entityId) throws Exception {
        List<Fixity> fixityList;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT F from Fixity F where F.id.entity='"+entityId+"'");
            fixityList = query.list();
            querySession.getTransaction().commit();
        }
        return Response.ok(gson.toJson(fixityList)).build();
    }
    @GET
    @Path("/relation/{entityId}")
    @Produces("application/json")
    public Response getRelations( @PathParam("entityId") String entityId) throws Exception {
        List<Relation> newRelationList;
        synchronized (querySession) {
            querySession.clear();
            querySession.beginTransaction();
            Query query =  querySession.createQuery("SELECT R from Relation R where R.id.cause.id='"+entityId+"'");
            List<Relation> relationList = (List<Relation>)query.list();
            newRelationList = new ArrayList<Relation>();

            for(Relation relation:relationList){
                Relation newRelation = new Relation();
                RelationPK newRelationPK = new RelationPK();

                RelationPK relationPK = relation.getId();

                RelationType relationType = new RelationType();
                relationType.setId(relationPK.getRelationType().getId());
                relationType.setRelationSchema(relationPK.getRelationType().getRelationSchema());
                relationType.setRelationElement(relationPK.getRelationType().getRelationElement());
                newRelationPK.setRelationType(relationType);

                BaseEntity effectEntity = new BaseEntity();
                effectEntity.setId(relationPK.getEffect().getId());
                effectEntity.setEntityName(relationPK.getEffect().getEntityName());
                effectEntity.setEntityCreatedTime(relationPK.getEffect().getEntityCreatedTime());
                effectEntity.setEntityLastUpdatedTime(relationPK.getEffect().getEntityLastUpdatedTime());
                newRelationPK.setEffect(effectEntity);

                BaseEntity causeEntity = new BaseEntity();
                causeEntity.setId(relationPK.getCause().getId());
                causeEntity.setEntityName(relationPK.getCause().getEntityName());
                causeEntity.setEntityCreatedTime(relationPK.getCause().getEntityCreatedTime());
                causeEntity.setEntityLastUpdatedTime(relationPK.getCause().getEntityLastUpdatedTime());
                newRelationPK.setCause(causeEntity);
                newRelationPK.setRelationType(relationPK.getRelationType());
                newRelation.setId(newRelationPK);

                newRelationList.add(newRelation);
            }
            querySession.getTransaction().commit();
        }
        String json = gson.toJson(newRelationList);
        return Response.ok(json).build();
    }


    @POST
    @Path("/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putResource(
            @QueryParam("entity") String entityJson,
            @QueryParam("type") String type
    ) throws IOException, ClassNotFoundException

    {
       BaseEntity baseEntity = (BaseEntity) gson.fromJson(entityJson, Class.forName(type));

        Set<Property> tempProperties =  new HashSet<Property>(baseEntity.getProperties());
        baseEntity.setProperties(new HashSet<Property>());
        Set<Property> newProperties =  new HashSet<Property>();

        for(Property property: tempProperties){
            property.setEntity(baseEntity);
            newProperties.add(property);
        }

        BaseEntity existingEntity = dataLayerVaRegistry.getBaseEntity(baseEntity.getId());
        if(existingEntity!=null)
            baseEntity.setProperties(existingEntity.getProperties(), newProperties);
        else
            baseEntity.setProperties(newProperties);

        Set<DataLocation> tempDataLocations =  new HashSet<DataLocation>(baseEntity.getDataLocations());
        baseEntity.setDataLocations(new HashSet<DataLocation>());
        Set<DataLocation> newDataLocations =  new HashSet<DataLocation>();

        for(DataLocation dataLocation: tempDataLocations){
            DataLocationPK dataLocationPK = dataLocation.getId();
            dataLocationPK.setEntity(baseEntity);
            dataLocation.setId(dataLocationPK);
            newDataLocations.add(dataLocation);
        }
        baseEntity.setDataLocations(newDataLocations);

        Set<DataIdentifier> tempDataIdentifiers =  new HashSet<DataIdentifier>(baseEntity.getDataIdentifiers());
        baseEntity.setDataIdentifiers(new HashSet<DataIdentifier>());
        Set<DataIdentifier> newDataIdentifiers =  new HashSet<DataIdentifier>();

        for(DataIdentifier dataIdentifier: tempDataIdentifiers){
            DataIdentifierPK dataIdentifierPK = dataIdentifier.getId();
            dataIdentifierPK.setEntity(baseEntity);
            dataIdentifier.setId(dataIdentifierPK);
            newDataIdentifiers.add(dataIdentifier);
        }
        baseEntity.setDataIdentifiers(newDataIdentifiers);



        if(baseEntity instanceof File){
            Set<Format> tempFormats =  new HashSet<Format>(((File) baseEntity).getFormats());
            ((File) baseEntity).setFormats(new HashSet<Format>());
            Set<Format> newFormats =  new HashSet<Format>();

            for(Format format: tempFormats){
                format.setEntity((File) baseEntity);
                newFormats.add(format);
            }
            ((File) baseEntity).setFormats(newFormats);
        }


//        if(baseEntity instanceof CollectionWrapper)
//            ((CollectionWrapper)baseEntity).setState(dataLayerVaRegistry.getState("state:1"));
        dataLayerVaRegistry.merge(baseEntity);      //solve this issue of trying to write again

        dataLayerVaRegistry.flushSession();

        return Response.ok().build();
    }


    @POST
    @Path("/agent/{agentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putAgent(
            @QueryParam("entity") String entityJson
    ) throws IOException, ClassNotFoundException

    {
        Agent agent = gson.fromJson(entityJson, Agent.class);

        Set<AgentRole> roles =  new HashSet<AgentRole>(agent.getAgentRoles());
        agent.setAgentRoles(new HashSet<AgentRole>());
        Set<AgentRole> newRoles =  new HashSet<AgentRole>();
        for(AgentRole role: roles){
            AgentRolePK agentRolePK = role.getId();
            agentRolePK.setAgent(agent);
            role.setId(agentRolePK);
            newRoles.add(role);
        }

        Set<AgentProfile> profiles =  new HashSet<AgentProfile>(agent.getAgentProfiles());
        agent.setAgentProfiles(new HashSet<AgentProfile>());
        Set<AgentProfile> newProfiles =  new HashSet<AgentProfile>();
        for(AgentProfile profile: profiles){
            AgentProfilePK agentProfilePK = profile.getId();
            agentProfilePK.setAgent(agent);
            profile.setId(agentProfilePK);
            newProfiles.add(profile);
        }

        agent.setAgentRoles(newRoles);
        agent.setAgentProfiles(newProfiles);

        dataLayerVaRegistry.merge(agent);
        dataLayerVaRegistry.flushSession();

        return Response.ok().build();
    }

    @POST
    @Path("/aggregation/{entityId}")
    @Consumes("application/json")
    @Transactional
    public Response postAggregations( @QueryParam("aggList") String aggregationJson
    ) throws IOException, ClassNotFoundException

    {
        Type listType = new TypeToken<ArrayList<AggregationWrapper>>() {
                    }.getType();
        List<AggregationWrapper> aggregationWrapperList = gson.fromJson(aggregationJson, listType);
        for(AggregationWrapper aggregationWrapper: aggregationWrapperList){
            Aggregation aggregation = new Aggregation();
            AggregationPK aggregationPK = new AggregationPK();
            aggregationPK.setParent(aggregationWrapper.getParent());
            aggregationPK.setChild(aggregationWrapper.getChild());
            aggregation.setId(aggregationPK);
            dataLayerVaRegistry.merge(aggregation);
            dataLayerVaRegistry.flushSession();
        }

        return Response.ok().build();
    }


    @POST
    @Path("/fixity")
    @Consumes("application/json")
    public Response postFixity( @QueryParam("fixityList") String fixityJson
    ) throws IOException, ClassNotFoundException

    {
        Type listType = new TypeToken<ArrayList<Fixity>>() {
        }.getType();
        List<Fixity> fixityList = gson.fromJson(fixityJson, listType);
        for(Fixity fixity: fixityList){
            dataLayerVaRegistry.merge(fixity);
            dataLayerVaRegistry.flushSession(); //seems like doesn't close session, so read is not working okay
        }

        return Response.ok().build();
    }

    @POST
    @Path("/relation")
    @Consumes("application/json")
    public Response postRelations( @QueryParam("relList") String relationListJson
    ) throws IOException, ClassNotFoundException

    {
        Type listType = new TypeToken<ArrayList<Relation>>() {
        }.getType();
        List<Relation> relationList = gson.fromJson(relationListJson, listType);
        for(Relation relation: relationList){
            dataLayerVaRegistry.merge(relation);
        }
        dataLayerVaRegistry.flushSession();
        return Response.ok().build();
    }

    @POST
    @Path("/delrelation")
    @Consumes("application/json")
    public Response deleteRelations( @QueryParam("relList") String relationListJson
    ) throws IOException, ClassNotFoundException
    {
        Type listType = new TypeToken<ArrayList<Relation>>() {
        }.getType();
        List<Relation> relationList = gson.fromJson(relationListJson, listType);
        for(Relation relation: relationList){
            dataLayerVaRegistry.delete(relation);
        }
        dataLayerVaRegistry.flushSession();
        return Response.ok().build();
    }

    @POST
    @Path("/obsolete/{entityId}")
    public Response makeObsolete( @PathParam("entityId") String obsoleteId
    ) throws IOException, ClassNotFoundException

    {
        BaseEntity baseEntity = dataLayerVaRegistry.getBaseEntity(obsoleteId);
       if(baseEntity instanceof Collection)
        {
            ((Collection)baseEntity).setIsObsolete(1);
            dataLayerVaRegistry.update(baseEntity);
            dataLayerVaRegistry.flushSession();
        }
        return Response.ok().build();
    }
}
