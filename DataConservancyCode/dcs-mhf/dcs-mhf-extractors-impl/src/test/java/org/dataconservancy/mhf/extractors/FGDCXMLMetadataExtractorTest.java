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


package org.dataconservancy.mhf.extractors;

import junit.framework.Assert;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.*;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.DateTimeRange;
import org.dataconservancy.mhf.test.support.BuilderTestUtil;
import org.dataconservancy.model.gqm.Location;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

public class FGDCXMLMetadataExtractorTest {

    private static final String sampleFile = "/SampleMetadataFiles/sample2.xml";
    private static final String sampleFile_unknownDate = "/SampleMetadataFiles/FGDC_XML_unknown_date.xml";
    private static final String sampleFile_dateRange = "/SampleMetadataFiles/FGDC_XML_range_date.xml";
    private static final String sampleFile2 = "/SampleMetadataFiles/fgdc_sample_adopted_tmdls_jan12.shp.xml";

    private MetadataObjectBuilder moBuilder;
    private AttributeValueBuilder avBuilder;
    private FGDCXMLMetadataExtractor underTest;


    private MetadataInstance fgdcMetadataInstance;
    Collection<MetadataRepresentation> extractionRepresentation_withMultipleDates;
    Collection<MetadataRepresentation> extractionRepresentation_withRangeDate;
    Collection<MetadataRepresentation> extractionRepresentation_withUnknownDate;

