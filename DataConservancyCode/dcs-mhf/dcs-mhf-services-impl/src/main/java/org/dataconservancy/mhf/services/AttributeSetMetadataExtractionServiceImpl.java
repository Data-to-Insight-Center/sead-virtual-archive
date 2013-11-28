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


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.mhf.eventing.events.MetadataExtractionEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.extractor.api.MetadataExtractor;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;

public class AttributeSetMetadataExtractionServiceImpl implements MetadataExtractionService {

    TypedRegistry<MetadataExtractor> registry;
    
    public AttributeSetMetadataExtractionServiceImpl(TypedRegistry<MetadataExtractor> registry) {
        this.registry = registry;
    }
    
    @Override
    public Set<AttributeSetMetadataRepresentation> extract(MetadataInstance metadataInstance) {
        //Look up all of the metadata finder registry entries
        Set<RegistryEntry<MetadataExtractor>> entries = registry.lookup(metadataInstance.getFormatId(), AttributeSetMetadataRepresentation.REPRESENTATION_ID);
        
        //Loop through the registry entries and get all of the metadata finders and use them to find metadata on the business object.
        Set<AttributeSetMetadataRepresentation> representations = new HashSet<AttributeSetMetadataRepresentation>();
        for (RegistryEntry<MetadataExtractor> entry : entries) {
            try {
                Collection<MetadataRepresentation> reps = entry.getEntry().extractMetadata(metadataInstance);
                for(MetadataRepresentation representation : reps ) {
                    if(representation.getRepresentationId().equals(AttributeSetMetadataRepresentation.REPRESENTATION_ID))
                    {
                        try {
                            representations.add((AttributeSetMetadataRepresentation) representation);
                            MetadataExtractionEvent event = new MetadataExtractionEvent(metadataInstance.getFormatId(), representation.toString(), MetadataExtractionEvent.ExtractionEventType.EXTRACTION);
                            MetadataHandlingEventManager.getInstance().sendEvent(event);
                        } catch (ClassCastException e) {
                            MetadataExtractionEvent event = new MetadataExtractionEvent(metadataInstance.getFormatId(), e.getMessage(), MetadataExtractionEvent.ExtractionEventType.ERROR);
                            MetadataHandlingEventManager.getInstance().sendEvent(event);
                        }
                    }
                }
            } catch (ExtractionException e) {
                MetadataExtractionEvent event = new MetadataExtractionEvent(metadataInstance.getFormatId(), e.getMessage(), MetadataExtractionEvent.ExtractionEventType.ERROR);
                MetadataHandlingEventManager.getInstance().sendEvent(event);
            } 
        }    

        return representations;
    }
}
