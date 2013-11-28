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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests that the serialize method for the metadata give the desired output.
 *
 */
public class EZIDMetadataTest {
    
    private EZIDMetadata metadata;
    private String expectedResult;
    
    @Before
    public void setup() {       
       
        metadata = new EZIDMetadata();
        metadata.addMetadata("_profile", "datacite");
        metadata.addMetadata("_target", "scheme://host:port/contextPath/collection/collection_splash.action?collectionId=collection:id");
        metadata.addMetadata("_status", "reserved");   
        metadata.addMetadata("datacite.resourceType", "Collection/Data");
        metadata.addMetadata("datacite.title", "title");
        metadata.addMetadata("datacite.publisher", "publisher");
        metadata.addMetadata("datacite.publicationDate", "2011");
        metadata.addMetadata("datacite.creator", "Foo, Bar");
        
        expectedResult = "_profile: datacite\n" +
                "_target: scheme%3A//host%3Aport/contextPath/collection/collection_splash.action?collectionId=collection%3Aid\n" +
                "_status: reserved\n" +
                "datacite.resourceType: Collection/Data\n" +
                "datacite.title: title\n" +
                "datacite.publisher: publisher\n" +
                "datacite.publicationDate: 2011\n" +
                "datacite.creator: Foo, Bar\n";
    }
    
    /**
     * Tests that the result of serialized matches the expected result.
     * @throws IOException
     */
    @Test
    public void testSerialize() throws IOException {
        String serialization = metadata.serialize();

        assertFalse(serialization.isEmpty());
        assertEquals(expectedResult, serialization);
    }
}