    Collection<Attribute> extractedAttributes;
    @Before
    public void setUp() throws URISyntaxException, FileNotFoundException {
        moBuilder = BuilderTestUtil.newXstreamModelBuilder();
        avBuilder = BuilderTestUtil.newXstreamAttributeValueBuilder();

        underTest = new FGDCXMLMetadataExtractor(moBuilder,avBuilder);

        URL url_fgdc_xml_multiple_dates_file =  this.getClass().getResource(sampleFile);

        fgdcMetadataInstance = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, url_fgdc_xml_multiple_dates_file);
        extractionRepresentation_withMultipleDates = underTest.extractMetadata(fgdcMetadataInstance);
    }

    /**
     * This test is written specifically to test sampleFile.xml - wich contain metadata for dataset id "ELOKA002".
     * Performing this test on other files will yield unexpected and misleading result.
     *
     * Test the extracted metadata to make sure it contains one instance of spatial metadata as expected..
     */
    @Test
    public void testSpatialInformationExtracted() {
        AttributeSet foundAttributeSet = null;

        AttributeSet foundSpatialAttributeSet = null;

        for (MetadataRepresentation metadataRepresentation : extractionRepresentation_withMultipleDates) {
            if (metadataRepresentation.getRepresentationId().equals(AttributeSetMetadataRepresentation.REPRESENTATION_ID)) {
                foundAttributeSet = ((AttributeSetMetadataRepresentation) metadataRepresentation).getRepresentation();
                if (foundAttributeSet.getName().equals(MetadataAttributeSetName.SPATIAL_METADATA)) {
                    foundSpatialAttributeSet = foundAttributeSet;
                }
            }
        }

        //test that a spatial attribute set was found
        Assert.assertNotNull(foundSpatialAttributeSet);
        //test that only one spatial attribute was found in the set
        Assert.assertEquals(1, foundSpatialAttributeSet.getAttributes().size());
        //Get the attribute from the set
        Iterator<Attribute> iter = foundSpatialAttributeSet.getAttributes().iterator();
        Attribute spatialAttribute = iter.next();
        //test that the attribute type is Location
        Assert.assertTrue(spatialAttribute.getType().equals(MetadataAttributeType.LOCATION));
        //deserialize the attribute value for further examination
        ByteArrayInputStream bais = new ByteArrayInputStream(spatialAttribute.getValue().getBytes());
        Location extractedLocation = avBuilder.buildLocation(bais);
        Assert.assertNotNull(extractedLocation);


    }

    /**
     * This test is written specifically to test sampleFile.xml - which contain metadata for dataset id "ELOKA002".
     * Performing this test on other files will yield unexpected and misleading result.
     *
     * Test the extracted metadata to make sure it contains four instances of temporal metadata as expected..
     */
    @Test
    public void testTemporalInformationExtracted() {
        AttributeSet foundAttributeSet = null;

        AttributeSet foundTemporalAttributeSet = null;

        //Look for temporal attribute set
        for (MetadataRepresentation metadataRepresentation : extractionRepresentation_withMultipleDates) {
            if (metadataRepresentation.getRepresentationId().equals(AttributeSetMetadataRepresentation.REPRESENTATION_ID)) {
                foundAttributeSet = ((AttributeSetMetadataRepresentation) metadataRepresentation).getRepresentation();
                if (foundAttributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                    foundTemporalAttributeSet = foundAttributeSet;
                }
            }
        }

        //test that found temporal attribute set is not null
        Assert.assertNotNull(foundTemporalAttributeSet);
        //Test for expected number of temporal attributes in the set
        //There should be 4 Attributes: 3 coming from the multiple dates of the timeperd section, 1 coming from pubdate section
//        Assert.assertEquals(4, foundTemporalAttributeSet.getAttributes().size());
        Iterator<Attribute> iter = foundTemporalAttributeSet.getAttributes().iterator();
        while (iter.hasNext()) {
            Attribute temporalAttribute = iter.next();
            Assert.assertTrue(temporalAttribute.getType().equals(MetadataAttributeType.DATE_TIME));
            System.out.println(temporalAttribute);
        }
    }

    /**
     * This test is written specifically to test sampleFile.xml - wich contain metadata for dataset id "ELOKA002".
     * Performing this test on other files will yield unexpected and misleading result.
     *
     * Test the extracted metadata to make sure it contains 87 instances of keyword metadata as expected..
     */
    @Test
    public void testKeywordInformationExtracted() {
        AttributeSet foundAttributeSet = null;

        AttributeSet foundTemporalAttributeSet = null;

        for (MetadataRepresentation metadataRepresentation : extractionRepresentation_withMultipleDates) {
            if (metadataRepresentation.getRepresentationId().equals(AttributeSetMetadataRepresentation.REPRESENTATION_ID)) {
                foundAttributeSet = ((AttributeSetMetadataRepresentation) metadataRepresentation).getRepresentation();
                if (foundAttributeSet.getName().equals(MetadataAttributeSetName.KEYWORD_METADATA)) {
                    foundTemporalAttributeSet = foundAttributeSet;
                }
            }
        }

        Assert.assertNotNull(foundTemporalAttributeSet);
        Assert.assertEquals(87, foundTemporalAttributeSet.getAttributes().size());
    }

    @Test
    public void testDateRangeExtracted() throws FileNotFoundException, URISyntaxException {
        URL urlDateRangeFile =  this.getClass().getResource(sampleFile_dateRange);
        File fgdc_xml_range_date_file = new File(urlDateRangeFile.toURI());

        fgdcMetadataInstance = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, urlDateRangeFile);
        extractionRepresentation_withRangeDate = underTest.extractMetadata(fgdcMetadataInstance);

        AttributeSet foundAttributeSet = null;

        AttributeSet foundTemporalAttributeSet = null;

        //Look for temporal attribute set
        for (MetadataRepresentation metadataRepresentation : extractionRepresentation_withRangeDate) {
            if (metadataRepresentation.getRepresentationId().equals(AttributeSetMetadataRepresentation.REPRESENTATION_ID)) {
                foundAttributeSet = ((AttributeSetMetadataRepresentation) metadataRepresentation).getRepresentation();
                if (foundAttributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                    foundTemporalAttributeSet = foundAttributeSet;
                }
            }
        }

        //test that found temporal attribute set is not null
        Assert.assertNotNull(foundTemporalAttributeSet);
        //Test for expected number of temporal attributes in the set
        //There should be 4 Attributes: 3 coming from the multiple dates of the timeperd section, 1 coming from pubdate section
        Assert.assertEquals(2, foundTemporalAttributeSet.getAttributes().size());
        Iterator<Attribute> iter = foundTemporalAttributeSet.getAttributes().iterator();
        while (iter.hasNext()) {
            Attribute temporalAttribute = iter.next();
            Assert.assertTrue(temporalAttribute.getType().equals(MetadataAttributeType.DATE_TIME) || temporalAttribute.getType().equals(MetadataAttributeType.DATE_TIME_RANGE));
        }

    }

    @Test
    public void testUnknownDateExtracted() throws FileNotFoundException, URISyntaxException {
        URL urlUnknownDate =  this.getClass().getResource(sampleFile_unknownDate);

        fgdcMetadataInstance = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, urlUnknownDate);
        extractionRepresentation_withUnknownDate = underTest.extractMetadata(fgdcMetadataInstance);

        AttributeSet foundAttributeSet = null;

        AttributeSet foundTemporalAttributeSet = null;

        //Look for temporal attribute set
        for (MetadataRepresentation metadataRepresentation : extractionRepresentation_withUnknownDate) {
            if (metadataRepresentation.getRepresentationId().equals(AttributeSetMetadataRepresentation.REPRESENTATION_ID)) {
                foundAttributeSet = ((AttributeSetMetadataRepresentation) metadataRepresentation).getRepresentation();
                if (foundAttributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                    foundTemporalAttributeSet = foundAttributeSet;
                }
            }
        }

        //test that found temporal attribute set is not null
        Assert.assertNotNull(foundTemporalAttributeSet);
        //Test for expected number of temporal attributes in the set
        //There should be 4 Attributes: 3 coming from the multiple dates of the timeperd section, 1 coming from pubdate section
        Assert.assertEquals(2, foundTemporalAttributeSet.getAttributes().size());
        Iterator<Attribute> iter = foundTemporalAttributeSet.getAttributes().iterator();
        while (iter.hasNext()) {
            Attribute temporalAttribute = iter.next();
            Assert.assertTrue(temporalAttribute.getType().equals(MetadataAttributeType.DATE_TIME) || temporalAttribute.getType().equals(MetadataAttributeType.DATE_TIME_RANGE));
            if ( temporalAttribute.getType().equals(MetadataAttributeType.DATE_TIME_RANGE)) {
                //deserialize the attribute value for further examination
                ByteArrayInputStream bais = new ByteArrayInputStream(temporalAttribute.getValue().getBytes());
                DateTimeRange extractDateRange = avBuilder.buildDateTimeRange(bais);
                Assert.assertNull(extractDateRange.getEndDateTime());
            }
        }

    }

}
