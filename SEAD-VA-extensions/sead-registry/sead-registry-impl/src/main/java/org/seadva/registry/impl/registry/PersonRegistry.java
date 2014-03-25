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
import org.seadva.registry.dao.EntityDao;
import org.seadva.registry.dao.EntityTypeDao;
import org.seadva.registry.dao.PropertyDao;
import org.seadva.registry.dao.jdbc.impl.EntityJdbcDaoImpl;
import org.seadva.registry.dao.jdbc.impl.EntityTypeJdbcDaoImpl;
import org.seadva.registry.dao.jdbc.impl.PropertyJdbcDaoImpl;
import org.seadva.registry.impl.resource.PersonResource;

import java.util.List;

/**
 * Read and write person resource to registry
 */
public class PersonRegistry implements Registry {

    BaseDaoImpl baseDao;
    PersonRegistry(BaseDaoImpl baseDao){
        this.baseDao = baseDao;
    }

    @Override
    public Resource getResource(String entityId) {
        try {
            EntityDao entityDao = this.baseDao.entityDaoImpl.getEntity(entityId);
            List<PropertyDao> propertyDao = this.baseDao.propertyDaoImpl.getPropertyForEntity(entityId);
            List<EntityTypeDao> entityTypeDao = this.baseDao.entityTypeDaoImpl.getEntity(entityId);
            return new PersonResource(entityDao, entityTypeDao, propertyDao);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new PersonResource();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean putResource(ResourceType resourceType, Resource resource) {
        try {
            EntityDao entityDao = ((PersonResource)resource).getEntityDao();
            boolean insertEntity = this.baseDao.entityDaoImpl.insertEntity(entityDao);
            boolean insertProperties = true;
            for(PropertyDao propertyDao:((PersonResource)resource).getPropertyDao()){
                if(!this.baseDao.propertyDaoImpl.insertProperty(propertyDao))
                {
                    insertProperties = false;
                    break;
                }
            }
            boolean insertEntityTypes = true;
            for(EntityTypeDao entityTypeDao:((PersonResource)resource).getEntityTypeDao()){
                if(!this.baseDao.entityTypeDaoImpl.insertEntityType(entityTypeDao)){
                    insertEntityTypes = false;
                    break;
                }
            }

            if(insertEntity&&
                    insertProperties&&
                    insertEntityTypes)
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
