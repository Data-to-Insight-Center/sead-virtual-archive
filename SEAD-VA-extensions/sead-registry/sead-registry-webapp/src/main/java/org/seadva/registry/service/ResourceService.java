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
import org.seadva.registry.database.factories.vaRegistry.VaRegistryDataPoolFactory;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.services.data.ApplicationContextHolder;
import org.seadva.registry.database.services.data.DataLayerVaRegistry;
import org.seadva.registry.database.services.data.DataLayerVaRegistryImpl;
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
    static DataLayerVaRegistry dataLayerVaRegistry;
    static Session querySession;
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
        Query query =  querySession.createQuery("SELECT M from MetadataType M where M.metadataElement='"+element+"'");
        if(query.list().size()==0)
            throw new NotFoundException("Metadata type not found in metadata type registry");
        MetadataType metadataType = (MetadataType) query.list().get(0);
        return Response.ok(gson.toJson(metadataType)).build();
    }

    @GET
    @Path("/aggregation/{entityId}")
    @Produces("application/json")
    public Response getAggregations( @PathParam("entityId") String entityId) throws Exception {

        Query query =  querySession.createQuery("SELECT A from Aggregation A where A.id.parent='"+entityId+"'");
        List<AggregationWrapper> aggregationList = new ArrayList<AggregationWrapper>();

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
      return Response.ok(gson.toJson(aggregationList)).build();
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
        baseEntity.setProperties(newProperties);

        if(baseEntity instanceof Collection)
            ((Collection)baseEntity).setState(dataLayerVaRegistry.getState("state:1"));
        dataLayerVaRegistry.merge(baseEntity);      //solve this issue of trying to write again

        dataLayerVaRegistry.flushSession();

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
            dataLayerVaRegistry.merge(aggregation);
        }

        dataLayerVaRegistry.flushSession();
        return Response.ok().build();
    }

}
