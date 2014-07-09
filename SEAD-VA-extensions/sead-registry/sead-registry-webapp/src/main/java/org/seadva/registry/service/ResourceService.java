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
import org.seadva.registry.database.common.DBConnectionPool;
import org.seadva.registry.database.model.dao.vaRegistry.*;
import org.seadva.registry.database.model.dao.vaRegistry.impl.*;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.transaction.TransactionConfiguration;

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
<<<<<<< HEAD
    static DataLayerVaRegistry dataLayerVaRegistry;
    static final Session querySession;
=======
  //  static DataLayerVaRegistry dataLayerVaRegistry;
    static BaseEntityDao baseEntityDao;
    static CollectionDao collectionEntityDao;
    static FileDao fileDao;
    static MetadataTypeDao metadataTypeDao;
    static DataIdentifierTypeDao dataIdentifierTypeDao;
    static RelationTypeDao relationTypeDao;
    static RoleTypeDao roleTypeDao;
    static RepositoryDao repositoryDao;
    static ProfileTypeDao profileTypeDao;
    static StateDao stateDao;
    static FixityDao fixityDao;
    static AggregationDao aggregationDao;
    static RelationDao relationDao;
    static AgentDao agentDao;

>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.
    static {

        try {
            DBConnectionPool.init("jdbc:mysql://localhost:3306/va_registry","username","pwd",8,30,0);
            DBConnectionPool.launch();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        baseEntityDao = new BaseEntityDaoImpl();
        collectionEntityDao = new CollectionDaoImpl();
        metadataTypeDao = new MetadataTypeDaoImpl();
        dataIdentifierTypeDao = new DataIdentifierTypeDaoImpl();
        relationTypeDao = new RelationTypeDaoImpl();
        roleTypeDao = new RoleTypeDaoImpl();
        repositoryDao = new RepositoryDaoImpl();
        profileTypeDao = new ProfileTypeDaoImpl();
        stateDao = new StateDaoImpl();
        fixityDao = new FixityDaoImpl();
        aggregationDao = new AggregationDaoImpl();
        relationDao = new RelationDaoImpl();
        fileDao = new FileDaoImpl();
        agentDao = new AgentDaoImpl();
    }

     @GET
     @Path("/collection/{entityId}")
     @Produces("application/json")
     public Response getEntity( @PathParam("entityId") String entityId) throws Exception {

         Collection entity = collectionEntityDao.getCollection(entityId);
         String json = gson.toJson(entity);
         return Response.ok(json).build();
     }

    @GET
    @Path("/file/{entityId}")
    @Produces("application/json")
    public Response getFile( @PathParam("entityId") String entityId) throws Exception {

        Collection entity = collectionEntityDao.getCollection(entityId);
        String json = gson.toJson(entity);
        return Response.ok(json).build();
    }



    @GET
    @Path("/metadataType/{typeId}")
    @Produces("application/json")
<<<<<<< HEAD
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
=======
    public Response getType( @PathParam("typeId") String typeId) throws Exception {
        MetadataType metadataType = metadataTypeDao.getMetadataType(typeId);
        if(metadataType==null)
            throw new NotFoundException("Role type not found in role type registry");
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.
        return Response.ok(gson.toJson(metadataType)).build();
    }



    @GET
    @Path("/identifiertype/{typename}")
    @Produces("application/json")
    public Response getIdentifierType( @PathParam("typename") String typeName) throws Exception {
<<<<<<< HEAD
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
=======
        DataIdentifierType dataIdentifierType = dataIdentifierTypeDao.getDataIdentifierType(typeName);
        if(dataIdentifierType==null)
            throw new NotFoundException("DataIdentifer type not found in DataIdentifer type registry");
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.
        return Response.ok(gson.toJson(dataIdentifierType)).build();
    }


    @GET
    @Path("/relationType/{element}")
    @Produces("application/json")
    public Response getRelationByType( @PathParam("element") String element) throws Exception {
<<<<<<< HEAD
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
=======

        RelationType relationType = relationTypeDao.getRelationType(element);
        if(relationType == null)
            throw new NotFoundException("Relation type not found in relation type registry");
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.
        return Response.ok(gson.toJson(relationType)).build();
    }

    @GET
    @Path("/roleType/{element}")
    @Produces("application/json")
    public Response getRoleByType( @PathParam("element") String element) throws Exception {
<<<<<<< HEAD
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
=======

        RoleType roleType = roleTypeDao.getRoleType(element);
        if(roleType == null)
            throw new NotFoundException("Role type not found in role type registry");
        return Response.ok(gson.toJson(roleType)).build();
    }

        @GET
        @Path("/profileType/{element}")
        @Produces("application/json")
        public Response getProfileByType( @PathParam("element") String element) throws Exception {
            ProfileType profileType = profileTypeDao.getProfileType(element);
            if(profileType==null)
                throw new NotFoundException("Profile type not found in role type registry");
            return Response.ok(gson.toJson(profileType)).build();
        }
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.

        @GET
        @Path("/repository/{name}")
        @Produces("application/json")
        public Response getRepositoryByName( @PathParam("name") String repoName) throws Exception {
            Repository repository = repositoryDao.getRepository(repoName);
            if(repository==null)
                throw new NotFoundException("No repository found matching the given name "+repoName+" in registry");
            return Response.ok(gson.toJson(repository)).build();
        }

<<<<<<< HEAD
            if(type!=null)
                queryStr += " C.state.stateType='"+type+"' ";
=======
        @GET
        @Path("/state/{name}")
        @Produces("application/json")
        public Response getStateByName( @PathParam("name") String stateName) throws Exception {
            State state = stateDao.getState(stateName);
            if(state == null)
                throw new NotFoundException("No state found matching the given name "+stateName+" in registry");
            return Response.ok(gson.toJson(state)).build();
        }
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.

        @GET
        @Path("/aggregation/{entityId}")
        @Produces("application/json")
        public Response getAggregations( @PathParam("entityId") String entityId) throws Exception {

<<<<<<< HEAD
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
=======
            List<AggregationWrapper> aggregationList = aggregationDao.getAggregations(entityId);
            return Response.ok(gson.toJson(aggregationList)).build();
        }

        @GET
        @Path("/listCollections/{type}")
        @Produces("application/json")
        public Response getAllCollections(@PathParam("type") String type,
                                          @QueryParam("submitterId") String submitterId, //Researcher who submitted Curation Object or Curator who submitted Published Object would be the submitters
                                          @QueryParam("repository") String repository, //Repository Name to which CurationObject is to be submitted or to which Published Object was already Published
                                          @QueryParam("fromDate") String fromDate,
                                          @QueryParam("toDate") String toDate) throws Exception {

            List<CollectionWrapper> finalCollectionWrappers = new ArrayList<CollectionWrapper>();
            List<Collection> collections = collectionEntityDao.listCollections(submitterId, repository, type);
            for(Collection collection:collections){

                List<Relation> relationList =  relationDao.getRelations(collection.getId());
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
            return Response.ok(gson.toJson(finalCollectionWrappers)).build();
        }

>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.

        @GET
        @Path("/fixity/{entityId}")
        @Produces("application/json")
        public Response getFixities( @PathParam("entityId") String entityId) throws Exception {
            List<Fixity> fixityList = fixityDao.getFixities(entityId);
            return Response.ok(gson.toJson(fixityList)).build();
        }

        @GET
        @Path("/relation/{entityId}")
        @Produces("application/json")
        public Response getRelations( @PathParam("entityId") String entityId) throws Exception {

<<<<<<< HEAD
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
=======
            List<Relation> relationList =  relationDao.getRelations(entityId);
            Set<Relation> newRelationList = new HashSet<Relation>();
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.

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
<<<<<<< HEAD
            querySession.getTransaction().commit();
        }
        String json = gson.toJson(newRelationList);
        return Response.ok(json).build();
    }
=======

            String json = gson.toJson(newRelationList);
            return Response.ok(json).build();
        }
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.


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

            BaseEntity existingEntity = baseEntityDao.getBaseEntity(baseEntity.getId());
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



            //Todo
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
            if(baseEntity instanceof Collection)
                collectionEntityDao.insertCollection((Collection)baseEntity);
            else if(baseEntity instanceof File)
                fileDao.insertFile((File)baseEntity);
            else
                baseEntityDao.insertEntity(baseEntity);

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
            agentDao.putAgent(agent);
            return Response.ok().build();
        }

        @POST
        @Path("/aggregation/{entityId}")
        @Consumes("application/json")
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
                aggregationDao.putAggregation(aggregation);
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
            fixityDao.putFixities(fixityList);
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
            List<Relation> relationList =  gson.fromJson(relationListJson, listType);
            for(Relation relation: relationList){
                relationDao.putRelation(relation);
            }
            return Response.ok().build();
        }

<<<<<<< HEAD
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
=======
        @POST
        @Path("/obsolete/{entityId}")
        public Response makeObsolete( @PathParam("entityId") String entityId) throws IOException, ClassNotFoundException
>>>>>>> d443df5... Registry using direct JDBC calls instead of  using hibernate calls.

        {
            baseEntityDao.updateEntity(entityId, 1);
            return Response.ok().build();
        }
    }
