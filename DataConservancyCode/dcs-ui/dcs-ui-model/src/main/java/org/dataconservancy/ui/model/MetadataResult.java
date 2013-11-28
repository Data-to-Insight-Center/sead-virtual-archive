/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.ui.model;

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.mhf.representation.api.AttributeSet;

/**
 * A container class to store all the information returned when calling the metadata handling framework. 
 *
 */
public class MetadataResult {

    private Set<AttributeSet> attributes;
    private Set<MetadataEventMessage> metadataValidationErrors;
    private Set<MetadataEventMessage> metadataValidationSuccess;
    private Set<MetadataEventMessage> metadataValidationWarnings;
    private boolean validationPerformed = false;
    
    private Set<MetadataEventMessage> metadataExtractionErrors;
    private Set<MetadataEventMessage> metadataExtractionSuccess;
    
    /**
     * A class to deliver the pertinent information from any metadata events in a digestable format.
     */
    public class MetadataEventMessage {
        private String message;
        private String metadataInfo;
        
        public MetadataEventMessage(String message, String metadataInfo) {
            this.message = message;
            this.metadataInfo = metadataInfo;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getMetadataInfo() {
            return metadataInfo;
        }
    }
    
    public MetadataResult() {
        attributes = new HashSet<AttributeSet>();
        metadataValidationErrors = new HashSet<MetadataEventMessage>();
        metadataValidationSuccess = new HashSet<MetadataEventMessage>();
        metadataValidationWarnings = new HashSet<MetadataEventMessage>();
        
        metadataExtractionErrors = new HashSet<MetadataEventMessage>();
        metadataExtractionSuccess = new HashSet<MetadataEventMessage>();
    }
    
    public MetadataResult(Set<AttributeSet> attributes, Set<MetadataEventMessage> metadataValidationErrors, 
                          Set<MetadataEventMessage> metadataValidationSuccess, Set<MetadataEventMessage> metadataValidationWarnings, Set<MetadataEventMessage> metadataExtractionErrors, 
                          Set<MetadataEventMessage> metadataExtractionSuccess) {
        this.attributes = attributes;
        this.metadataValidationErrors = metadataValidationErrors;
        this.metadataValidationSuccess = metadataValidationSuccess;
        this.metadataExtractionErrors = metadataExtractionErrors;
        this.metadataExtractionSuccess = metadataExtractionSuccess;
        this.metadataValidationWarnings = metadataValidationWarnings;
    }
    
    public boolean hasErrors() {
        return !metadataValidationErrors.isEmpty() || !metadataExtractionErrors.isEmpty();
    }
    
    public void setValidationPerformed(boolean performed) {
        validationPerformed = performed;
    }
    
    public boolean getValidationPeformed() {
        return validationPerformed;
    }
    
    public Set<AttributeSet> getAttributeSets() {
        return attributes;
    }
    
    public void setAttributeSets(Set<AttributeSet> attributes) {
        this.attributes = attributes;
    }
    
    public void addAttributeSet(AttributeSet attribute) {
        attributes.add(attribute);
    }
    
    public Set<MetadataEventMessage> getMetadataValidationErrors() {
        return metadataValidationErrors;
    }
    
    public void setMetadataValidationErrors(Set<MetadataEventMessage> validationErrors) {
        this.metadataValidationErrors = validationErrors;
    }
    
    public void addMetadataValidationError(MetadataEventMessage validationError) {
        metadataValidationErrors.add(validationError);
    }
    
    public Set<MetadataEventMessage> getMetadataValidationSuccesses() {
        return metadataValidationSuccess;
    }
    
    public void setMetadataValidationSuccesses(Set<MetadataEventMessage> validationSuccesses) {
        this.metadataValidationSuccess = validationSuccesses;
    }
    
    public void addMetadataValidationSuccess(MetadataEventMessage validationSuccess) {
        metadataValidationSuccess.add(validationSuccess);
    }
    
    public Set<MetadataEventMessage> getMetadataValidationWarnings() {
        return metadataValidationWarnings;
    }
    
    public void setMetadataValidationWarnings(Set<MetadataEventMessage> validationWarnings) {
        this.metadataValidationWarnings = validationWarnings;
    }
    
    public void addMetadataValidationWarning(MetadataEventMessage validationWarning) {
        metadataValidationWarnings.add(validationWarning);
    }
    
    public Set<MetadataEventMessage> getMetadataExtractionErrors() {
        return metadataExtractionErrors;
    }
    
    public void setMetadataExtractionErrors(Set<MetadataEventMessage> metadataExtractionErrors) {
        this.metadataExtractionErrors = metadataExtractionErrors;
    }
    
    public void addMetadataExtractionError(MetadataEventMessage metadataExtractionError) {
        metadataExtractionErrors.add(metadataExtractionError);
    }
    
    public Set<MetadataEventMessage> getMetadataExtractionSuccesses() {
        return metadataExtractionSuccess;
    }
    
    public void setMetadataExtractionSuccesses(Set<MetadataEventMessage> metadataExtractionSuccesses) {
        this.metadataExtractionSuccess = metadataExtractionSuccesses;
    }
    
    public void addMetadataExtractionSuccess(MetadataEventMessage extractionSuccess) {
        metadataExtractionSuccess.add(extractionSuccess);
    }
}