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
package org.dataconservancy.ui.it;

import org.dataconservancy.mhf.extractor.api.MetadataExtractor;
import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.mhf.services.*;
import org.dataconservancy.mhf.validators.XmlMetadataValidatorImpl;
import org.dataconservancy.mhf.validators.registry.impl.FormatRegistryImpl;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.shared.memory.InMemoryRegistry;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.model.*;
import org.dataconservancy.ui.services.MetadataBizService;
import org.dataconservancy.ui.services.MetadataBizServiceImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MetadataBizServiceIT extends BaseIT {
    private static Collection collection;
    private static Project project;
    private static DataItem dataItem;
    private static DataFile dataFileJPG1;
    private static DataFile dataFileJPG2;
    private static MetadataFile validMetadataFileFGDC;

    private static final String jpegDataFilePath = "/SampleDataFiles/purpleEXIFedited.jpg";
    private static final String jpegDataFile2Path = "/SampleDataFiles/bikesOfAmsterdam.jpg";

    @Autowired
    @Qualifier("metadataFinderRegistry")
    private InMemoryRegistry finderRegistry;

    @Autowired
    @Qualifier("schemeRegistry")
    private TypedRegistry<DcsMetadataScheme> dcsMetadataSchemeTypedRegistry;

    @Autowired
    @Qualifier("formatRegistryImpl")
    private TypedRegistry<DcsMetadataFormat> dcsMetadataFormatTypedRegistry;

    @Autowired
    @Qualifier("metadataExtractorRegistry")
    private InMemoryRegistry extractorRegistry;

    @Autowired
    @Qualifier("metadataValidatorRegistry")
    private InMemoryRegistry validatorRegistry;

    private MetadataBizService underTest;

    @Before
    public void setUp() throws MalformedURLException, URISyntaxException {

        assertNotNull(finderRegistry);
        assertEquals("dataconservancy.org:registry-entry:metadataFinder", finderRegistry.getType());
        Iterator itr = finderRegistry.iterator();
        while (itr.hasNext()) {
            Object object = itr.next();
            assertTrue(object instanceof BasicRegistryEntryImpl);
            BasicRegistryEntryImpl entry = (BasicRegistryEntryImpl)object;
            assertTrue(entry.getEntry() instanceof MetadataFinder);
        }

        assertNotNull(extractorRegistry);
        assertEquals("dataconservancy.org:registry-entry:metadataExtractor", extractorRegistry.getType());
        itr = extractorRegistry.iterator();
        while (itr.hasNext()) {
            Object object = itr.next();
            assertTrue(object instanceof BasicRegistryEntryImpl);
            BasicRegistryEntryImpl entry = (BasicRegistryEntryImpl)object;
            assertTrue(entry.getEntry() instanceof MetadataExtractor);
        }

        assertNotNull(validatorRegistry);
        assertEquals("dataconservancy.org:registry-entry:metadataValidator", validatorRegistry.getType());
        itr = validatorRegistry.iterator();
        while (itr.hasNext()) {
            Object object = itr.next();
            assertTrue(object instanceof BasicRegistryEntryImpl);
            BasicRegistryEntryImpl entry = (BasicRegistryEntryImpl)object;
            assertTrue(entry.getEntry() instanceof XmlMetadataValidatorImpl);
        }

        MetadataFindingService mdFindingService = new MetadataFindingServiceImpl(finderRegistry);
        AttributeSetMetadataExtractionServiceImpl mdExtractionService = new AttributeSetMetadataExtractionServiceImpl(extractorRegistry);
        MetadataValidationService mdValidationService = new MetadataValidationServiceImpl(mdFindingService, validatorRegistry);
        MetadataHandlingService mdHandlingService = new MetadataHandlingServiceImpl(mdValidationService,  mdExtractionService, mdFindingService);
        underTest = new MetadataBizServiceImpl(mdHandlingService);
        setUpProject();
        setUpCollection();
        setUpDataItem();
        setUpMetadataFile();
    }

    @Test
    public void testHandlingCollectionMetadata() {


        MetadataResult result = underTest.validateAndExtractMetadata(collection, "project:id/1");

        //assert that there is no extraction error
        assertEquals(0, result.getMetadataExtractionErrors().size());

        AttributeSet collectionCoreMDAS = null;
        AttributeSet systemMDAS = null;

        for (AttributeSet attributeSet : result.getAttributeSets()) {
            if (MetadataAttributeSetName.COLLECTION_CORE_METADATA.equals(attributeSet.getName())) {
                collectionCoreMDAS = attributeSet;
            } else if (MetadataAttributeSetName.SYSTEM_METADATA.equals(attributeSet.getName())) {
                systemMDAS = attributeSet;
            }
        }

        //Assert that only collection core and system attribute sets were found.
        assertEquals(2, result.getAttributeSets().size());
        assertNotNull("Collection core metadata attribute set was expected, but not found.", collectionCoreMDAS);
        assertNotNull("System metadata attribute set was expected, but not found.", systemMDAS);

        boolean foundCollectionTitle = false;
        boolean foundCollectionSummary = false;
        boolean foundCollectionId = false;
        boolean foundCollectionCitableLocator = false;
        boolean foundCollectionDepositorId = false;
        boolean foundCollectionAlternateId1 = false;
        boolean foundCollectionAlternateId2 = false;
        boolean foundCollectionPublicationDate = false;
        boolean foundCollectionDepositDate = false;
        boolean foundCollectionMDFileId = false;

        for (Attribute attribute : collectionCoreMDAS.getAttributes()) {
            if (attribute.getValue().equals(collection.getTitle())) {
                foundCollectionTitle = true;
            } else if (attribute.getValue().equals(collection.getSummary())) {
                foundCollectionSummary = true;
            } else if (attribute.getValue().equals(collection.getId())) {
                foundCollectionId = true;
            } else if (attribute.getValue().equals(collection.getCitableLocator())) {
                foundCollectionCitableLocator = true;
            } else if (attribute.getValue().equals(collection.getDepositorId())) {
                foundCollectionDepositorId = true;
            } else if (attribute.getValue().equals(collection.getAlternateIds().get(0))) {
                foundCollectionAlternateId1 = true;
            } else if (attribute.getValue().equals(collection.getAlternateIds().get(1))) {
                foundCollectionAlternateId2 = true;
            } else if (attribute.getValue().equals(String.valueOf(collection.getPublicationDate()))) {
                foundCollectionPublicationDate = true;
            }
        }

        assertTrue("Expected id for collection, but wasn't found.", foundCollectionId);
        assertTrue("Expected title for collection, but wasn't found.", foundCollectionTitle);
        assertTrue("Expected summary for collection, but wasn't found.", foundCollectionSummary);
        assertTrue("Expected citable locator for collection, but wasn't found.", foundCollectionCitableLocator);
        assertTrue("Expected alternate id1 for collection, but wasn't found.", foundCollectionAlternateId1);
        assertTrue("Expected alternate id2 for collection, but wasn't found.", foundCollectionAlternateId2);
        assertTrue("Expected publication date for collection, but wasn't found.", foundCollectionPublicationDate);

        //Assert that metadata file ids were not extracted
        assertFalse("Expected metadata file id for collection, but wasn't found.", foundCollectionMDFileId);

        for (Attribute attribute : systemMDAS.getAttributes()) {
            if (attribute.getValue().equals(collection.getDepositorId())) {
                foundCollectionDepositorId = true;
            } else if (attribute.getValue().equals(String.valueOf(collection.getDepositDate()))) {
                foundCollectionDepositDate = true;
            }
        }

        assertTrue("Expected depositor id for collection, but wasn't found.", foundCollectionDepositorId);
        assertTrue("Expected deposit date for collection, but wasn't found.", foundCollectionDepositDate);

        //assert that there is no validation error
        assertEquals(0,result.getMetadataValidationErrors().size());
    }

    @Test
    public void testHandlingDataItemMetadata() throws MalformedURLException, URISyntaxException {
        setUpDataItem();
        MetadataResult result = underTest.validateAndExtractMetadata(dataItem, null);

        AttributeSet spatialAttributeSet = null;
        AttributeSet temporalAttributeSet = null;
        AttributeSet keywordAttributeSet = null;
        List<AttributeSet> dataFileCoreAttributeSets = new ArrayList<AttributeSet>();
        AttributeSet systemAttributeSet = null;
        AttributeSet dataItemCoreAttributeSet = null;

        for (AttributeSet attributeSet : result.getAttributeSets()) {
            if (attributeSet.getName().equals(MetadataAttributeSetName.SPATIAL_METADATA)) {
                spatialAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                temporalAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.KEYWORD_METADATA)) {
                keywordAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.DATAFILE_CORE_METADATA)) {
                dataFileCoreAttributeSets.add(attributeSet);
            }else if (attributeSet.getName().equals(MetadataAttributeSetName.DATAITEM_CORE_METADATA)) {
                dataItemCoreAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.SYSTEM_METADATA)) {
                systemAttributeSet = attributeSet;
            }
        }

        //Check for proper DataItem core attribute set
        assertNotNull(dataItemCoreAttributeSet);

        boolean foundDataItemId = false;
        boolean foundDataItemName = false;
        boolean foundDataItemDescription = false;
        assertEquals(3, dataItemCoreAttributeSet.getAttributes().size());
        for (Attribute attribute : dataItemCoreAttributeSet.getAttributes()) {
            if (attribute.getName().equals(MetadataAttributeName.BUSINESS_ID) &&
                attribute.getValue().equals(dataItem.getId())) {
                foundDataItemId = true;
            } else if (attribute.getName().equals(MetadataAttributeName.TITLE) &&
                attribute.getValue().equals(dataItem.getName())) {
                foundDataItemName = true;
            }else if (attribute.getName().equals(MetadataAttributeName.DESCRIPTION) &&
                attribute.getValue().equals(dataItem.getDescription())) {
                foundDataItemDescription = true;
            }
        }

        assertTrue(foundDataItemDescription);
        assertTrue(foundDataItemId);
        assertTrue(foundDataItemName);

        //Check for proper dataFile Core attribute sets
        assertEquals(2, dataFileCoreAttributeSets.size());
        Iterator iterator;
        Attribute tempAttribute;
        Object tempObject;
        boolean foundDataFile1Title = false;
        boolean foundDataFile2Title = false;
        boolean foundDataFile2Format = false;
        boolean foundDataFile1Format = false;

        for (AttributeSet attributeSet : dataFileCoreAttributeSets) {
            assertTrue(attributeSet.getName().equals(MetadataAttributeSetName.DATAFILE_CORE_METADATA));
            //expecting only 2 core attribute to be extracted: title and format - if the DataFileMetadataFinder imple change
            //this will have to be updated
            assertEquals(2, attributeSet.getAttributes().size());
            iterator = attributeSet.getAttributes().iterator();
            while (iterator.hasNext()) {
                tempObject = iterator.next();
                assertTrue(tempObject instanceof Attribute);
                tempAttribute = (Attribute) tempObject;
                if (tempAttribute.getValue().equals(dataFileJPG1.getName())) {
                    foundDataFile1Title = true;
                } else if (tempAttribute.getValue().equals(dataFileJPG2.getName())) {
                    foundDataFile2Title = true;
                } else if (tempAttribute.getValue().equals(dataFileJPG1.getFormat())) {
                    foundDataFile1Format = true;
                } else if (tempAttribute.getValue().equals(dataFileJPG2.getFormat())) {
                    foundDataFile2Format = true;
                }
            }
        }

        assertTrue(foundDataFile1Format);
        assertTrue(foundDataFile2Format);
        assertTrue(foundDataFile2Title);
        assertTrue(foundDataFile1Title);


        //Assert that expected attribute sets are found:
        assertNotNull(spatialAttributeSet);
        assertNotNull(temporalAttributeSet);
        assertNotNull(keywordAttributeSet);
        assertNotNull(systemAttributeSet);

    }

    @Test
    public void testHandlingProjectMetadata() {
        setUpProject();
        MetadataResult result = underTest.validateAndExtractMetadata(project, null);

        AttributeSet coreAttributeSet = null;
        AttributeSet systemAttributeSet = null;
        for (AttributeSet attributeSet : result.getAttributeSets()) {
            if (attributeSet.getName().equals(MetadataAttributeSetName.PROJECT_CORE_METADATA)) {
                coreAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.SYSTEM_METADATA)) {
                systemAttributeSet = attributeSet;
            }
        }

        //Make sure no other (unexpected) attribute set was added to the result
        assertEquals(2, result.getAttributeSets().size());
        assertNotNull(coreAttributeSet);
        assertNotNull(systemAttributeSet);

        boolean foundProjectId = false;
        boolean foundProjectName = false;
        boolean foundProjectDescription = false;
        boolean foundProjectAwardNumber1 = false;
        boolean foundProjectAwardNumber2 = false;
        boolean foundProjectStartDate = false;
        boolean foundProjectEndDate = false;

        for (Attribute attribute : coreAttributeSet.getAttributes()) {
            if (attribute.getValue().equals(project.getName())) {
                foundProjectName = true;
            } else if (attribute.getValue().equals(project.getDescription())) {
                foundProjectDescription = true;
            } else if (attribute.getValue().equals(project.getId())) {
                foundProjectId = true;
            } else if (attribute.getValue().equals(project.getNumbers().get(0))) {
                foundProjectAwardNumber1 = true;
            } else if (attribute.getValue().equals(project.getNumbers().get(1))) {
                foundProjectAwardNumber2 = true;
            } else if (attribute.getValue().equals(String.valueOf(project.getStartDate()))) {
                foundProjectStartDate = true;
            } else if (attribute.getValue().equals(String.valueOf(project.getEndDate()))) {
                foundProjectEndDate = true;
            }
        }

        assertTrue(foundProjectName);
        assertTrue(foundProjectId);
        assertTrue(foundProjectDescription);
        assertTrue(foundProjectAwardNumber1);
        assertTrue(foundProjectAwardNumber2);
        assertTrue(foundProjectStartDate);
        //Project end date was never set
        assertFalse(foundProjectEndDate);

        //test that there was no system attribute set produced from extracting metadata on Project object
        //system attribute set would include deposit date and depositor information on the Project object.
        //Project object does not currently possess those attributes.
        assertEquals(0, systemAttributeSet.getAttributes().size());
    }

    @Ignore
    @Test
    public void testHandlingDataFileMetadata() {

    }

    /**
     * Test handling metadata file in form of an FGDC formated XML file.
     * <p/>
     * Expect to find:
     * <ul>
     *     <li>Spatial metadata attribute set</li>
     *     <li>Temporal metadata attribute set</li>
     *     <li>Keyword metadata attribute set</li>
     *     <li>System metadata attribute set</li>
     *     <li>Core metadata attribute set</li>
     * </ul>
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    //TODO: unignore when fgdc site comes back or different url can be used to load schema
    @Ignore
    @Test
    public void testValidMetadataFileValidationAndExtraction() throws MalformedURLException, URISyntaxException {
        
        MetadataResult result = underTest.validateAndExtractMetadata(setUpMetadataFile(), "data:item:id/1");

        AttributeSet spatialAttributeSet = null;
        AttributeSet temporalAttributeSet = null;
        AttributeSet keywordAttributeSet = null;
        AttributeSet coreAttributeSet = null;
        AttributeSet systemAttributeSet = null;
        for (AttributeSet attributeSet : result.getAttributeSets()) {
            if (attributeSet.getName().equals(MetadataAttributeSetName.SPATIAL_METADATA)) {
                spatialAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                temporalAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.KEYWORD_METADATA)) {
                keywordAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.METADATA_CORE_METADATA)) {
                coreAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.SYSTEM_METADATA)) {
                systemAttributeSet = attributeSet;
            }
        }

        //Assert that expected attribute sets are found:
        assertNotNull(spatialAttributeSet);
        assertNotNull(temporalAttributeSet);
        assertNotNull(keywordAttributeSet);
        assertNotNull(coreAttributeSet);
        assertNotNull(systemAttributeSet);

        //test that there was no system attribute set produced from extracing metadata on MetadataFile object
        //system attribute set would include deposit date and depositor information on the MetadataFile object.
        //MetadataFile object does not currently possess those attributes.
        assertEquals(0, systemAttributeSet.getAttributes().size());
        //assert that there is no validation error
        assertEquals(0,result.getMetadataValidationErrors().size());

    }

    @Ignore
    @Test
    public void testExtractionErrorReporting () {

    }

    @Ignore
    @Test
    public void testValidationErrorReporting () {

    }

    /**
     * Insures that there's at least one validation error reported when validating an invalid XML file; insures that
     * there are zero success messages when validating an invalid XML file.
     *
     * @throws MalformedURLException
     * @throws URISyntaxException
     */

    @Test
    public void testInvalidMetadataFileValidationAndExtraction() throws MalformedURLException, URISyntaxException {
        final MetadataResult result = underTest.validateAndExtractMetadata(setUpInvalidMetadataFile(), null);

        assertNotNull(result);
        assertTrue("Expecting validation errors when validating invalid file.", result.getMetadataValidationErrors().size() > 0);
        Set<MetadataResult.MetadataEventMessage> errorMessages = result.getMetadataValidationErrors();
        //Assert that Success messages were not empty
        for (MetadataResult.MetadataEventMessage message : errorMessages) {
            assertNotNull(message);
            assertFalse(message.getMessage().isEmpty());
        }

        Set<MetadataResult.MetadataEventMessage> messages = result.getMetadataValidationSuccesses();

        //No validation no longer returns a success message. So this should be 0.
        assertEquals(0, messages.size());

        for (MetadataResult.MetadataEventMessage message : messages) {
            //Assert that the successful messages are empty, as no validation process actually took place.
            assertTrue(message.getMessage().isEmpty());
        }

        //make sure no extraction messages were produced
        assertEquals(0, result.getMetadataExtractionSuccesses().size());
        assertEquals(0, result.getMetadataExtractionErrors().size());
    }
    
    @Test
    public void testValidMetadataFileValidation() throws MalformedURLException, URISyntaxException {
        MetadataResult result = underTest.validateMetadata(setUpMetadataFile());
        
        assertTrue("Expecting validation successes when validating a valid file", result
                .getMetadataValidationSuccesses().size() > 0);

        Set<MetadataResult.MetadataEventMessage> successMessages = result.getMetadataValidationSuccesses();
        // Assert that Success messages were not null
        for (MetadataResult.MetadataEventMessage message : successMessages) {
            assertNotNull(message);
        }

        // assert that there is no validation error
        for (MetadataResult.MetadataEventMessage message : result.getMetadataValidationErrors()) {
            System.out.println(message);
        }
        assertEquals(0, result.getMetadataValidationErrors().size());
        
        // Make sure no extraction messages were produced
        assertEquals(0, result.getMetadataExtractionSuccesses().size());
        assertEquals(0, result.getMetadataExtractionErrors().size());
    }
    
    @Test
    public void testInvalidMetadataFileValidation() throws MalformedURLException, URISyntaxException {
        final MetadataResult result = underTest.validateMetadata(setUpInvalidMetadataFile());
        
        assertNotNull(result);
        assertTrue("Expecting validation errors when validating invalid file.", result.getMetadataValidationErrors()
                .size() > 0);
        Set<MetadataResult.MetadataEventMessage> errorMessages = result.getMetadataValidationErrors();
        // Assert that Error messages were not empty
        for (MetadataResult.MetadataEventMessage message : errorMessages) {
            assertNotNull(message);
        }
        
        Set<MetadataResult.MetadataEventMessage> messages = result.getMetadataValidationSuccesses();
        
        //No validation no longer returns a success message. So this should be 0.
        assertEquals(0, messages.size());
        
        for (MetadataResult.MetadataEventMessage message : messages) {
            // Assert that the successful messages are empty, as no validation process actually took place.
            assertTrue(message.getMessage().isEmpty());
        }
        
        // make sure no extraction messages were produced
        assertEquals(0, result.getMetadataExtractionSuccesses().size());
        assertEquals(0, result.getMetadataExtractionErrors().size());
    }

    private void setUpProject() {
        project = new Project();
        project.setId("project:id");
        project.setDescription("project:description");
        project.setFundingEntity("Donut factory");
        project.setName("Sugar glazed donuts");
        project.setStartDate(new DateTime());
        project.addNumber("123456");
        project.addNumber("654321");

    }

    private void setUpCollection() {
        //Set up collection:
        ContactInfo ci1 = new ContactInfo("role1", "Footsie Tootsie", "email1@address.com", "555555555",
                new Address("1 Candy lane", "Wonkietown", "MD","12345", "USA"));
        ContactInfo ci2 = new ContactInfo("role2", "Chubby Cow", "email2@address.com", "666666666",
                new Address("1 Candy lane", "Wonkietown", "MD","12345", "USA"));
        List<ContactInfo> cis = new ArrayList<ContactInfo>();
        cis.add(ci1);
        cis.add(ci2);

        List<String> alternateIds = new ArrayList<String>();
        alternateIds.add("collection:id:alternate:1");
        alternateIds.add("collection:id:alternate:2");

        List<String> metadataFileIds = new ArrayList<String>();
        metadataFileIds.add("metadata:file1:id");

        PersonName creator1 = new PersonName("", "Mewing", "The", "Cat", "");
        PersonName creator2 = new PersonName("", "Woofing", "The", "Dog", "");
        PersonName creator3 = new PersonName("", "Gasteracantha", "Spider", "Arcuata", "");
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(creator1);
        creators.add(creator2);
        creators.add(creator3);

        collection = new Collection("collection:title","collection:summary", "collection:id", "collection:citable:locator",
                new DateTime("2012-03-01T15:26:04.009-05:00"), cis, alternateIds, creators, "depositor:id", new DateTime(), null, project.getId(), new ArrayList<String>());

    }

    private void setUpDataItem() throws URISyntaxException, MalformedURLException {
        List<String> metadataFileIds = new ArrayList<String>();
        metadataFileIds.add("metadata:file:id1");
        metadataFileIds.add("metadata:file:id2");
        metadataFileIds.add("metadata:file:id3");

        List<DataFile> dataFiles = new ArrayList<DataFile>();
        dataItem = new DataItem("data:item:name", "test data item", "data:item:id", "depositor:id", new DateTime(), dataFiles, metadataFileIds, collection.getId() );

        //TODO: create a couple data files

        URL url =  this.getClass().getResource(jpegDataFilePath);
        File jpdDataFile = new File(url.toURI());
        dataFileJPG1 = new DataFile();
        dataFileJPG1.setName("Test file 1");
        dataFileJPG1.setSource(jpdDataFile.toURI().toURL().toExternalForm());
        dataFileJPG1.setPath(jpdDataFile.getPath());
        dataFileJPG1.setFormat(MetadataFormatId.JPG_FORMAT_ID);
        dataItem.addFile(dataFileJPG1);

        url =  this.getClass().getResource(jpegDataFile2Path);
        jpdDataFile = new File(url.toURI());
        dataFileJPG2 = new DataFile();
        dataFileJPG2.setName("Test file 2");
        dataFileJPG2.setSource(jpdDataFile.toURI().toURL().toExternalForm());
        dataFileJPG2.setPath(jpdDataFile.getPath());
        dataFileJPG2.setFormat(MetadataFormatId.JPEG_FORMAT_ID);
        dataItem.addFile(dataFileJPG2);

    }

    private MetadataFile setUpMetadataFile() throws URISyntaxException, MalformedURLException {
        //TODO: create metadata file
        validMetadataFileFGDC = new MetadataFile("metadata:file1", null, "text/xml", "metadata:name:Sample2.xml", null, MetadataFormatId.FGDC_XML_FORMAT_ID, "");

        URL url = this.getClass().getResource(MHFResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH);
        File fgdcXmlFile = new File(url.toURI());
        assertNotNull(fgdcXmlFile);
        validMetadataFileFGDC.setSource(fgdcXmlFile.toURI().toURL().toExternalForm());
        validMetadataFileFGDC.setPath(fgdcXmlFile.getPath());
        return validMetadataFileFGDC;
    }

    /**
     * Returns a MetadataFile instance for an invalid FGDC XML file.  Note this is the same as {@link #setUpMetadataFile()}
     * right now, but I think the intent of {@link #setUpMetadataFile()} was to return a valid metadata file, not an
     * invalid one.  So at some point that method would be updated to return a valid file, distinguishing it from
     * the MetadataFile returned by this method.
     *
     * @return a MetadataFile that is invalid with regard to the FGDC 1998 XSD
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private MetadataFile setUpInvalidMetadataFile() throws URISyntaxException, MalformedURLException {
        final MetadataFile invalidFile = new MetadataFile("metadata:file2", null, "application/xml",
                "metadata:name:sample2.xml", null, MetadataFormatId.FGDC_XML_FORMAT_ID, "");
        final URL resource = this.getClass().getResource(MHFResources.SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH);
        assertNotNull(resource);
        invalidFile.setSource(resource.toURI().toURL().toExternalForm());
        File f = new File(resource.toURI());
        invalidFile.setPath(f.getParentFile().getPath());
        return invalidFile;
    }
}
