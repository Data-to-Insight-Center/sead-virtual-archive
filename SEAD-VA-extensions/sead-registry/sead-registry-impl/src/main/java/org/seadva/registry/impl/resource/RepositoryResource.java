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

package org.seadva.registry.impl.resource;

import org.seadva.registry.api.Resource;
import org.seadva.registry.dao.EntityDao;
import org.seadva.registry.dao.EntityTypeDao;
import org.seadva.registry.dao.PropertyDao;

import java.util.List;

/**
 * Repository Resource
 */
public class RepositoryResource implements Resource {

    private EntityDao entityDao;
    private List<EntityTypeDao> entityTypeDao;
    private List<PropertyDao> propertyDao;

    public RepositoryResource(){}

    public RepositoryResource(EntityDao entityDao, List<EntityTypeDao> entityTypeDao, List<PropertyDao> propertyDao){
        this.entityDao = entityDao;
        this.entityTypeDao = entityTypeDao;
        this.propertyDao = propertyDao;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    public List<PropertyDao> getPropertyDao() {
        return propertyDao;
    }

    public void setPropertyDao(List<PropertyDao> propertyDao) {
        this.propertyDao = propertyDao;
    }

    public List<EntityTypeDao> getEntityTypeDao() {
        return entityTypeDao;
    }

    public void setEntityTypeDao(List<EntityTypeDao> entityTypeDao) {
        this.entityTypeDao = entityTypeDao;
    }
}
