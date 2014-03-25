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

package org.seadva.registry.impl.registry;

import org.seadva.registry.api.Registry;
import org.seadva.registry.api.Resource;
import org.seadva.registry.api.ResourceType;
import org.seadva.registry.dao.*;
import org.seadva.registry.impl.resource.Constants;
import org.seadva.registry.impl.resource.ContainerResource;

import java.util.*;

/**
 * Read and write Container resource to registry
 */
public class ContainerRegistry implements Registry {

    BaseDaoImpl baseDao;
    ContainerRegistry(BaseDaoImpl baseDao){
        this.baseDao = baseDao;
    }

    Map<String,List<AggregationDao>> aggregationDaos;
    Map<String,List<RelationDao>> relationDaos;
    @Override
    public Resource getResource(String entityId) {
        try {
            EntityDao entityDao = this.baseDao.entityDaoImpl.getEntity(entityId);

            aggregationDaos = new HashMap<String, List<AggregationDao>>();
            getAggregations(entityId);

            relationDaos = new HashMap<String, List<RelationDao>>();
            getRelations(entityId);


            Map<String,List<EntityDao>> entities = new HashMap<String,List<EntityDao>>();
            Map<String,List<PropertyDao>> properties = new HashMap<String,List<PropertyDao>>();

            properties.put(entityId, this.baseDao.propertyDaoImpl.getPropertyForEntity(entityId));

            Map<String,List<EntityTypeDao>> entityTypes = new HashMap<String, List<EntityTypeDao>>();

            entityTypes.put(entityId,this.baseDao.entityTypeDaoImpl.getEntity(entityId));

            if(aggregationDaos!=null){
                Iterator aggItr = aggregationDaos.entrySet().iterator();
                while(aggItr.hasNext()){
                    Map.Entry<String, List<AggregationDao>> pair = (Map.Entry<String, List<AggregationDao>>) aggItr.next();
                    for(AggregationDao agg:pair.getValue()){
                        properties.put(agg.getChild_id(), this.baseDao.propertyDaoImpl.getPropertyForEntity(agg.getChild_id()));
                        entityTypes.put(agg.getChild_id(),this.baseDao.entityTypeDaoImpl.getEntity(agg.getChild_id()));
                        List<EntityDao> temp = new ArrayList<EntityDao>();
                        temp.add(this.baseDao.entityDaoImpl.getEntity(agg.getChild_id()));
                        entities.put(agg.getChild_id(), temp);
                        getRelations(agg.getChild_id());
                    }
                }
            }


            if(relationDaos!=null){
                Iterator relItr = relationDaos.entrySet().iterator();
                while(relItr.hasNext()){
                    Map.Entry<String, List<RelationDao>> pair = (Map.Entry<String, List<RelationDao>>) relItr.next();
                    for(RelationDao relationDao:pair.getValue()){
                        properties.put(relationDao.getEffect_id(), this.baseDao.propertyDaoImpl.getPropertyForEntity(relationDao.getEffect_id()));
                        entityTypes.put(relationDao.getEffect_id(), this.baseDao.entityTypeDaoImpl.getEntity(relationDao.getEffect_id()));
                        List<EntityDao> temp = new ArrayList<EntityDao>();
                        temp.add(this.baseDao.entityDaoImpl.getEntity(relationDao.getEffect_id()));
                        entities.put(relationDao.getEffect_id(), temp);
                    }
                }
            }



            ContainerResource containerResource = new ContainerResource();
            containerResource.setEntity(entityDao);
            containerResource.setChildEntities(entities);
            containerResource.setAggregations(aggregationDaos);
            containerResource.setRelations(relationDaos);
            containerResource.setProperties(properties);
            containerResource.setEntityType(entityTypes);
            return containerResource;

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new ContainerResource();  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void getAggregations(String parentId) throws Exception {

        List<AggregationDao> children = this.baseDao.aggregationDaoImpl.getAggregationForParent(parentId);
        if(children!=null) {
            List<AggregationDao> tempChildren = aggregationDaos.get(parentId);
            if(tempChildren==null)
                tempChildren = new ArrayList<AggregationDao>();
            tempChildren.addAll(children);
            aggregationDaos.put(parentId,tempChildren);
        }
        for(AggregationDao child: children)
            getAggregations(child.getChild_id());
    }

    private void getRelations(String entityId) throws Exception {

        List<RelationDao> relations = this.baseDao.relationDaoImpl.getRelationForEntity(entityId);
        if(relations!=null)
            relationDaos.put(entityId,relations);
        for(RelationDao rel: relations)
            getRelations(rel.getEffect_id());
    }

    @Override
    public boolean putResource(ResourceType resourceType, Resource resource) {
        try {
            ContainerResource containerResource = (ContainerResource)resource;

            EntityDao entityDao = containerResource.getEntity();
            Map<String, List<EntityDao>> childEntities = containerResource.getChildEntities();
            Map<String, List<EntityTypeDao>> entityTypes = containerResource.getEntityType();
            Map<String,List<PropertyDao>> properties = containerResource.getProperties();
            Map<String,List<AggregationDao>> aggregations = containerResource.getAggregations();
            Map<String, List<RelationDao>> relations = containerResource.getRelations();

            boolean insertEntity = this.baseDao.entityDaoImpl.insertEntity(entityDao);


            boolean insertChildEntities = true;
            if(childEntities!=null){
                Iterator entityItr = childEntities.entrySet().iterator();
                while(entityItr.hasNext()){
                    Map.Entry<String, List<EntityDao>> pair = (Map.Entry<String, List<EntityDao>>) entityItr.next();
                    for(EntityDao child:pair.getValue()){
                         if(!this.baseDao.entityDaoImpl.insertEntity(child))
                         {
                             insertChildEntities = false;
                             break;
                         }
                    }
                }
            }

            boolean insertEntityTypes = true;

            if(entityTypes!=null){
                Iterator eTypeItr = entityTypes.entrySet().iterator();
                while(eTypeItr.hasNext()){
                    Map.Entry<String, List<EntityTypeDao>> pair = (Map.Entry<String, List<EntityTypeDao>>) eTypeItr.next();
                    for(EntityTypeDao entityTypeDao:pair.getValue()){
                        if(!this.baseDao.entityTypeDaoImpl.insertEntityType(entityTypeDao))
                        {
                            insertEntityTypes = false;
                            break;
                        }
                    }
                }
            }

            boolean insertAggregations = true;

            if(aggregations!=null){
                Iterator aggItr = aggregations.entrySet().iterator();
                while(aggItr.hasNext()){
                    Map.Entry<String, List<AggregationDao>> pair = (Map.Entry<String, List<AggregationDao>>) aggItr.next();
                    for(AggregationDao child:pair.getValue()){
                        if(!this.baseDao.aggregationDaoImpl.insertAggregation(child)){
                            insertAggregations = false;
                            break;
                        }
                    }
                }
            }

            boolean insertRelations = true;

            if(relations!=null){
                Iterator relItr = relations.entrySet().iterator();
                while(relItr.hasNext()){
                    Map.Entry<String, List<RelationDao>> pair = (Map.Entry<String, List<RelationDao>>)relItr.next();
                    for(RelationDao relationDao:pair.getValue()){
                        if(!this.baseDao.relationDaoImpl.insertRelation(relationDao)){
                            insertRelations = false;
                            break;
                        }
                    }
                }
            }

            boolean insertProperties = true;

            if(properties!=null){
                Iterator propItr = properties.entrySet().iterator();
                while(propItr.hasNext()){
                    Map.Entry<String, List<PropertyDao>> pair = (Map.Entry<String, List<PropertyDao>>)propItr.next();
                    for(PropertyDao propertyDao:pair.getValue()){
                        if(Constants.metadataPredicateMap.containsKey(propertyDao.getName())){
                            boolean multiValued = Constants.metadataPredicateMap.get(propertyDao.getName());
                            if(multiValued){
                                if(!this.baseDao.propertyDaoImpl.insertProperty(propertyDao)){
                                    insertProperties = false;
                                    break;
                                }
                            }
                            else{
                                if(!this.baseDao.propertyDaoImpl.insertSingleValueProperty(propertyDao)){
                                    insertProperties = false;
                                    break;
                                }
                            }
                        }
                        else {
                            if(!this.baseDao.propertyDaoImpl.insertProperty(propertyDao)){
                                insertProperties = false;
                                break;
                            }
                        }
                    }
                }
            }

            if(insertEntity
                    &&insertChildEntities
                    &&insertEntityTypes
                    &&insertAggregations
                    &&insertRelations
                    &&insertProperties)
                return true;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    @Override
    public boolean updateResource(Resource resource) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteResource(Object key) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

