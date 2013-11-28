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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.mhf.eventing.events.MetadataExtractionEvent;
import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
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
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttributeSetMetadataExtractionServiceTest implements MetadataHandlingEventListener {

    MetadataExtractionService underTest;

    private final String INSTANCE_ONE_FORMAT = "format:one";
    
    TypedRegistry<MetadataExtractor> mockRegistry;
    
    private MetadataInstance instanceOne;
    private MetadataExtractor extractorOne;
    private MetadataExtractor extractorTwo;
    
    private List<MetadataExtractionEvent> events;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws ExtractionException {
        
        mockRegistry = (TypedRegistry<MetadataExtractor>) mock(TypedRegistry.class);
        
        instanceOne = mock(MetadataInstance.class);
        
        when(instanceOne.getFormatId()).thenReturn(INSTANCE_ONE_FORMAT);
        
        List<MetadataInstance> instances = new ArrayList<MetadataInstance>();
        
        instances.add(instanceOne);
    
        extractorOne = mock(MetadataExtractor.class);
        extractorTwo = mock(MetadataExtractor.class);
        
        Set<RegistryEntry<MetadataExtractor>> entries = new HashSet<RegistryEntry<MetadataExtractor>>();
        BasicRegistryEntryImpl<MetadataExtractor> entry = (BasicRegistryEntryImpl<MetadataExtractor>)mock(BasicRegistryEntryImpl.class);
        entries.add(entry);

        BasicRegistryEntryImpl<MetadataExtractor> entryTwo = (BasicRegistryEntryImpl<MetadataExtractor>)mock(BasicRegistryEntryImpl.class);
        entries.add(entryTwo);
                
        when(entry.getEntry()).thenReturn(extractorOne);
        when(entryTwo.getEntry()).thenReturn(extractorTwo);
       
        when(mockRegistry.lookup(instanceOne.getFormatId(), AttributeSetMetadataRepresentation.REPRESENTATION_ID)).thenReturn(entries);
        List<MetadataRepresentation> reps = new ArrayList<MetadataRepresentation>();
        AttributeSetMetadataRepresentation rep = mock(AttributeSetMetadataRepresentation.class);
        
        AttributeSet attrSet = mock(AttributeSet.class);
        Set<Attribute> attrs = new HashSet<Attribute>();
        Attribute attr = new MetadataAttribute("name", "type", "value");
        attrs.add(attr);
        when(attrSet.getAttributes()).thenReturn(attrs);
        when(rep.getRepresentation()).thenReturn(attrSet);       
        when(rep.getRepresentationId()).thenReturn(AttributeSetMetadataRepresentation.REPRESENTATION_ID);
        reps.add(rep);
        
        when(extractorOne.extractMetadata(instanceOne)).thenReturn(reps);
        when(extractorTwo.extractMetadata(instanceOne)).thenReturn(reps);
        
        underTest = new AttributeSetMetadataExtractionServiceImpl(mockRegistry);
        
        events = new ArrayList<MetadataExtractionEvent>();
        MetadataHandlingEventManager.getInstance().registerListener(this);
    }
    
    /**
     * Tests that two metadata extractors fire two extraction events
     * @throws InterruptedException
     */
    @Test
    public void testTwoExtractorsTriggerTwoEvents() throws InterruptedException {
        Set<MetadataRepresentation<AttributeSet>> reps = underTest.extract(instanceOne);
        assertNotNull(reps);
        
        assertEquals(1, reps.size());
        AttributeSet returnedAttributes = reps.iterator().next().getRepresentation();
        Collection<Attribute> attributes = returnedAttributes.getAttributes();
        assertEquals(1, attributes.size());
        
        Thread.sleep(500);
        
        assertEquals(2, events.size());
        assertEquals(instanceOne.getFormatId(), events.get(0).getObjectId());
        assertEquals(instanceOne.getFormatId(), events.get(1).getObjectId());
    }
    
    /**
     * Tests that if one extractor throws an exception we still recieve both events.
     * @throws ExtractionException
     * @throws InterruptedException
     */
    @Test
    public void testOneExtractorThrowsException() throws ExtractionException, InterruptedException {
        doThrow(new ExtractionException("Exception message")).when(extractorOne).extractMetadata(instanceOne);

        assertNotNull(underTest.extract(instanceOne));
        
        Thread.sleep(500);
        
        assertEquals(2, events.size());
        
        List<String> messages = new ArrayList<String>();
        
        for (MetadataExtractionEvent event : events) {
            messages.add(event.getMessage());
        }
        
        assertTrue(messages.contains("Exception message"));
        
        assertEquals(instanceOne.getFormatId(), events.get(0).getObjectId());
        assertEquals(instanceOne.getFormatId(), events.get(1).getObjectId());
    }
    
    @Test
    public void testIncorrectRepresentationTriggersEvent() throws InterruptedException {
        MetadataRepresentation rep = mock(MetadataRepresentation.class);
        List<MetadataRepresentation> reps = new ArrayList<MetadataRepresentation>();
 
        when(rep.getRepresentationId()).thenReturn(AttributeSetMetadataRepresentation.REPRESENTATION_ID);
        reps.add(rep);
        
        when(extractorOne.extractMetadata(instanceOne)).thenReturn(reps);
        
        assertNotNull(underTest.extract(instanceOne));
        
        Thread.sleep(500);
        
        assertEquals(2, events.size());
        
        boolean classCastFound = false;
        for (MetadataExtractionEvent event : events) {
            if (event.getMessage().contains("cannot be cast")) {
                classCastFound = true;
            }
        }
        
        assertTrue(classCastFound);
        
        if (MetadataExtractionEvent.ExtractionEventType.ERROR == events.get(0).getType()) {
            assertEquals(MetadataExtractionEvent.ExtractionEventType.EXTRACTION, events.get(1).getType());
        } else {
            assertEquals(MetadataExtractionEvent.ExtractionEventType.ERROR, events.get(1).getType());
            assertEquals(MetadataExtractionEvent.ExtractionEventType.EXTRACTION, events.get(0).getType());
        }
        

        assertEquals(instanceOne.getFormatId(), events.get(0).getObjectId());
        assertEquals(instanceOne.getFormatId(), events.get(1).getObjectId());
    }
    
    @After
    public void tearDown() {
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
    }
    
    @Override
    public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {
        events.add((MetadataExtractionEvent) event);
    }
    
}