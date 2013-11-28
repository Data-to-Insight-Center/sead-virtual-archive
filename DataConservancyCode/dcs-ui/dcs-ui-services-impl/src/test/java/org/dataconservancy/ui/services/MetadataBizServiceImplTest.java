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
package org.dataconservancy.ui.services;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.services.AttributeSetMetadataExtractionServiceImpl;
import org.dataconservancy.mhf.services.MetadataExtractionService;
import org.dataconservancy.mhf.services.MetadataFindingService;
import org.dataconservancy.mhf.services.MetadataFindingServiceImpl;
import org.dataconservancy.mhf.services.MetadataHandlingService;
import org.dataconservancy.mhf.services.MetadataHandlingServiceImpl;
import org.dataconservancy.mhf.services.MetadataValidationService;
import org.dataconservancy.mhf.services.MetadataValidationServiceImpl;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.model.MetadataResult.MetadataEventMessage;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;


@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MetadataBizServiceImplTest extends BaseUnitTest {
    
    private MetadataBizServiceImpl underTest;
    
    private MetadataHandlingService handlingService;
    
    private final String INSTANCE_ONE_FORMAT = "format:one";

    private MetadataFinder finder;
    private MetadataInstance instanceOne;
    TypedRegistry<MetadataFinder> finderRegistry;
    private MetadataFindingService findingService;

    private MetadataValidationService validationService;
    private MetadataValidator validatorOne;
    TypedRegistry<MetadataValidator> validatorRegistry;

    private MetadataHandlingEventManager mockEventManager;

    private AttributeSet attributeSet;
    
    @Before
    public void setup() {        
        
        attributeSet = mock(AttributeSet.class);
        
        //Set up the mock finder registry 
        instanceOne = mock(MetadataInstance.class);        
        when(instanceOne.getFormatId()).thenReturn(INSTANCE_ONE_FORMAT);        
        List<MetadataInstance> instances = new ArrayList<MetadataInstance>();        
        instances.add(instanceOne);
        
        finderRegistry = (TypedRegistry<MetadataFinder>) mock(TypedRegistry.class);
        Set<RegistryEntry<MetadataFinder>> finderEntries = new HashSet<RegistryEntry<MetadataFinder>>();
        BasicRegistryEntryImpl<MetadataFinder> finderEntry = (BasicRegistryEntryImpl<MetadataFinder>)mock(BasicRegistryEntryImpl.class);
        finderEntries.add(finderEntry);
 
        finder = mock(MetadataFinder.class);
        
        when(finderEntry.getEntry()).thenReturn(finder);
        
        when(finder.findMetadata(metadataFileOne)).thenReturn(instances);
        
        when(finderRegistry.lookup(metadataFileOne.getClass().getName())).thenReturn(finderEntries);
        
        when(finder.findMetadata(metadataFileTwo)).thenReturn(instances);
        
        when(finderRegistry.lookup(metadataFileTwo.getClass().getName())).thenReturn(finderEntries);
        
        findingService = new MetadataFindingServiceImpl(finderRegistry);
        
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

        validationService = new MetadataValidationServiceImpl(findingService, validatorRegistry);
        
        AttributeSetMetadataRepresentation rep = new AttributeSetMetadataRepresentation(attributeSet);
        Set<AttributeSetMetadataRepresentation> reps = new HashSet<AttributeSetMetadataRepresentation>();
        reps.add(rep);
        AttributeSetMetadataExtractionServiceImpl extractionService = mock(AttributeSetMetadataExtractionServiceImpl.class);
        when(extractionService.extract((MetadataInstance) anyObject())).thenReturn(reps);
        handlingService = new MetadataHandlingServiceImpl(validationService, extractionService, findingService);

        //This is a made up attribute
        Attribute attr = new MetadataAttribute("name", "string", metadataFileOne.getName());
        Set<Attribute> attributes = new HashSet<Attribute>();
        attributes.add(attr);
        
        when(attributeSet.getAttributes()).thenReturn(attributes);
        
        Set<AttributeSet> attributeSets = new HashSet<AttributeSet>();
        attributeSets.add(attributeSet);
        underTest = new MetadataBizServiceImpl(handlingService);
        underTest.setMetadataValidationService(validationService);
    }
    
    /**
     * Tests that given a metadata file the service will return a metadata result with an attribute and a validation message.
     */
    @Test
    public void testSimpleValidationAndExtraction() {
        MetadataResult result = underTest.validateAndExtractMetadata(metadataFileOne, "");
        assertNotNull(result);
        
        Set<AttributeSet> returnedAttributeSets = result.getAttributeSets();
        assertNotNull(returnedAttributeSets);
        
        assertEquals(1, returnedAttributeSets.size());
        
        AttributeSet returnAttributeSet = returnedAttributeSets.iterator().next();
        
        assertNotNull(returnAttributeSet);
        
        assertEquals(1, returnAttributeSet.getAttributes().size());
        
        Attribute returnedAttribute = returnAttributeSet.getAttributes().iterator().next();
        
        assertNotNull(returnedAttribute);
        
        Set<MetadataEventMessage> messages = result.getMetadataValidationSuccesses();        
        assertEquals(1, messages.size());
        
        Set<MetadataEventMessage> errorMessages = result.getMetadataValidationErrors();        
        assertEquals(0, errorMessages.size());  
        
        assertEquals(metadataFileOne.getName(), returnedAttribute.getValue()); 
    }

    /**
     * Tests that given a metadata file the service will return a metadata result with an attribute and a validation message.
     */
    @Test
    public void testSimpleExtraction() {
        MetadataResult result = underTest.extractMetadata(metadataFileOne);
        assertNotNull(result);

        Set<AttributeSet> returnedAttributeSets = result.getAttributeSets();
        assertNotNull(returnedAttributeSets);

        assertEquals(1, returnedAttributeSets.size());

        AttributeSet returnAttributeSet = returnedAttributeSets.iterator().next();

        assertNotNull(returnAttributeSet);

        assertEquals(1, returnAttributeSet.getAttributes().size());

        Attribute returnedAttribute = returnAttributeSet.getAttributes().iterator().next();

        assertNotNull(returnedAttribute);

        assertEquals(metadataFileOne.getName(), returnedAttribute.getValue());
    }
    
    /**
     * Tests that validateMetadataSchema will have the correct messages when validation succeeds and fails.
     * @throws ValidationException
     * @throws IOException 
     * @throws MalformedURLException 
     */
    @Test
    public void testValidateMetadataSchema() throws ValidationException, MalformedURLException, IOException {
        //Set up 
        Set<RegistryEntry<MetadataValidator>> validatorEntries = new HashSet<RegistryEntry<MetadataValidator>>();
        BasicRegistryEntryImpl<MetadataValidator> validatorEntry = (BasicRegistryEntryImpl<MetadataValidator>)mock(BasicRegistryEntryImpl.class);
        validatorEntries.add(validatorEntry);
        
        validatorOne = mock(MetadataValidator.class);
        when(validatorEntry.getEntry()).thenReturn(validatorOne);
        
        when(validatorRegistry.lookup(anyString())).thenReturn(validatorEntries);
        
        //Test that a mocked validation passes and returns a validation message.
        MetadataResult result = underTest.validateMetadataSchema(new URL("http://www.dataconservancy.org"));
        assertNotNull(result);
        
        assertEquals(0, result.getAttributeSets().size());
        
        assertEquals(1, result.getMetadataValidationSuccesses().size());
        assertEquals(0, result.getMetadataValidationErrors().size());
        
        //Test that if validation fails an error message is recieved
        doThrow(new ValidationException("Exception message")).when(validatorOne).validate((MetadataInstance)anyObject(), (URL)anyObject());
        result = underTest.validateMetadataSchema(new URL("http://www.google.com"));
        assertNotNull(result);
        
        assertEquals(0, result.getAttributeSets().size());
        
        assertEquals(0, result.getMetadataValidationSuccesses().size());
        assertEquals(1, result.getMetadataValidationErrors().size());
    }

    @Test
    public void testSimpleInvalidMetadataFileValidation() throws Exception {
        doThrow(new ValidationException("Exception message")).when(validatorOne).validate((MetadataInstance)anyObject(), (URL)anyObject());
        MetadataResult result = underTest.validateMetadata(metadataFileOne);
        assertNotNull(result);
        
        assertTrue(result.getValidationPeformed());
        Set<MetadataEventMessage> messages = result.getMetadataValidationSuccesses();
        assertEquals(0, messages.size());
        
        Set<MetadataEventMessage> errorMessages = result.getMetadataValidationErrors();
        assertEquals(1, errorMessages.size());
    }
    
    @Test
    public void testNoOpEventRecieved() throws Exception {
        when(validatorRegistry.lookup(instanceOne.getFormatId())).thenReturn(new HashSet<RegistryEntry<MetadataValidator>>());
        
        MetadataResult result = underTest.validateMetadata(metadataFileOne);
        assertNotNull(result);
        
        assertFalse(result.getValidationPeformed());
        Set<MetadataEventMessage> messages = result.getMetadataValidationSuccesses();
        assertEquals(0, messages.size());
        
        Set<MetadataEventMessage> errorMessages = result.getMetadataValidationErrors();
        assertEquals(0, errorMessages.size());
        
        Set<MetadataEventMessage> warningMessages = result.getMetadataValidationWarnings();
        assertEquals(1, warningMessages.size());
    }
    
    @Test
    public void testSimpleValidMetadataFileValidation() throws Exception {
        MetadataResult result = underTest.validateMetadata(metadataFileOne);
        assertNotNull(result);
        
        assertTrue(result.getValidationPeformed());
        Set<MetadataEventMessage> messages = result.getMetadataValidationSuccesses();
        assertEquals(1, messages.size());
        
        Set<MetadataEventMessage> errorMessages = result.getMetadataValidationErrors();
        assertEquals(0, errorMessages.size());
    }
    
    @Test
    public void testSampleFileValidation() throws Exception {
        MetadataFile file = new MetadataFile();
        URL testFileURL = MetadataBizServiceImplTest.class.getResource("/SampleXML/project-testbop.xml");
        file.setSource(testFileURL.toString());
        file.setName("test");
        
        DcsMetadataFormat format = new DcsMetadataFormat();
        format.setName("Business Object Package");
        format.setVersion("1.0");
        format.setId("http://dataconservancy.org/schemas/bop/1.0");
        
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("Business Object Package");
        scheme.setSchemaVersion("1.0");
        scheme.setSchemaUrl("http://dataconservancy.org/schemas/bop/1.0");
        
        format.addScheme(scheme);
        
        MetadataResult result = underTest.validateMetadata(file, format);
        
        assertNotNull(result);
        
        assertNotNull(result.getMetadataValidationSuccesses());
        for(MetadataEventMessage message : result.getMetadataValidationErrors()) {
            System.out.println(message.getMessage());
        }
        assertEquals(1, result.getMetadataValidationSuccesses().size());
        
        assertEquals(0, result.getMetadataValidationErrors().size()); 
    }
    
    @Test
    public void testInvalidSampleFileValidation() throws Exception {
        MetadataFile file = new MetadataFile();
        URL testFileURL = MetadataBizServiceImplTest.class.getResource("sampleDeposit/426022.xml");
        file.setSource(testFileURL.toString());
        file.setName("test");
        
        DcsMetadataFormat format = new DcsMetadataFormat();
        format.setName("Business Object Package");
        format.setVersion("1.0");
        format.setId("http://dataconservancy.org/schemas/bop/1.0");
        
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("Business Object Package");
        scheme.setSchemaVersion("1.0");
        scheme.setSchemaUrl("http://dataconservancy.org/schemas/bop/1.0");
        
        format.addScheme(scheme);
        
        MetadataResult result = underTest.validateMetadata(file, format);
        
        assertNotNull(result);
        
        assertNotNull(result.getMetadataValidationSuccesses());
        
        assertEquals(0, result.getMetadataValidationSuccesses().size());
        
        assertEquals(1, result.getMetadataValidationErrors().size());
        

    }
    
}