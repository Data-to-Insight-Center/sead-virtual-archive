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
import org.seadva.registry.dao.EntityDao;
import org.seadva.registry.dao.EntityTypeDao;
import org.seadva.registry.dao.PropertyDao;
import org.seadva.registry.impl.registry.BaseDaoImpl;
import org.seadva.registry.impl.registry.SeadRegistry;
import org.seadva.registry.impl.resource.RepositoryResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler test cases
 */
public class RespositoryRegistryTest extends JerseyTest {

    public RespositoryRegistryTest() throws Exception {
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
    public void testRespositoryPut() throws Exception {
        EntityDao entity = new EntityDao();
        String entity_id = "http://seada-test/" + "test_repo_id";
        entity.setEntity_id(entity_id);
        entity.setEntity_name("HPSS_Repository");

        EntityTypeDao entityTypeDao = new EntityTypeDao();
        entityTypeDao.setEntityTypeId("sead/repo:1");
        entityTypeDao.setEntityTypeName(ResourceType.REPOSITORY.getText());
        entityTypeDao.setEntity_id(entity_id);
        List<EntityTypeDao> entityTypeDaoList = new ArrayList<EntityTypeDao>();
        entityTypeDaoList.add(entityTypeDao);

        PropertyDao propertyDao = new PropertyDao();
        propertyDao.setEntity_id(entity_id);
        propertyDao.setProperty_id(1);
        propertyDao.setName("Name");
        propertyDao.setValueStr("IU SDA");
        List<PropertyDao> propertyDaoList = new ArrayList<PropertyDao>();
        propertyDaoList.add(propertyDao);

        RepositoryResource repositoryResource = new RepositoryResource(entity, entityTypeDaoList, propertyDaoList);

        seadRegistry.putResource(ResourceType.REPOSITORY, repositoryResource);

    }

    @Test
    public void testGetRepository() throws Exception {
        String entity_id = "http://seada-test/" + "test_repo_id";
        RepositoryResource repositoryResource = (RepositoryResource) seadRegistry.getResource(entity_id);
        System.out.print(repositoryResource.getEntityDao().getEntity_id());
    }

}
