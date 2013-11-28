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

import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.ui.model.DataItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MetadataValidationServiceImplTest implements MetadataHandlingEventListener {
    
    MetadataValidationService underTest;
    DataItem testBusinessObject;

    private final String INSTANCE_ONE_FORMAT = "format:one";
    private final String INSTANCE_TWO_FORMAT = "format:two";
    
    TypedRegistry<MetadataValidator> mockRegistry;
    MetadataFindingService mockMetadataFindingService;
    
    private MetadataInstance instanceOne;
    private MetadataValidator validatorOne;
    private MetadataValidator validatorTwo;
    
    List<MetadataValidationEvent> validationEvents;
    
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        
        testBusinessObject = new DataItem();
        testBusinessObject.setId("dataItem");
        testBusinessObject.setName("test");
        testBusinessObject.setDescription("description");
        
        mockRegistry = (TypedRegistry<MetadataValidator>) mock(TypedRegistry.class);
        mockMetadataFindingService = mock(MetadataFindingService.class);
        
        instanceOne = mock(MetadataInstance.class);
        MetadataInstance instanceTwo = mock(MetadataInstance.class);
        
        when(instanceOne.getFormatId()).thenReturn(INSTANCE_ONE_FORMAT);
        when(instanceTwo.getFormatId()).thenReturn(INSTANCE_TWO_FORMAT);
        
        List<MetadataInstance> instances = new ArrayList<MetadataInstance>();
        
        instances.add(instanceOne);
        instances.add(instanceTwo);
        
        when(mockMetadataFindingService.findMetadata(testBusinessObject)).thenReturn(instances);
        
        Set<RegistryEntry<MetadataValidator>> entries = new HashSet<RegistryEntry<MetadataValidator>>();
        BasicRegistryEntryImpl<MetadataValidator> entry = (BasicRegistryEntryImpl<MetadataValidator>)mock(BasicRegistryEntryImpl.class);
        entries.add(entry);
        
        validatorOne = mock(MetadataValidator.class);
        when(entry.getEntry()).thenReturn(validatorOne);
        
        when(mockRegistry.lookup(instanceOne.getFormatId())).thenReturn(entries);
        
        Set<RegistryEntry<MetadataValidator>> entriesTwo = new HashSet<RegistryEntry<MetadataValidator>>();
        BasicRegistryEntryImpl<MetadataValidator> entryTwo = (BasicRegistryEntryImpl<MetadataValidator>)mock(BasicRegistryEntryImpl.class);
        entriesTwo.add(entryTwo);
        
        validatorTwo = mock(MetadataValidator.class);
        when(entryTwo.getEntry()).thenReturn(validatorTwo);
        
        when(mockRegistry.lookup(instanceTwo.getFormatId())).thenReturn(entriesTwo);

        underTest = new MetadataValidationServiceImpl(mockMetadataFindingService, mockRegistry);  
        
        validationEvents = new ArrayList<MetadataValidationEvent>();
        
        MetadataHandlingEventManager.getInstance().registerListener(this);
    }
    
    @After
    public void tearDown() {
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
    }
    
    /**
     * Test that a single metadata instance returns a single validation event.
     * @throws InterruptedException
     */
    @Test
    public void testValidateOneMetadataInstance() throws InterruptedException {
        assertTrue(underTest.validate(testBusinessObject.getId(), instanceOne, null));
        
        Thread.sleep(500);
        
        assertEquals(1, validationEvents.size());
        
        MetadataValidationEvent event = validationEvents.get(0);
        
        assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
        assertEquals(testBusinessObject.getId(), event.getObjectId());
    }
    
    /**
     * Test that a business object recieves pass events for it's metadata instances.
     * @throws InterruptedException
     */
    @Test
    public void testValidateOneBusinessObject() throws InterruptedException {
        assertTrue(underTest.validate(testBusinessObject, null));
        
        Thread.sleep(500);
        
        assertEquals(2, validationEvents.size());
        
        List<String> objectIds = new ArrayList<String>();
        for(MetadataValidationEvent event : validationEvents) {
            objectIds.add(event.getObjectId());
        }
        
        assertTrue(objectIds.contains(testBusinessObject.getId()));
        assertFalse(objectIds.contains(instanceOne.getFormatId()));
        assertFalse(objectIds.contains(INSTANCE_TWO_FORMAT));
        
        MetadataValidationEvent event = validationEvents.get(0);
        assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
        
        event = validationEvents.get(1);
        assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
    }

    /**
     * Test that if one instance fails validation we get the error event and the other pass event.
     * @throws ValidationException
     * @throws InterruptedException
     */
    @Test
    public void testBusinessObjectOneInstanceFails() throws ValidationException, InterruptedException {
        doThrow(new ValidationException("Exception message")).when(validatorOne).validate(instanceOne, null);
        assertFalse(underTest.validate(testBusinessObject, null));
        
        Thread.sleep(500);
        
        assertEquals(2, validationEvents.size());        
          
        //Test to ensure we got one failure event and one pass event
        MetadataValidationEvent event = validationEvents.get(0);
        if (event.getType() == MetadataValidationEvent.ValidationType.PASS) {
            
            assertEquals(testBusinessObject.getId(), event.getObjectId());
            
            event = validationEvents.get(1);
            assertEquals(MetadataValidationEvent.ValidationType.FAILURE, event.getType());
            assertEquals(testBusinessObject.getId(), event.getObjectId());
            assertEquals("Exception message", event.getMessage());
        } else {
            assertEquals("Exception message", event.getMessage());
            assertEquals(testBusinessObject.getId(), event.getObjectId());

            event = validationEvents.get(1);
            assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
            assertEquals(testBusinessObject.getId(), event.getObjectId());
        }        
    }
    
    @Test
    public void testValidateNoRegistredValidators() throws InterruptedException {
        when(mockRegistry.lookup(instanceOne.getFormatId())).thenReturn(new HashSet<RegistryEntry<MetadataValidator>>());

        assertTrue(underTest.validate(testBusinessObject.getId(), instanceOne, null));
        
        Thread.sleep(500);
        
        assertEquals(1, validationEvents.size());
        
        MetadataValidationEvent event = validationEvents.get(0);
        
        assertEquals(MetadataValidationEvent.ValidationType.NOOP, event.getType());
        assertEquals(testBusinessObject.getId(), event.getObjectId());
        assertEquals("No entries could be found for id " + instanceOne.getFormatId(), event.getMessage());
    }
    
    @Override
    public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {
        validationEvents.add((MetadataValidationEvent) event);
    }    
}