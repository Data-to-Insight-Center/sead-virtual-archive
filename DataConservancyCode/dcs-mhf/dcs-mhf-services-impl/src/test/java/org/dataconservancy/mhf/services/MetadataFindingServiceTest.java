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

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.ui.model.DataItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataFindingServiceTest {
    
    MetadataFindingService underTest;
    DataItem testBusinessObject;

    private final String INSTANCE_ONE_FORMAT = "format:one";
    private final String INSTANCE_TWO_FORMAT = "format:two";
    
    TypedRegistry<MetadataFinder> mockRegistry;
    
    private MetadataInstance instanceOne;
    private MetadataFinder finder;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        
        testBusinessObject = new DataItem();
        testBusinessObject.setId("dataItem");
        testBusinessObject.setName("test");
        testBusinessObject.setDescription("description");
        
        mockRegistry = (TypedRegistry<MetadataFinder>) mock(TypedRegistry.class);
        
        instanceOne = mock(MetadataInstance.class);
        MetadataInstance instanceTwo = mock(MetadataInstance.class);
        
        when(instanceOne.getFormatId()).thenReturn(INSTANCE_ONE_FORMAT);
        when(instanceTwo.getFormatId()).thenReturn(INSTANCE_TWO_FORMAT);
        
        List<MetadataInstance> instances = new ArrayList<MetadataInstance>();
        
        instances.add(instanceOne);
        instances.add(instanceTwo);
    
        Set<RegistryEntry<MetadataFinder>> entries = new HashSet<RegistryEntry<MetadataFinder>>();
        BasicRegistryEntryImpl<MetadataFinder> entry = (BasicRegistryEntryImpl<MetadataFinder>)mock(BasicRegistryEntryImpl.class);
        entries.add(entry);
 
        finder = mock(MetadataFinder.class);
        
        when(entry.getEntry()).thenReturn(finder);
        
        when(finder.findMetadata(testBusinessObject)).thenReturn(instances);
        
        when(mockRegistry.lookup(testBusinessObject.getClass().getName())).thenReturn(entries); 
       
        underTest = new MetadataFindingServiceImpl(mockRegistry);
    }
    
    /**
     * Tests that the finder returns the expected metadata instances.
     */
    @Test
    public void testFinderReturnsInstances() {
        
        List<MetadataInstance> instances = underTest.findMetadata(testBusinessObject);
        assertEquals(2, instances.size());
        
        List<String> instanceFormats = new ArrayList<String>();
        for(MetadataInstance instance : instances) {
            instanceFormats.add(instance.getFormatId());
        }
        
        assertTrue(instanceFormats.contains(INSTANCE_ONE_FORMAT));
        assertTrue(instanceFormats.contains(INSTANCE_TWO_FORMAT));
    }
    
    /**
     * Tests that if no finders are found and empty list is returned
     */
    @Test 
    public void testFinderReturnsEmptyInstanceList() {
        when(mockRegistry.lookup(testBusinessObject.getClass().getName())).thenReturn(new HashSet<RegistryEntry<MetadataFinder>>()); 
        
        List<MetadataInstance> instances = underTest.findMetadata(testBusinessObject);
        assertNotNull(instances);
        assertEquals(0, instances.size());
        
    }
}