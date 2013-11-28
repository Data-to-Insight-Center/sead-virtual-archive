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

import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.ui.model.BusinessObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetadataHandlingServiceImpl implements MetadataHandlingService {

    private MetadataValidationService validationService;
    private MetadataFindingService findingService;
    private AttributeSetMetadataExtractionServiceImpl extractionService;
    
    public MetadataHandlingServiceImpl(MetadataValidationService validationService, 
                                       AttributeSetMetadataExtractionServiceImpl extractionService, MetadataFindingService findingService) {
        this.validationService = validationService;
        this.extractionService = extractionService;
        this.findingService = findingService;
    }
    
    @Override
    public Set<AttributeSet> validateAndExtractMetadata(BusinessObject businessObject,
                                                        String parentId, URL baseUrl) {

        Set<AttributeSet> attributes = new HashSet<AttributeSet>();

        List<MetadataInstance> instances = findingService.findMetadata(businessObject);

        boolean isValidMetadata = true;

        for (MetadataInstance mi : instances) {
            if (!validationService.validate(businessObject.getId(), mi, baseUrl)) {
                isValidMetadata = false;
            }
        }

        //proceed to extracting metadata representation only if validation on every metadata instance is successful.
        //TODO: This could be changed to be a policy driven decision point
        if (isValidMetadata) {
            for (MetadataInstance instance : instances) {
                Set<AttributeSetMetadataRepresentation> extractedAttributes = extractionService.extract(instance);
                for(AttributeSetMetadataRepresentation representation : extractedAttributes) {
                    attributes.add(representation.getRepresentation());
                }
            }
        }
        return attributes;
    }
    
    @Override
    public void validateMetadata(BusinessObject businessObject, URL baseUrl) {
        List<MetadataInstance> instances = findingService.findMetadata(businessObject);
        
        for (MetadataInstance mi : instances) {
            validationService.validate(businessObject.getId(), mi, baseUrl );
        }
    }

    @Override
    public Set<AttributeSet> extractMetadata(BusinessObject businessObject) {
        List<MetadataInstance> instances = findingService.findMetadata(businessObject);

        Set<AttributeSet> attributes = new HashSet<AttributeSet>();

        for (MetadataInstance mi : instances) {
            Set<AttributeSetMetadataRepresentation> extractedAttributes = extractionService.extract(mi);
            for(AttributeSetMetadataRepresentation representation : extractedAttributes) {
                attributes.add(representation.getRepresentation());
            }
        }

        return attributes;
    }
}