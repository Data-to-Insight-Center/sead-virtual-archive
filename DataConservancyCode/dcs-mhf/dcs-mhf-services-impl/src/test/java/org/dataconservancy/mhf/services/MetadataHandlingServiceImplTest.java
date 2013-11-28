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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.mhf.eventing.events.MetadataExtractionEvent;
import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.extractor.api.MetadataExtractor;
import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.ui.model.DataItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

public class MetadataHandlingServiceImplTest implements MetadataHandlingEventListener {

    private MetadataHandlingService underTest;
    private AttributeSetMetadataExtractionServiceImpl extractionService;
    private MetadataValidationService validationService;
    private MetadataFindingService findingService;
    
    private final String INSTANCE_ONE_FORMAT = "format:one";
    private final String INSTANCE_TWO_FORMAT = "format:two";
    
    TypedRegistry<MetadataExtractor> extractorRegistry;
    TypedRegistry<MetadataValidator> validatorRegistry;
    TypedRegistry<MetadataFinder> finderRegistry;

    private DataItem testDataItem;
    
    private MetadataInstance instanceOne;
    private MetadataInstance instanceTwo;
    
    private MetadataExtractor extractorOne;
    private MetadataExtractor extractorTwo;
    
    private MetadataValidator validatorOne;
    private MetadataValidator validatorTwo;
    
    private MetadataFinder finder;

    private String representationEventMessage;
    
    private ArrayList<MetadataValidationEvent> validationEvents;
    private ArrayList<MetadataExtractionEvent> extractionEvents;
    
    private Attribute attribute;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws ExtractionException {
        
        //Set up all of the mock objects needed for the test.
        testDataItem = new DataItem();
        testDataItem.setId("dataItem");
        testDataItem.setName("test");
        testDataItem.setDescription("description");
        
        validationEvents = new ArrayList<MetadataValidationEvent>();
        extractionEvents = new ArrayList<MetadataExtractionEvent>();
        
        instanceOne = mock(MetadataInstance.class);
        instanceTwo = mock(MetadataInstance.class);
        
        when(instanceOne.getFormatId()).thenReturn(INSTANCE_ONE_FORMAT);
        when(instanceTwo.getFormatId()).thenReturn(INSTANCE_TWO_FORMAT);
        
        List<MetadataInstance> instances = new ArrayList<MetadataInstance>();
        
        instances.add(instanceOne);
        instances.add(instanceTwo);
        
        extractorOne = mock(MetadataExtractor.class);
        extractorTwo = mock(MetadataExtractor.class);
        
        List<MetadataRepresentation> reps = new ArrayList<MetadataRepresentation>();
        AttributeSetMetadataRepresentation rep = mock(AttributeSetMetadataRepresentation.class);
        
        AttributeSet attrSet = mock(AttributeSet.class);
        Set<Attribute> attrs = new HashSet<Attribute>();
        attribute = new MetadataAttribute("name", "type", "value");
        attrs.add(attribute);
        when(attrSet.getAttributes()).thenReturn(attrs);
        when(rep.getRepresentation()).thenReturn(attrSet);       
        when(rep.getRepresentationId()).thenReturn(AttributeSetMetadataRepresentation.REPRESENTATION_ID);
        reps.add(rep);
        representationEventMessage = rep.toString();
        
        when(extractorOne.extractMetadata(instanceOne)).thenReturn(reps);
        when(extractorTwo.extractMetadata(instanceTwo)).thenReturn(reps);
        
        //Set up the mock finder registry 
        finderRegistry = (TypedRegistry<MetadataFinder>) mock(TypedRegistry.class);
        Set<RegistryEntry<MetadataFinder>> finderEntries = new HashSet<RegistryEntry<MetadataFinder>>();
        BasicRegistryEntryImpl<MetadataFinder> finderEntry = (BasicRegistryEntryImpl<MetadataFinder>)mock(BasicRegistryEntryImpl.class);
        finderEntries.add(finderEntry);
 
        finder = mock(MetadataFinder.class);
        
        when(finderEntry.getEntry()).thenReturn(finder);
        
        when(finder.findMetadata(testDataItem)).thenReturn(instances);
        
        when(finderRegistry.lookup(testDataItem.getClass().getName())).thenReturn(finderEntries); 
        
        findingService = new MetadataFindingServiceImpl(finderRegistry);
        
        //Set up the mock extractor registry
        extractorRegistry = (TypedRegistry<MetadataExtractor>) mock(TypedRegistry.class);
        
        Set<RegistryEntry<MetadataExtractor>> extractorEntries = new HashSet<RegistryEntry<MetadataExtractor>>();
        BasicRegistryEntryImpl<MetadataExtractor> extractorEntry = (BasicRegistryEntryImpl<MetadataExtractor>)mock(BasicRegistryEntryImpl.class);
        extractorEntries.add(extractorEntry);

        Set<RegistryEntry<MetadataExtractor>> extractorEntriesTwo = new HashSet<RegistryEntry<MetadataExtractor>>();
        BasicRegistryEntryImpl<MetadataExtractor> extractorEntryTwo = (BasicRegistryEntryImpl<MetadataExtractor>)mock(BasicRegistryEntryImpl.class);
        extractorEntriesTwo.add(extractorEntryTwo);
                
        when(extractorEntry.getEntry()).thenReturn(extractorOne);
        when(extractorEntryTwo.getEntry()).thenReturn(extractorTwo);
       
        when(extractorRegistry.lookup(instanceOne.getFormatId(), AttributeSetMetadataRepresentation.REPRESENTATION_ID)).thenReturn(extractorEntries);
        when(extractorRegistry.lookup(instanceTwo.getFormatId(), AttributeSetMetadataRepresentation.REPRESENTATION_ID)).thenReturn(extractorEntriesTwo);
       
        extractionService = new AttributeSetMetadataExtractionServiceImpl(extractorRegistry);
        
        //Set up the validator registry for the validation service
        validatorRegistry = (TypedRegistry<MetadataValidator>) mock(TypedRegistry.class);

        Set<RegistryEntry<MetadataValidator>> validatorEntries = new HashSet<RegistryEntry<MetadataValidator>>();
        BasicRegistryEntryImpl<MetadataValidator> validatorEntry = (BasicRegistryEntryImpl<MetadataValidator>)mock(BasicRegistryEntryImpl.class);
        validatorEntries.add(validatorEntry);
        
        validatorOne = mock(MetadataValidator.class);
        when(validatorEntry.getEntry()).thenReturn(validatorOne);
        
        when(validatorRegistry.lookup(instanceOne.getFormatId())).thenReturn(validatorEntries);
        
        Set<RegistryEntry<MetadataValidator>> validatorEntriesTwo = new HashSet<RegistryEntry<MetadataValidator>>();
        BasicRegistryEntryImpl<MetadataValidator> validatorEntryTwo = (BasicRegistryEntryImpl<MetadataValidator>)mock(BasicRegistryEntryImpl.class);
        validatorEntriesTwo.add(validatorEntryTwo);
        
        validatorTwo = mock(MetadataValidator.class);
        when(validatorEntryTwo.getEntry()).thenReturn(validatorTwo);
        
        when(validatorRegistry.lookup(instanceTwo.getFormatId())).thenReturn(validatorEntriesTwo);
        validationService = new MetadataValidationServiceImpl(findingService, validatorRegistry);
        
        underTest = new MetadataHandlingServiceImpl(validationService, extractionService, findingService);
        
        MetadataHandlingEventManager.getInstance().registerListener(this);
    }
    
