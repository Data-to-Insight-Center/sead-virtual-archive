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

package org.seadva.registry.mapper;

import com.google.gson.Gson;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.api.Resource;
import org.seadva.registry.api.ResourceType;
import org.seadva.registry.dao.*;
import org.seadva.registry.impl.resource.Constants;
import org.seadva.registry.impl.resource.ContainerResource;

import java.util.*;

/**
 * Maps registry content to SEAD/DC schema
 */
public class DcsMapper {

    Map<ResourceType, Class> classMap;
    public void init(){
        classMap = new HashMap<ResourceType, Class>();
        classMap.put(ResourceType.CONTAINER, ContainerResource.class);

    }

    Map<String,List<PropertyDao>> propertyDaoList;
    Map<String,List<EntityDao>> entityDaoList;
    Map<String,List<AggregationDao>> aggregationDaoList;
    Map<String,List<RelationDao>> relationDaoList;
    ResearchObject sip;

    ResearchObject map(String gsonString, ResourceType resourceType){

        Gson gson = new Gson();

        Resource resource = (Resource) gson.fromJson(gsonString, classMap.get(resourceType));

        sip = new ResearchObject();
        if (resourceType == ResourceType.CONTAINER){
            ContainerResource containerResource = (ContainerResource)resource;
            EntityDao entityDao = containerResource.getEntity();
            propertyDaoList = containerResource.getProperties();
            relationDaoList = containerResource.getRelations();
            aggregationDaoList = containerResource.getAggregations();
            entityDaoList = containerResource.getChildEntities();
            manifestations = new HashMap<String, List<String>>();
            constructSip(null, entityDao.getEntity_id());
            Iterator manItr = manifestations.entrySet().iterator();
            while(manItr.hasNext()){
                Map.Entry<String,List<String>> pair = (Map.Entry<String, List<String>>) manItr.next();
                DcsManifestation manifestation = new DcsManifestation();
                manifestation.setDeliverableUnit(pair.getKey()+"_key");
                for(String file:pair.getValue()){
                    DcsManifestationFile manifestationFile = new DcsManifestationFile();
                    DcsFileRef ref = new DcsFileRef();
                    ref.setRef(file);
                    manifestationFile.setRef(ref);
                    manifestation.addManifestationFile(manifestationFile);
                }
                sip.addManifestation(manifestation);
            }
        }

        //client uses this utility to convert Gson output from registry into ResearchObject
        return sip;
    }

    Map<String, List<String>> manifestations;
    void constructSip(String parentId, String entityId){

        List<PropertyDao> propertyDaos = propertyDaoList.get(entityId);
        List<AggregationDao> aggregationDaos = aggregationDaoList.get(entityId);
        List<RelationDao> relationDaos = relationDaoList.get(entityId);

        if(aggregationDaos.size()==0){  //File
            SeadFile file = new SeadFile();
            file.setId(entityId);
            List<String> children = manifestations.get(parentId);
            if(children == null)
                children = new ArrayList<String>();
            children.add(entityId);
            manifestations.put(parentId,children);

            for(PropertyDao propertyDao:propertyDaos){
                if(propertyDao.getName().equalsIgnoreCase("title"))
                   file.setName(propertyDao.getValueStr());
                else if(propertyDao.getName().equalsIgnoreCase("size"))
                    file.setSizeBytes(Long.getLong(propertyDao.getValueStr()));
            }

            for(RelationDao relationDao:relationDaos){
                if(relationDao.getRelation().equalsIgnoreCase(Constants.hasFormat))
                {
                    for(EntityDao object :entityDaoList.get(relationDao.getEffect_id()))
                    {
                        int type = 0 ;
                        int val = 0;
                        DcsFormat format = new DcsFormat();
                        for(PropertyDao propertyDao:propertyDaoList.get(object.getEntity_id())){

                            if(propertyDao.getName().equalsIgnoreCase("formatType")){
                                 format.setSchemeUri(propertyDao.getValueStr());
                                type = 1;
                            }
                            else if(propertyDao.getName().equalsIgnoreCase("formatValue")){
                                format.setFormat(propertyDao.getValueStr());
                                val = 1;
                            }
                            if(type==1&&val==1){
                                file.addFormat(format);
                                format = new DcsFormat();
                                type = 0;
                                val = 0;
                            }
                        }
                    }
                }
            }
            //we can safely assume that relationships go only one hop, hence we do not make recursive calls for relationships
            sip.addFile(file);
        }
        else{ //collection
            SeadDeliverableUnit du = new SeadDeliverableUnit();
            du.setId(entityId);
            for(PropertyDao propertyDao:propertyDaos){
                if(propertyDao.getName().equalsIgnoreCase("title"))
                    du.setTitle(propertyDao.getValueStr());
                else if(propertyDao.getName().equalsIgnoreCase("size"))
                    du.setSizeBytes(Long.getLong(propertyDao.getValueStr()));
            }
            sip.addDeliverableUnit(du);
        }

        for(AggregationDao aggregationDao:aggregationDaos){
            constructSip(entityId, aggregationDao.getChild_id());
        }
    }

