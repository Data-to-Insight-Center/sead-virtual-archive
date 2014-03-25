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

/**
 * Read and write delegator
 */
public class SeadRegistry extends BaseDaoImpl implements Registry {

    RegistryMapper mapper = new RegistryMapper();

    BaseDaoImpl registryDao;
    public SeadRegistry(BaseDaoImpl registryDao) {
        this.registryDao = registryDao;
    }

    public void init() throws InstantiationException, IllegalAccessException {
        mapper.load(this.registryDao);
    }
    Registry delegate;

    @Override
    public Resource getResource(String entityId) {
        try {
            ResourceType resourceType = mapper.getResourceType(entityId);
            delegate = mapper.getRegistry(resourceType);
            Resource resource = delegate.getResource(entityId);
            return resource;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean putResource(ResourceType resourceType, Resource resource) {
        try {
            delegate = mapper.getRegistry(resourceType);
            return delegate.putResource(resourceType, resource);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
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
