/*
 * Copyright 2013 The Trustees of Indiana University
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

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.seadva.registry.api.ResourceType;
import org.seadva.registry.dao.*;
import org.seadva.registry.impl.resource.Constants;
import org.seadva.registry.impl.resource.ContainerResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler test cases
 */
public class ContainerRegistryTest extends JerseyTest {

    public ContainerRegistryTest() throws Exception {
        super("org.seadva.registry.impl");
    }

    @Before
    public void init() throws IllegalAccessException, InstantiationException {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "testContext.xml"});
        seadRegistry = new SeadRegistry((BaseDaoImpl) appContext.getBean("registry"));
        seadRegistry.init();
    }
    SeadRegistry seadRegistry;

    @Test
    public void testCollectionPut() throws Exception {
        EntityDao entity = new EntityDao();
        String entity_id = "http://seada-test/" + "test_coll_id";
        entity.setEntity_id(entity_id);
        entity.setEntity_name("Test Collection");

        Map<String,List<EntityTypeDao>> entityTypeDaoMap = new HashMap<String, List<EntityTypeDao>>();


        EntityTypeDao entityTypeDao = new EntityTypeDao();
        entityTypeDao.setEntityTypeId("sead/type:2");
        entityTypeDao.setEntityTypeName(ResourceType.CONTAINER.getText());
        entityTypeDao.setEntity_id(entity_id);
        List<EntityTypeDao> entityTypeDaoList = new ArrayList<EntityTypeDao>();
        entityTypeDaoList.add(entityTypeDao);
        entityTypeDaoMap.put(entity_id,entityTypeDaoList);

        PropertyDao propertyDao = new PropertyDao();
        propertyDao.setEntity_id(entity_id);
        propertyDao.setName("Creator");
        propertyDao.setValueStr("Test creator");

        PropertyDao propertyDaoTitle = new PropertyDao();
        propertyDaoTitle.setEntity_id(entity_id);
        propertyDaoTitle.setName("title");
        propertyDaoTitle.setValueStr("Test Title");
        Map<String,List<PropertyDao>> propertyDaoMap = new HashMap<String,List<PropertyDao>>();
        List<PropertyDao> propertyDaoList = new ArrayList<PropertyDao>();
        propertyDaoList.add(propertyDao);
        propertyDaoList.add(propertyDaoTitle);
        propertyDaoMap.put(entity_id, propertyDaoList);

        EntityDao childentity = new EntityDao();
        String child_entity_id = "http://seada-test/" + "test_file_id";
        childentity.setEntity_id(child_entity_id);
        childentity.setEntity_name("Test File");

        String format_id = "http:/seadva/format_id";
        EntityDao formatEntity = new EntityDao();
        formatEntity.setEntity_id(format_id);
        formatEntity.setEntity_name("mp4");

        PropertyDao formatProperty = new PropertyDao();
        formatProperty.setEntity_id(format_id);
        formatProperty.setName("formatType");
        formatProperty.setValueStr("IANA");

        PropertyDao formatValue = new PropertyDao();
        formatValue.setEntity_id(format_id);
        formatValue.setName("formatValue");
        formatValue.setValueStr("MP-4");
        propertyDaoList = new ArrayList<PropertyDao>();
        propertyDaoList.add(formatProperty);
        propertyDaoList.add(formatValue);

        propertyDaoMap.put(format_id,propertyDaoList);

        List<RelationDao> relationDaos = new ArrayList<RelationDao>();
        RelationDao relationDao = new RelationDao();
        relationDao.setCause_id(child_entity_id);
        relationDao.setEffect_id(format_id);
        relationDao.setRelation(Constants.hasFormat);
        relationDaos.add(relationDao);

        Map<String,List<RelationDao>> relMap = new HashMap<String, List<RelationDao>>();
        relMap.put(child_entity_id,relationDaos);

        List<EntityDao> children = new ArrayList<EntityDao>();
        children.add(childentity);
        children.add(formatEntity);

        Map<String, List<AggregationDao>> aggregationDaos  = new HashMap<String, List<AggregationDao>>();

        List<AggregationDao> temp = new ArrayList<AggregationDao>();
        AggregationDao aggregationDao = new AggregationDao(entity_id, child_entity_id);
        temp.add(aggregationDao);
        aggregationDaos.put(entity_id, temp);

        EntityTypeDao child_entityTypeDao = new EntityTypeDao();
        child_entityTypeDao.setEntityTypeId("sead/type:3");
        child_entityTypeDao.setEntityTypeName(ResourceType.FILE.getText());
        child_entityTypeDao.setEntity_id(child_entity_id);

        entityTypeDaoList = new ArrayList<EntityTypeDao>();
        entityTypeDaoList.add(child_entityTypeDao);
        entityTypeDaoMap.put(child_entity_id,entityTypeDaoList);

        PropertyDao child_propertyDao = new PropertyDao();
        child_propertyDao.setEntity_id(child_entity_id);
        child_propertyDao.setName("Creator");
        child_propertyDao.setValueStr("File Test creator");

        propertyDaoList = new ArrayList<PropertyDao>();
        propertyDaoList.add(child_propertyDao);
        propertyDaoMap.put(child_entity_id, propertyDaoList);

        ContainerResource containerResource = new ContainerResource();
        containerResource.setEntity(entity);

        containerResource.setEntityType(entityTypeDaoMap);
        containerResource.setProperties(propertyDaoMap);

        Map<String, List<EntityDao>> childMap = new HashMap<String, List<EntityDao>>();
        childMap.put(entity_id,children); //??
        containerResource.setChildEntities(childMap);
        containerResource.setAggregations(aggregationDaos);
        containerResource.setRelations(relMap);

        seadRegistry.putResource(ResourceType.CONTAINER, containerResource);

    }

    @Test
    public void testGetCollection() throws Exception {
        String entity_id = "http://seada-test/" + "test_coll_id";
        ContainerResource containerResource = (ContainerResource) seadRegistry.getResource(entity_id);
        System.out.print(containerResource.getEntity().getEntity_id());
    }

}