    String map(ResearchObject sip, ResourceType resourceType){

        Gson gson = new Gson();

        if (resourceType == ResourceType.CONTAINER){
            ContainerResource containerResource = new ContainerResource();
            Map<String,List<EntityDao>> entityDaoList = new HashMap<String, List<EntityDao>>();
            Map<String,List<EntityTypeDao>> entityTypeDaoList = new HashMap<String, List<EntityTypeDao>>();
            Map<String,List<AggregationDao>> aggregationDaoList = new HashMap<String, List<AggregationDao>>();
            Map<String,List<PropertyDao>> propertyDaoList = new HashMap<String, List<PropertyDao>>();

            Collection<DcsDeliverableUnit> deliverableUnits = sip.getDeliverableUnits();

            for(DcsDeliverableUnit du:deliverableUnits){
                EntityTypeDao entityTypeDao = new EntityTypeDao();
                entityTypeDao.setEntityTypeName(ResourceType.CONTAINER.getText());
                entityTypeDao.setEntity_id(du.getId());

                List<EntityTypeDao> temp = new ArrayList<EntityTypeDao>();
                temp.add(entityTypeDao);
                entityTypeDaoList.put(du.getId(), temp);

                List<PropertyDao> tempProp = new ArrayList<PropertyDao>();
                PropertyDao propertyDao = new PropertyDao();
                propertyDao.setEntity_id(du.getId());
                propertyDao.setName("title");
                propertyDao.setValueStr(du.getTitle());
                tempProp.add(propertyDao);
                propertyDaoList.put(du.getId(), tempProp);

                if(du.getParents()==null||du.getParents().size()==0){
                    EntityDao entityDao = new EntityDao();
                    entityDao.setEntity_id(du.getId());
                    entityDao.setEntity_name(du.getTitle());
                    containerResource.setEntity(entityDao);
                    continue;
                }
                EntityDao entityDao = new EntityDao();
                entityDao.setEntity_id(du.getId());
                entityDao.setEntity_name(du.getTitle());
                List<EntityDao> temp2 = new ArrayList<EntityDao>();
                temp2.add(entityDao);
                entityDaoList.put(du.getId(), temp2);
                for(DcsDeliverableUnitRef parent: du.getParents()){
                    AggregationDao aggregationDao =new AggregationDao();
                    aggregationDao.setParent_id(parent.getRef());
                    aggregationDao.setChild_id(du.getId());
                    List<AggregationDao> agg = aggregationDaoList.get(parent.getRef());
                    if(agg==null)
                        agg = new ArrayList<AggregationDao>();
                    agg.add(aggregationDao);
                    aggregationDaoList.put(parent.getRef(),agg);
                }

            }

            Collection<DcsManifestation> manifestationCollection = sip.getManifestations();
            for(DcsManifestation man:manifestationCollection){

                EntityDao entityDao = new EntityDao();
                entityDao.setEntity_id(man.getId());
                entityDao.setEntity_name(man.getId());
                List<EntityDao> temp = new ArrayList<EntityDao>();
                temp.add(entityDao);
                entityDaoList.put(man.getId(), temp);

                AggregationDao aggregationDao =new AggregationDao();
                aggregationDao.setParent_id(man.getDeliverableUnit());
                aggregationDao.setChild_id(man.getId());
                List<AggregationDao> agg = aggregationDaoList.get(man.getDeliverableUnit());
                if(agg==null)
                    agg = new ArrayList<AggregationDao>();
                agg.add(aggregationDao);
                aggregationDaoList.put(man.getDeliverableUnit(),agg);

                for(DcsManifestationFile file:man.getManifestationFiles()){
                    aggregationDao =new AggregationDao();
                    aggregationDao.setParent_id(man.getId());
                    aggregationDao.setChild_id(file.getRef().getRef());
                    agg = aggregationDaoList.get(man.getDeliverableUnit());
                    if(agg==null)
                        agg = new ArrayList<AggregationDao>();
                    agg.add(aggregationDao);
                    aggregationDaoList.put(man.getId(),agg);
                }
                List<PropertyDao> tempProp = new ArrayList<PropertyDao>();
                PropertyDao propertyDao = new PropertyDao();
                propertyDao.setEntity_id(man.getId());
                propertyDao.setName("title");
                propertyDao.setValueStr(man.getId());
                tempProp.add(propertyDao);
                propertyDaoList.put(man.getId(), tempProp);
            }

            Collection<DcsFile> fileCollection = sip.getFiles();
            for(DcsFile file:fileCollection){

                EntityDao entityDao = new EntityDao();
                entityDao.setEntity_id(file.getId());
                entityDao.setEntity_name(file.getName());
                List<EntityDao> temp = new ArrayList<EntityDao>();
                temp.add(entityDao);
                entityDaoList.put(file.getId(), temp);

                EntityTypeDao entityTypeDao = new EntityTypeDao();
                entityTypeDao.setEntityTypeName(ResourceType.CONTAINER.getText());
                entityTypeDao.setEntity_id(file.getId());

                List<EntityTypeDao> temp2 = new ArrayList<EntityTypeDao>();
                temp2.add(entityTypeDao);
                entityTypeDaoList.put(file.getId(), temp2);

                List<PropertyDao> tempProp = new ArrayList<PropertyDao>();
                PropertyDao propertyDao = new PropertyDao();
                propertyDao.setEntity_id(file.getId());
                propertyDao.setName("title");
                propertyDao.setValueStr(file.getName());
                tempProp.add(propertyDao);
                propertyDaoList.put(file.getId(), tempProp);
            }




            containerResource.setChildEntities(entityDaoList);
            containerResource.setAggregations(aggregationDaoList);
//            containerResource.setRelations(relationDaos);
            containerResource.setProperties(propertyDaoList);
            containerResource.setEntityType(entityTypeDaoList);
            return gson.toJson(containerResource);
        }
        //client uses this utility to convert Sead SIP into Gson that registry can understand
        //before semd the post request to track it in the registry
        return  null;
    }
}
