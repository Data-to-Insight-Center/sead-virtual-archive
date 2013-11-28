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
package org.dataconservancy.ui.services;

import java.util.Collection;
import java.util.Set;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.model.AttributeSetName;

/**
 * This is a Utility class to be used by {@code ExternalReferenceVerification} and {@code InternalReferenceChecker} in
 * order to verify each business object referenced in the package can be successfully dereferenced.
 */
public abstract class BaseReferenceChecker extends BaseIngestService {

    public boolean checkPackageReferences(String depositId, IngestWorkflowState state) {
        
        Set<AttributeSet> packageAttributeSets = state.getAttributeSetManager().matches(
                AttributeSetName.ORE_REM_PACKAGE, new AttributeImpl(null, null, null));
        for (AttributeSet pkg : packageAttributeSets) {
            
            // First check through all the projects that are aggegated by this package
            Collection<Attribute> projectAggregations = pkg.getAttributesByName("Package-Aggregates-Project");
            for (Attribute projectAggregation : projectAggregations) {
                String projectRefId = projectAggregation.getValue();
                if (!checkReferenceExists(depositId, state, projectRefId, "project")) {
                    return false;
                }                
            }
            
            // Check through all the collections that are aggregated by this package
            Collection<Attribute> collectionAggregations = pkg.getAttributesByName("Package-Aggregates-Collection");
            for (Attribute collectionAggregation : collectionAggregations) {
                String collectionRefId = collectionAggregation.getValue();
                if (!checkReferenceExists(depositId, state, collectionRefId, "collection")) {
                    return false;
                }                
            }
            
            // First check through all the collections that are aggregated by this project
            Collection<Attribute> dataItemAggregations = pkg.getAttributesByName("Package-Aggregates-DataItem");
            for (Attribute dataItemAggregation : dataItemAggregations) {
                String dataItemRefId = dataItemAggregation.getValue();                
                if (!checkReferenceExists(depositId, state, dataItemRefId, "dataItem")) {
                    return false;
                }
            }
            
            // Check for all the files aggregated by this project
            Collection<Attribute> fileAggregations = pkg.getAttributesByName("Package-Aggregates-File");
            for (Attribute fileAggregation : fileAggregations) {
                String fileRefId = fileAggregation.getValue();
                if (!checkReferenceExists(depositId, state, fileRefId, "file")) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean checkProjectReferences(String depositId, IngestWorkflowState state) {
        
        Set<AttributeSet> projectAttributeSets = state.getAttributeSetManager().matches(
                AttributeSetName.ORE_REM_PROJECT, new AttributeImpl(null, null, null));
        for (AttributeSet project : projectAttributeSets) {
            
            // First check through all the collections that are aggregated by this project
            Collection<Attribute> collectionAggregations = project.getAttributesByName("Project-Aggregates-Collection");
            for (Attribute collectionAggregation : collectionAggregations) {
                String collectionRefId = collectionAggregation.getValue();
                if (!checkReferenceExists(depositId, state, collectionRefId, "collection")) {
                    return false;
                }
            }
            
            // Check for all the files aggregated by this project
            Collection<Attribute> fileAggregations = project.getAttributesByName("Project-Aggregates-File");
            for (Attribute fileAggregation : fileAggregations) {
                String fileRefId = fileAggregation.getValue();
                if (!checkReferenceExists(depositId, state, fileRefId, "file")) {
                    return false;
                }                
            }
        }
        return true;
    }
    
    public boolean checkCollectionReferences(String depositId, IngestWorkflowState state) {
        
        Set<AttributeSet> collectionAttributeSets = state.getAttributeSetManager().matches(
                AttributeSetName.ORE_REM_COLLECTION, new AttributeImpl(null, null, null));
        for (AttributeSet collection : collectionAttributeSets) {
            
            // First check through all the collections that are aggregated by this collection
            Collection<Attribute> collectionAggregations = collection
                    .getAttributesByName("Collection-Aggregates-Collection");
            for (Attribute collectionAggregation : collectionAggregations) {
                String collectionRefId = collectionAggregation.getValue();
                if (!checkReferenceExists(depositId, state, collectionRefId, "collection")) {
                    return false;
                }
            }
            
            // Check for all the data items aggregated by this collection
            Collection<Attribute> dataItemAggregations = collection
                    .getAttributesByName("Collection-Aggregates-DataItem");
            for (Attribute dataItemAggregation : dataItemAggregations) {
                String dataItemRefId = dataItemAggregation.getValue();
                if (!checkReferenceExists(depositId, state, dataItemRefId, "dataItem")) {
                    return false;
                }                
            }
            
            // Check for all the files aggregated by this collection
            Collection<Attribute> fileAggregations = collection.getAttributesByName("Collection-Aggregates-File");
            for (Attribute fileAggregation : fileAggregations) {
                String fileRefId = fileAggregation.getValue();
                if (!checkReferenceExists(depositId, state, fileRefId, "file")) {
                    return false;
                }
            }
            
            // Check for all the projects this collection is aggregated by
            Collection<Attribute> aggregatingProjects = collection
                    .getAttributesByName("Collection-Aggregated-By-Project");
            for (Attribute aggrigatingProject : aggregatingProjects) {
                String projectRefId = aggrigatingProject.getValue();
                if (!checkReferenceExists(depositId, state, projectRefId, "project")) {
                    return false;
                }
            }
            
            // Check for all the collections this collection is aggregated by
            Collection<Attribute> aggregatingCollections = collection
                    .getAttributesByName("Collection-IsPartOf-Collection");
            for (Attribute aggregatingCollection : aggregatingCollections) {
                String collectionRefId = aggregatingCollection.getValue();
                if (!checkReferenceExists(depositId, state, collectionRefId, "collection|project")) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean checkDataItemReferences(String depositId, IngestWorkflowState state) {
        
        Set<AttributeSet> dataItemAttributeSets = state.getAttributeSetManager().matches(
                AttributeSetName.ORE_REM_DATAITEM, new AttributeImpl(null, null, null));
        for (AttributeSet pkg : dataItemAttributeSets) {
            
            // Check for all the files aggregated by this data item
            Collection<Attribute> fileAggregations = pkg.getAttributesByName("DataItem-Aggregates-File");
            for (Attribute fileAggregation : fileAggregations) {
                String fileRefId = fileAggregation.getValue();
                if (!checkReferenceExists(depositId, state, fileRefId, "file")) {
                    return false;
                }
            }
            
            // Check for all the collections that aggregate this data item
            Collection<Attribute> collectionAggregations = pkg.getAttributesByName("DataItem-IsPartOf-Collection");
            for (Attribute collectionAggregation : collectionAggregations) {
                String collectionRefId = collectionAggregation.getValue();
                if (!checkReferenceExists(depositId, state, collectionRefId, "collection")) {
                    return false;
                }
            }
            
            // Check for all the data items that this data item is a version of
            Collection<Attribute> dataItemVersions = pkg.getAttributesByName("DataItem-IsVersionOf-DataItem");
            for (Attribute dataItemVersion : dataItemVersions) {
                String dataItemRefId = dataItemVersion.getValue();
                if (!checkReferenceExists(depositId, state, dataItemRefId, "dataItem")) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean checkFileReferences(String depositId, IngestWorkflowState state) {
        
        Set<AttributeSet> fileAttributeSets = state.getAttributeSetManager().matches(AttributeSetName.ORE_REM_FILE,
                new AttributeImpl(null, null, null));
        for (AttributeSet file : fileAttributeSets) {
            
            // Check for all the objects that claim this file as metadata
            Collection<Attribute> isMetadataForRelationships = file.getAttributesByName("File-IsMetadata-For");
            for (Attribute isMetadataFor : isMetadataForRelationships) {
                String isMetadataForRefId = isMetadataFor.getValue();
                if (!checkMetadataReferenceExists(depositId, state, isMetadataForRefId)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected abstract boolean checkReferenceExists(String depositId, IngestWorkflowState state, String reference, String expectedReferenceType);  
    
    protected abstract boolean checkMetadataReferenceExists(String depositId, IngestWorkflowState state, String reference);
    
}
