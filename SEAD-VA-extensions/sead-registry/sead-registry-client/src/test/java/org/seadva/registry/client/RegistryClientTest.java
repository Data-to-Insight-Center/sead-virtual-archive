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

package org.seadva.registry.client;


import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;
import org.seadva.registry.database.model.obj.vaRegistry.BaseEntity;
import org.seadva.registry.database.model.obj.vaRegistry.Property;

import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertTrue;

public class RegistryClientTest extends JerseyTest {

    RegistryClient client;

    public RegistryClientTest() throws Exception {
        super("org.seadva.registry");
        client = new RegistryClient("http://localhost:8080/registry/rest");

    }

    @Test
    public void testQueryByProperty() throws IOException {

        org.seadva.registry.database.model.obj.vaRegistry.Collection collection = new org.seadva.registry.database.model.obj.vaRegistry.Collection();
        collection.setId(UUID.randomUUID().toString());
        collection.setName("test");
        collection.setVersionNum("1");
        collection.setIsObsolete(0);
        collection.setEntityName("test");
        collection.setEntityCreatedTime(new Date());
        collection.setEntityLastUpdatedTime(new Date());
        collection.setState(client.getStateByName("PublishedObject"));

        Property property = new Property();
        property.setMetadata(client.getMetadataByType("abstract"));
        property.setValuestr("test");
        collection.addProperty(property);

        client.postCollection(collection);

        List<BaseEntity> entityList = client.queryByProperty("abstract", "test");
        assertTrue(entityList.size() > 0);
    }


    @Test
    public void testUpdateProperty() throws IOException {

        List<BaseEntity> entityList = client.queryByProperty("abstract", "test");
        for(BaseEntity entity:entityList){

            Iterator props = entity.getProperties().iterator();
            Set<Property> updatesProperties = new HashSet<Property>();
            while (props.hasNext()){
                Property property = (Property) props.next();
                if(property.getMetadata().getMetadataElement().contains("abstract"))
                    property.setValuestr("new value");
                updatesProperties.add(property);
                props.remove();
            }

            entity.setProperties(updatesProperties);
            client.postEntity(entity);
        }
        entityList = client.queryByProperty("abstract", "new value");
        assertTrue(entityList.size() > 0);
    }
}