    /**
     * Test the standard case that all validation and extraction events occur.
     * @throws InterruptedException
     */
    @Test
    public void testBusinessObjectReturnsAllEvents() throws InterruptedException {
        Set<AttributeSet> attributes = underTest.validateAndExtractMetadata(testDataItem, "", null);
        assertNotNull(attributes);
        
        assertEquals(1, attributes.size());
        
        AttributeSet attrSet = attributes.iterator().next();
        assertNotNull(attrSet);
        assertEquals(1, attrSet.getAttributes().size());
        
        assertEquals(attribute, attrSet.getAttributes().iterator().next());
        
        Thread.sleep(500);
        
        assertEquals(2, validationEvents.size());
        for (MetadataValidationEvent event : validationEvents) {
            assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
            assertEquals(testDataItem.getId(), event.getObjectId());
        }
        
        assertEquals(2, extractionEvents.size());
    }
    
    /**
     * Test that is an extractor fails we get an exception message, and the message from the successful extraction.
     * @throws InterruptedException
     * @throws ExtractionException
     */
    @Test
    public void testFailedExtraction() throws InterruptedException, ExtractionException {
        when(extractorTwo.extractMetadata(instanceTwo)).thenThrow(new ExtractionException("extraction exception"));
        assertNotNull(underTest.validateAndExtractMetadata(testDataItem, "", null));
        
        Thread.sleep(500);
        
        assertEquals(2, validationEvents.size());
        for (MetadataValidationEvent event : validationEvents) {
            assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
            assertEquals(testDataItem.getId(), event.getObjectId());
        }
        
        assertEquals(2, extractionEvents.size());
        ArrayList<String> messages = new ArrayList<String>();
        
        for (MetadataExtractionEvent event : extractionEvents) {
            messages.add(event.getMessage());
        }
        
        assertTrue(messages.contains("extraction exception"));
        assertTrue(messages.contains(representationEventMessage));
    }
    
