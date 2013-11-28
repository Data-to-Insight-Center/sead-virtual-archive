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
package org.dataconservancy.ui.it.support;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MetadataPreviewScreenResponseParserTest {

    private final Class<? extends MetadataPreviewScreenResponseParserTest> aClass = this.getClass();
    private final URL resource = aClass.getResource("MetadataPreview-success.html");
    private File validationSuccess = new File(resource.getPath());
    
    private final URL failureResource = aClass.getResource("MetadataPreview-failure.html");
    private File validationFailure = new File(failureResource.getPath());
    
    private final URL messageResource = aClass.getResource("MetadataPreview-updateMessage.html");
    private File validationMessage = new File(messageResource.getPath());

    @Test
    public void testSimple() throws Exception {
        final String parentId = "http://localhost:8080/collection/2";
        final String redirectUrl = "viewCollectionDetails";
        final String filename = "sample3-valid.xml";

        MetadataPreviewScreenResponseParser underTest = new MetadataPreviewScreenResponseParser(
                new FileInputStream(validationSuccess));

        assertEquals(parentId, underTest.getParentId());
        assertEquals(redirectUrl, underTest.getRedirectUrl());
        assertEquals(filename, underTest.getFilename());

        assertEquals(5, underTest.getSuccessEvents().size());
        assertEquals("Successful", underTest.getSuccessEvents().get(0));
        assertEquals("Validation succeeded: validation of an XML instance document with format id " +
                "dataconservancy.org:formats:file:metadata:fgdc:xml succeeded against the schema " +
                "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd obtained from " +
                "http://localhost:8080/dcs/datastream/http%3A%2F%2Flocalhost%3A8080%2Fdcs%2Fentity%2F1234",
                underTest.getSuccessEvents().get(1));
        
        assertEquals(1, underTest.getGeoSpatialAttributes().size());
        assertEquals("-80.22399 55.56779 -78.78479 55.56779 -78.78479 56.93479 -80.22399 56.93479 POLYGON", 
                     underTest.getGeoSpatialAttributes().get(0));
        assertEquals(1, underTest.getTemporalAttributes().size());
        assertEquals("725864400000 America/New_York 1238558400000 2010-04-01T00:00:00.000-04:00",
                     underTest.getTemporalAttributes().get(0));

    }
    
    @Test
    public void testFailure() throws Exception {
        final String parentId = "http://localhost:8080/collection/2";
        final String redirectUrl = "viewCollectionDetails";
        final String filename = "sample2.xml";

        MetadataPreviewScreenResponseParser underTest = new MetadataPreviewScreenResponseParser(
                new FileInputStream(validationFailure));

        assertEquals(parentId, underTest.getParentId());
        assertEquals(redirectUrl, underTest.getRedirectUrl());
        assertEquals(filename, underTest.getFilename());

        assertEquals(2, underTest.getFailureEvents().size());
        assertEquals("Failed", underTest.getFailureEvents().get(0));
        assertTrue(underTest.getFailureEvents().get(1).startsWith("Validation failed: validation of an XML instance document with format id " +
                "dataconservancy.org:formats:file:metadata:fgdc:xml failed against the schema " +
                "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd obtained from " +
                "http://localhost:8080/dcs/datastream/http%3A%2F%2Flocalhost%3A8080%2Fdcs%2Fentity%2F1234: " +
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'datsetid'. " +
                "One of '{citation}' is expected."));
    }
    
    @Test
    public void testMessage() throws Exception {
        final String parentId = "http://localhost:8080/collection/2";
        final String redirectUrl = "viewCollectionDetails";
        final String filename = "test";

        MetadataPreviewScreenResponseParser underTest = new MetadataPreviewScreenResponseParser(
                new FileInputStream(validationMessage));

        assertEquals(parentId, underTest.getParentId());
        assertEquals(redirectUrl, underTest.getRedirectUrl());
        assertEquals(filename, underTest.getFilename());

        assertEquals("The file test has already been validated and stored in the archive.", underTest.getMessage());
    }
    
    
}
