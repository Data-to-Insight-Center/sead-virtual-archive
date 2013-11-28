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

package org.dataconservancy.mhf.eventing.events;

public class MetadataValidationEvent extends MetadataHandlingEvent {

    public static enum ValidationType {
        PASS,
        FAILURE,
        NOOP
    };
    
    private String validationFailure;
    private ValidationType type;

    /**
     * @param objectId - The id of the object being validated.
     * @param message - A message explaining the validation event.
     * @param validationFailure - A text representation of the section of metadata that failed validation, or an empty string if none.
     * @param type - The type of the metadata validation event.
     */
    public MetadataValidationEvent(String objectId, String message, String validationFailure, ValidationType type) {
        super(objectId, message);
        this.validationFailure = validationFailure;
        this.type = type;
    }
    
    public String getValidationFailure() {
        return validationFailure;
    }
    
    public ValidationType getType() {
        return type;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((validationFailure == null) ? 0 : validationFailure.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetadataValidationEvent other = (MetadataValidationEvent) obj;
        if (objectId == null) {
            if (other.objectId != null)
                return false;
        } else if (!objectId.equals(other.objectId))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (validationFailure == null) {
            if (other.validationFailure != null)
                return false;
        } else if (!validationFailure.equals(other.validationFailure))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
                
        return true;
    }
}
