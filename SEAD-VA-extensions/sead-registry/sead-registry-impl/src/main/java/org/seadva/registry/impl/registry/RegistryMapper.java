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
import org.seadva.registry.dao.jdbc.impl.EntityJdbcDaoImpl;
import org.seadva.registry.dao.jdbc.impl.EntityTypeJdbcDaoImpl;
import org.seadva.registry.dao.jdbc.impl.PropertyJdbcDaoImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps resource type to registry implementation
 */
public class RegistryMapper {
    static Map<ResourceType,Registry> registryMap = new HashMap<ResourceType, Registry>();

    public void load(BaseDaoImpl baseDao) throws IllegalAccessException, InstantiationException {
        registryMap.put(ResourceType.REPOSITORY, new RepositoryRegistry(baseDao));
        registryMap.put(ResourceType.FILE, new FileRegistry(baseDao));
        registryMap.put(ResourceType.CONTAINER, new ContainerRegistry(baseDao));
        registryMap.put(ResourceType.PERSON, new PersonRegistry(baseDao));
        registryMap.put(ResourceType.RULE, new  RuleRegistry(baseDao));
    }

    Registry getRegistry(ResourceType resourceType) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return registryMap.get(resourceType);
    }

    public ResourceType getResourceType(String entityId) {
        try {
            List<EntityTypeDao> entityTypeDao = new EntityTypeJdbcDaoImpl().getEntityType(entityId);
            return ResourceType.fromString(entityTypeDao.get(0).getEntityTypeName());
    } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
