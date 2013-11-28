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
package org.dataconservancy.ui.services.util;

import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.services.BaseUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the metadata scheme registry wrapper. Note: This test uses the in memory registry to test the wrapper, in production the wrapper will likely
 * use the Archive backed registry.
 */
public class MetadataSchemeRegistryWrapperTest extends BaseUnitTest {
    
    private MetadataSchemeRegistryWrapper underTest;
    
    @Autowired
    @Qualifier("formatRegistryImpl")
    private TypedRegistry<DcsMetadataFormat> registry;
    
    @Before
    public void setup() {
        underTest = new MetadataSchemeRegistryWrapper("", "Registry of Metadata Schemes", registry);
    }
    
    /**
     * Tests that the iterator can successfully reach all of the schemes referenced in the format registry.
     */
    @Test
    public void testIterator() {
        int count = 0;
        Iterator<RegistryEntry<DcsMetadataScheme>> iter = underTest.iterator();
        while (iter.hasNext()) {
            RegistryEntry<DcsMetadataScheme> scheme = iter.next();           
            count++;
        }
        
        assertEquals(16, count);
    }
    
    /**
     * Tests that a scheme can be retrieved from the format registry.
     */
    @Test
    public void testRetrieve() {
        RegistryEntry<DcsMetadataScheme> schemeEntry = underTest.retrieve("classpath:/org/dataconservancy/mhf/resources/schemas/xsd/xml.xsd");
        assertNotNull(schemeEntry);
        
        DcsMetadataScheme scheme = schemeEntry.getEntry();
        assertNotNull(schemeEntry);
        
        assertEquals("XSD Schema: XML XSD", scheme.getName());
        assertEquals("http://www.w3.org/2001/xml.xsd", scheme.getSchemaUrl());
    }
       
    /**
     * Tests that retrieving a non existent scheme correctly returns null.
     */
    @Test
    public void testRetrieveNonExistent() {
        RegistryEntry<DcsMetadataScheme> schemeEntry = underTest.retrieve("foo");
        assertNull(schemeEntry);
    }
    
    /**
     * Tests that a look up correctly returns a single entry when passed a single key.
     */
    @Test
    public void testLookupSingleKey() {
        Set<RegistryEntry<DcsMetadataScheme>> returnedSchemes = underTest.lookup("datatypes.dtd");
        assertNotNull(returnedSchemes);
        
        assertEquals(1, returnedSchemes.size());
        
        assertNotNull(returnedSchemes.iterator().next().getEntry());
    }
    
    /**
     * Tests that a lookup correctly returns a single entry when passed multiple keys.
     */
    @Test
    public void testLookupMultipleKeys() {
        Set<RegistryEntry<DcsMetadataScheme>> returnedSchemes = underTest.lookup("fgdc-std-001-1998.xsd", "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
        assertNotNull(returnedSchemes);
        
        assertEquals(1, returnedSchemes.size());
        
        assertNotNull(returnedSchemes.iterator().next().getEntry());
    }
    
    /**
     * Tests that an empty list is returned when lookup is performed with a non existent key.
     */
    @Test
    public void testLookupNonExistentKey() {
        Set<RegistryEntry<DcsMetadataScheme>> returnedSchemes = underTest.lookup("foo");
        assertNotNull(returnedSchemes);
        
        assertEquals(0, returnedSchemes.size());
    }
    
    /**
     * Tests that when passed keys from two seperate schemes an empty list is performed since there is not a full match to the two keys.
     */
    @Test
    public void testMisMatchedKeys() {
        Set<RegistryEntry<DcsMetadataScheme>> returnedSchemes = underTest.lookup("XMLScheme.dtd", "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
        assertNotNull(returnedSchemes);
        
        assertEquals(0, returnedSchemes.size());
    }
}