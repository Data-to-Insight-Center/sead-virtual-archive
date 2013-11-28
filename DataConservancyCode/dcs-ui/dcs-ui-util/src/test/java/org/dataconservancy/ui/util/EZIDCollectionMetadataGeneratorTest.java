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

package org.dataconservancy.ui.util;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.util.EZIDMetadata.MetadataPair;
import org.joda.time.DateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests that metadata is generated correctly from a collection, and proper exceptions are thrown when requirements are not met.
 *
 */
public class EZIDCollectionMetadataGeneratorTest {
    
    EZIDCollectionMetadataGeneratorImpl generator;
    
    @Before
    public void setup() {
        generator = new EZIDCollectionMetadataGeneratorImpl();
        generator.setContextPath("contextPath");
        generator.setHost("host");
        generator.setPort("port");
        generator.setPublisher("publisher");
        generator.setScheme("scheme");
    }
    
    /**
     * Tests that metadata is correctly created from a collection with all required fields.
     * @throws EZIDMetadataException
     */
    @Test
    public void testCollectionAllRequiredFields() throws EZIDMetadataException {
        Collection collection = new Collection();
        collection.setTitle("this is the title");
        collection.setSummary("this is the summary");
        collection.setId("collection:id");
        collection.setPublicationDate(new DateTime("2011-11-8"));
        PersonName creator = new PersonName();
        creator.setFamilyNames("Smith Doe");
        creator.setGivenNames("Joe");
        PersonName creatorTwo = new PersonName();
        creatorTwo.setFamilyNames("Foo");
        creatorTwo.setGivenNames("bar");
        collection.addCreator(creator);
        collection.addCreator(creatorTwo);
        
        EZIDMetadata metadata = generator.generateMetadata(collection);
        List<MetadataPair> mdList = metadata.getMetadata();
        assertEquals(9, mdList.size());
        
        for (MetadataPair md : mdList) {
            if (md.getParam().equalsIgnoreCase("_profile")) {
                assertTrue(md.getValue().equalsIgnoreCase("datacite"));
            } else if (md.getParam().equalsIgnoreCase("_target")) {
                assertTrue(md.getValue().equalsIgnoreCase("scheme://host:port/contextPath/collection/collection_splash.action?collectionId=collection:id"));
            } else if (md.getParam().equalsIgnoreCase("_status")) { 
                assertTrue(md.getValue().equalsIgnoreCase("reserved"));
            } else if (md.getParam().equalsIgnoreCase("datacite.resourceType")) {
                assertTrue(md.getValue().equalsIgnoreCase("Collection/Data"));
            } else if (md.getParam().equalsIgnoreCase("datacite.title")) {
                assertTrue(md.getValue().equalsIgnoreCase(collection.getTitle()));
            } else if (md.getParam().equalsIgnoreCase("datacite.publisher")) {
                assertTrue(md.getValue().equalsIgnoreCase("publisher"));
            } else if (md.getParam().equalsIgnoreCase("datacite.publicationDate")) {
                assertTrue(md.getValue().equalsIgnoreCase("2011"));
            } else if (md.getParam().equalsIgnoreCase("datacite.creator")) {
                assertTrue(md.getValue().equalsIgnoreCase("Smith Doe, Joe") || md.getValue().equalsIgnoreCase("Foo, bar"));                
            }
        }
    }
    
    /**
     * Tests that proper exception is thrown when creator is missing from collection.
     */
    @Test
    public void testMissingCreator() {
        Collection collection = new Collection();
        collection.setTitle("this is the title");
        collection.setSummary("this is the summary");
        collection.setId("collection:id");
        collection.setPublicationDate(new DateTime("2011-11-8"));

        boolean exceptionCaught = false;
        try {
            generator.generateMetadata(collection);
        } catch (EZIDMetadataException e) {
            exceptionCaught = true;
            assertTrue(e.getMessage().equalsIgnoreCase("Creators must be set to generate EZID metadata"));
        }
        
        assertTrue(exceptionCaught);
    }
    
    /**
     * Tests that correct exception is thrown when publication date is missing from collection.
     */
    @Test
    public void testMissingPublicationDate() {
        Collection collection = new Collection();
        collection.setTitle("this is the title");
        collection.setSummary("this is the summary");
        collection.setId("collection:id");
        PersonName creator = new PersonName();
        creator.setFamilyNames("Smith Doe");
        creator.setGivenNames("Joe");
        PersonName creatorTwo = new PersonName();
        creatorTwo.setFamilyNames("Foo");
        creatorTwo.setGivenNames("bar");
        collection.addCreator(creator);
        collection.addCreator(creatorTwo);
        
        boolean exceptionCaught = false;
        try {
            generator.generateMetadata(collection);
        } catch (EZIDMetadataException e) {
            exceptionCaught = true;
            assertTrue(e.getMessage().equalsIgnoreCase("Publication Date must be set to generate EZID metadata"));
        }
        
        assertTrue(exceptionCaught);
    }
    
}