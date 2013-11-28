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
package org.dataconservancy.mhf.eventing.manager;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.mhf.eventing.events.MetadataExtractionEvent;
import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataHandlingEventManagerListenerTest implements MetadataHandlingEventListener {
    
    private ArrayList<MetadataValidationEvent> validationEvents;
    private ArrayList<MetadataExtractionEvent> extractionEvents;
    
    @Before
    public void setup() {
        validationEvents = new ArrayList<MetadataValidationEvent>();
        extractionEvents = new ArrayList<MetadataExtractionEvent>();
    }

    @Test
    public void testListenerReceivesEvents() throws InterruptedException {
        MetadataHandlingEventManager.getInstance().registerListener(this);
        
        MetadataValidationEvent event = new MetadataValidationEvent("object", "message", "failure", MetadataValidationEvent.ValidationType.FAILURE);
        MetadataHandlingEventManager.getInstance().sendEvent(event);
        
        Thread.sleep(1000);
        
        assertEquals(1, validationEvents.size());
        assertTrue(validationEvents.get(0).getObjectId().equalsIgnoreCase("object"));
        assertTrue(validationEvents.get(0).getMessage().equalsIgnoreCase("message"));
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
    }
    
    @Test
    public void testListenerReceivesMultipeEvents() throws InterruptedException {
        MetadataHandlingEventManager.getInstance().registerListener(this);
        
        MetadataValidationEvent event = new MetadataValidationEvent("object", "validation", "", MetadataValidationEvent.ValidationType.PASS);
        MetadataExtractionEvent extractionEvent = new MetadataExtractionEvent("object", "extraction", MetadataExtractionEvent.ExtractionEventType.EXTRACTION);
        
        MetadataHandlingEventManager.getInstance().sendEvent(event);
        MetadataHandlingEventManager.getInstance().sendEvent(event);
        MetadataHandlingEventManager.getInstance().sendEvent(event);
        
        MetadataHandlingEventManager.getInstance().sendEvent(extractionEvent);
        MetadataHandlingEventManager.getInstance().sendEvent(extractionEvent);
        
        Thread.sleep(1000);
        
        assertEquals(3, validationEvents.size());
        assertEquals(2, extractionEvents.size());
        
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
    }
    
    @Test
    public void testUnRegisteredListenerReceivesNoEvents() throws InterruptedException {
        MetadataHandlingEventManager.getInstance().registerListener(this);
        
        MetadataValidationEvent event = new MetadataValidationEvent("object", "message", "", MetadataValidationEvent.ValidationType.PASS);
        MetadataHandlingEventManager.getInstance().sendEvent(event);
        
        Thread.sleep(1000);
        
        assertEquals(1, validationEvents.size());
   
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
        
        MetadataHandlingEventManager.getInstance().sendEvent(event);
        
        Thread.sleep(1000);
        
        assertEquals(1, validationEvents.size());
    }

    @Override
    public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {
        if(event instanceof MetadataValidationEvent) {
            validationEvents.add((MetadataValidationEvent) event);
        } else if (event instanceof MetadataExtractionEvent) {
            extractionEvents.add((MetadataExtractionEvent) event);
        }        
    }
}
