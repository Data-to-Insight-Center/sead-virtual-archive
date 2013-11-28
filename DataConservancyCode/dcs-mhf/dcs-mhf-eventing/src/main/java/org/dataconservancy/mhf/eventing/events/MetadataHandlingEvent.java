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

import org.joda.time.DateTime;

public class MetadataHandlingEvent {
    
    protected String objectId;
    protected String message;
    protected DateTime eventGenerated;
    
    public MetadataHandlingEvent(String objectId, String message) {
        this.objectId = objectId;
        this.message = message;
        this.eventGenerated = DateTime.now();
    }
    
    public String getObjectId() {
        return objectId;
    }
    
    public void setObjectId(String id) {
        this.objectId = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public DateTime getEventGenerated() {
        return eventGenerated;
    }

    public void setEventGenerated(DateTime eventGenerated) {
        this.eventGenerated = eventGenerated;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((eventGenerated == null) ? 0 : eventGenerated.hashCode());
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
        MetadataHandlingEvent other = (MetadataHandlingEvent) obj;
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
        if (eventGenerated == null) {
            if (other.eventGenerated != null)
                return false;
        } else if (!eventGenerated.equals(other.eventGenerated))
            return false;
        
        return true;
    }
    
}