    /**
     * This test is to document current behavior that if one of the validators fails no extraction is preformed.
     * @throws ValidationException 
     * @throws InterruptedException 
     */
    @Test
    public void testFailedValidationStopsExtraction() throws ValidationException, InterruptedException {
        doThrow(new ValidationException("Exception message")).when(validatorOne).validate(instanceOne, null);
        
        assertNotNull(underTest.validateAndExtractMetadata(testDataItem, "", null));
        
        Thread.sleep(500);
        
        assertEquals(2, validationEvents.size());
        //Test to ensure we got one failure event and one pass event
        MetadataValidationEvent event = validationEvents.get(0);
        if (event.getType() == MetadataValidationEvent.ValidationType.PASS) {
            
            assertEquals(testDataItem.getId(), event.getObjectId());
            
            event = validationEvents.get(1);
            assertEquals(MetadataValidationEvent.ValidationType.FAILURE, event.getType());
            assertEquals(testDataItem.getId(), event.getObjectId());
            assertEquals("Exception message", event.getMessage());
        } else {
            assertEquals("Exception message", event.getMessage());
            assertEquals(testDataItem.getId(), event.getObjectId());

            event = validationEvents.get(1);
            assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
            assertEquals(testDataItem.getId(), event.getObjectId());
        }        
        
        assertEquals(0, extractionEvents.size());
    }
    
    /**
     * Tests that when finder registry can't find anything we return empty results with no errors.
     * @throws InterruptedException 
     */
    @Test 
    public void testFinderReturnsEmptyInstanceList() throws InterruptedException {
        when(finderRegistry.lookup(testDataItem.getClass().getName())).thenReturn(new HashSet<RegistryEntry<MetadataFinder>>()); 
        
        assertNotNull(underTest.validateAndExtractMetadata(testDataItem, "", null));
        
        Thread.sleep(500);
        
        assertEquals(0, validationEvents.size());
        assertEquals(0, extractionEvents.size());
        
    }
    
    @Test
    public void testValidDataItemValidation() throws Exception {
        underTest.validateMetadata(testDataItem, null);
        
        Thread.sleep(500);

        // Expecting to get two validation PASS events
        assertEquals(2, validationEvents.size());
        // Ensuring that both events are PASS.
        for (MetadataValidationEvent event : validationEvents) {
            assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
        }
    }
    
    @Test
    public void testInvalidDataItemValidation() throws Exception {
        doThrow(new ValidationException("Exception message")).when(validatorOne).validate(instanceOne, null);
        underTest.validateMetadata(testDataItem, null);
        
        Thread.sleep(500);

        // Expecting to get two validation events
        assertEquals(2, validationEvents.size());
        // Ensuring that one event is PASS and the other is FAILURE.
        MetadataValidationEvent event = validationEvents.get(0);
        if (event.getType() == MetadataValidationEvent.ValidationType.PASS) {
            event = validationEvents.get(1);
            assertEquals(MetadataValidationEvent.ValidationType.FAILURE, event.getType());
            assertEquals(testDataItem.getId(), event.getObjectId());
            assertEquals("Exception message", event.getMessage());
        } else {
            assertEquals("Exception message", event.getMessage());
            assertEquals(testDataItem.getId(), event.getObjectId());

            event = validationEvents.get(1);
            assertEquals(MetadataValidationEvent.ValidationType.PASS, event.getType());
            assertEquals(testDataItem.getId(), event.getObjectId());
        }
    }

    @Test
    public void testExtractionFromValidDataItem() throws Exception {
        Set<AttributeSet> attributes = underTest.extractMetadata(testDataItem);
        assertNotNull(attributes);

        assertEquals(1, attributes.size());

        AttributeSet attrSet = attributes.iterator().next();
        assertNotNull(attrSet);
        assertEquals(1, attrSet.getAttributes().size());

        assertEquals(attribute, attrSet.getAttributes().iterator().next());

        Thread.sleep(500);

        assertEquals(2, extractionEvents.size());
    }

    @After
    public void tearDown() {
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
    }
    
    @Override
    public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {
        if (event instanceof MetadataValidationEvent) {
            validationEvents.add((MetadataValidationEvent) event);
        } else if (event instanceof MetadataExtractionEvent) {
            extractionEvents.add((MetadataExtractionEvent) event);
        }
        
    }
    
}