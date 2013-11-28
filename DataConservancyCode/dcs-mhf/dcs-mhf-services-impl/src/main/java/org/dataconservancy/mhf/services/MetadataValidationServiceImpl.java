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

import java.net.URL;

import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.ui.model.BusinessObject;

import java.util.List;
import java.util.Set;

public class MetadataValidationServiceImpl implements MetadataValidationService {

    private MetadataFindingService metadataFindingService;
    private TypedRegistry<MetadataValidator> registry;

    //TODO: is it necessary to force user to provide a finding service at constructor? Specially when validating a single MetadataInstance does not require a finding service.
    public MetadataValidationServiceImpl(MetadataFindingService metadataFindingService, TypedRegistry<MetadataValidator> registry) {
        this.metadataFindingService = metadataFindingService;
        this.registry = registry;
    }

    public boolean validate(String businessObjectId,MetadataInstance metadataInstance, URL baseUrl) {

        boolean isValid = true;
        
        //If the business object id hasn't been set default to the metadata instance format id for the event.
        String objectId = metadataInstance.getFormatId();
        if (businessObjectId != null && !businessObjectId.isEmpty()) {
           objectId = businessObjectId; 
        }
        
        //Look up all of the metadata finder registry entries
        Set<RegistryEntry<MetadataValidator>> entries = registry.lookup(metadataInstance.getFormatId());
        if (entries.isEmpty()) {
            MetadataValidationEvent event = new MetadataValidationEvent(objectId, "No entries could be found for id " + metadataInstance.getFormatId(), "", MetadataValidationEvent.ValidationType.NOOP);
            MetadataHandlingEventManager.getInstance().sendEvent(event);
        } else {
            //Loop through the registry entries and get all of the metadata validators and use them to validate the instance
            for (RegistryEntry<MetadataValidator> entry : entries) {
                try {
                    entry.getEntry().validate(metadataInstance, baseUrl);
                } catch (ValidationException e) {
                    isValid = false;
                    
                    MetadataValidationEvent event = new MetadataValidationEvent(objectId, e.getMessage(), "", MetadataValidationEvent.ValidationType.FAILURE);
                    MetadataHandlingEventManager.getInstance().sendEvent(event);
                }
            }
    
            if (isValid) {
                MetadataValidationEvent event = new MetadataValidationEvent(objectId, "", "", MetadataValidationEvent.ValidationType.PASS);
                MetadataHandlingEventManager.getInstance().sendEvent(event);
            }
        }
        
        return isValid;
    }

    public boolean validate(BusinessObject businessObject, URL baseUrl) {
        
        boolean isValid = true;
        List<MetadataInstance> metadataInstances = metadataFindingService.findMetadata(businessObject);
        for (MetadataInstance mi : metadataInstances) {
            if (!validate(businessObject.getId(), mi, baseUrl)) {
                isValid = false;
            }
        }        
        
        return isValid;
    }
}
