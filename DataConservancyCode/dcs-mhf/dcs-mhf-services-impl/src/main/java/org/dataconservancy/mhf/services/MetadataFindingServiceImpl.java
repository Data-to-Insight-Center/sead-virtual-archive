/*
 * Copyright 2013 Johns Hopkins University
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.mhf.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.ui.model.BusinessObject;

public class MetadataFindingServiceImpl implements MetadataFindingService {

    //Typed registry that stores all of the metadata finders
    private TypedRegistry<MetadataFinder> registry;
    
    public MetadataFindingServiceImpl(TypedRegistry<MetadataFinder> registry) {
        this.registry = registry;
    }
    
    public List<MetadataInstance> findMetadata(BusinessObject businessObject) {
        //Look up all of the metadata finder registry entries
        Set<RegistryEntry<MetadataFinder>> entries = registry.lookup(businessObject.getClass().getName());
        
        //Loop through the registry entries and get all of the metadata finders and use them to find metadata on the business object.
        List<MetadataInstance> instances = new ArrayList<MetadataInstance>();
        for (RegistryEntry<MetadataFinder> entry : entries) {
            instances.addAll(entry.getEntry().findMetadata(businessObject));
        }    
       
        return instances;
    }
}
