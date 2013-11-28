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


import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.test.support.BuilderTestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

public class JPEGDNMetadataExtractorTest extends BaseEXIFMetadataExtractorTest {
    private static final String pathToJpgFileWithKeywordCopyright = "/SampleImageFiles/purpleEXIFedited.jpg";
    private static final String pathToJpgFileWithAllExtractableMD = "/SampleImageFiles/bikesOfAmsterdam.jpg";
    private static final String pathToJpgFileWithGPSSpatialTemporal = "/SampleImageFiles/purple.JPG";
    private JPEGDNMetadataExtractor jpgExtractor;

    private MetadataInstance instanceWithSpatialTemporalMD;
    private MetadataInstance instanceWithKeywordCopyrightMD;
    private MetadataInstance instanceWithAllExtractableMD;

    @Before
    public void setUp() throws URISyntaxException, FileNotFoundException {

        jpgExtractor = new JPEGDNMetadataExtractor(BuilderTestUtil.newXstreamModelBuilder(),
                BuilderTestUtil.newXstreamAttributeValueBuilder());

        //Loading files
        URL url_jpgFileWithGPSSpatialTemporal =  this.getClass().getResource(pathToJpgFileWithGPSSpatialTemporal);

        URL url_jpgWithKeywordCopyright =  this.getClass().getResource(pathToJpgFileWithKeywordCopyright);

        URL url_jpgWithAllExtractableMD =  this.getClass().getResource(pathToJpgFileWithAllExtractableMD);

        instanceWithSpatialTemporalMD = new FileMetadataInstance(MetadataFormatId.JPG_FORMAT_ID, url_jpgFileWithGPSSpatialTemporal);
        instanceWithKeywordCopyrightMD = new FileMetadataInstance(MetadataFormatId.JPG_FORMAT_ID, url_jpgWithKeywordCopyright);
        instanceWithAllExtractableMD = new FileMetadataInstance(MetadataFormatId.JPG_FORMAT_ID, url_jpgWithAllExtractableMD);

        extractedMetadataWithSpatialTemporal = jpgExtractor.extractMetadata(instanceWithSpatialTemporalMD);
        extractedMetadataWithKeywordCopyright = jpgExtractor.extractMetadata(instanceWithKeywordCopyrightMD);
        extractedMetadataWithAllExtractable = jpgExtractor.extractMetadata(instanceWithAllExtractableMD);

    }


    /**
     * Test that temporal metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "purple.JPG".
     * <p/>
     * DONOT remove purple.JPG from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testTemporalMetadataExtracted() {
        super.testTemporalMetadataExtracted();
    }


    /**
     * Test that spatial metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "purple.JPG".
     * <p/>
     * DONOT remove purple.JPG from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testSpatialMetadataExtracted() {
        super.testSpatialMetadataExtracted();
    }

    /**
     * Test that copyright metadata is NOT one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "purple.JPG", and that attempt to extract copyright metadata from a file, which
     * does not contain it does not cause extractor to crash.
     * <p/>
     * DONOT remove purple.JPG from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testNoCopyrightMetadataExtracted() {
        super.testNoCopyrightMetadataExtracted();
    }


    /**
     * Test that keyword metadata is NOT one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "purple.JPG", and that attempt to extract keyword metadata from a file, which
     * does not contain it, does not cause extractor to crash.
     * <p/>
     * DONOT remove purple.JPG from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testNoKeywordMetadataExtracted() {
        super.testNoKeywordMetadataExtracted();
    }

    /**
     * Supplied a jpeg file ("purpleEXIFedited.jpg") with no embedded temporal metadata, extract metadata into
     * extractMetadata2:
     * <ul>
     *     <li>Make sure extractor does not crash</li>
     *     <li>Make sure no temporal attribute set shows up in the result </li>
     * </ul>
     * <p/>
     * DONOT remove purpleEXIFedited.JPG from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testNoTemporalMetadataExtracted() {
        super.testNoTemporalMetadataExtracted();
    }

    /**
     * Supplied a jpeg file ("purpleEXIFedited.jpg") with no embedded spatial metadata, extract metadata into
     * extractMetadata2:
     * <ul>
     *     <li>Make sure extractor does not crash</li>
     *     <li>Make sure no temporal attribute set shows up in the result </li>
     * </ul>
     * <p/>
     * DONOT remove purpleEXIFedited.jpg from "resources/SampleImageFiles/" folder. Running this test against other
     * files may produce unexpected results.
     */
    @Test
    public void testNoSpatialMetadataExtracted() {
        super.testNoSpatialMetadataExtracted();
    }


    /**
     * Test that copyright metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "purpleEXIFedited.jpg".
     * <p/>
     * DONOT remove purpleEXIFedited.jpg from "resources/SampleImageFiles/" folder. Running this test against other
     * files may produce unexpected results.
     */
    @Test
    public void testCopyrightMetadataExtracted() {
        super.testCopyrightMetadataExtracted();
    }

    /**
     * Test that keyword metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "purpleEXIFedited.jpg".
     * <p/>
     * DONOT remove purpleEXIFedited.jpg from "resources/SampleImageFiles/" folder. Running this test against other
     * files may produce unexpected results.
     */
    @Test
    public void testKeywordMetadataExtracted() {
        super.testKeywordMetadataExtracted();
    }

    /**
     * Test that all extractable metadata attribute sets are extracted. These include:
     * <ul>
     *     <li>Spatial Attribute Set</li>
     *     <li>Temporal Attribute Set</li>
     *     <li>Keyword Attribute Set</li>
     *     <li>Copyright Attribute Set</li>
     * </ul>
     * <p/>
     * DONOT remove bikesOfAmsterdam.jpg from "resources/SampleImageFiles/" folder. Running this test against other
     * files may produce unexpected results.
     */
    @Test
    public void testAllExtractableExtracted() {
        /*
        for (MetadataRepresentation representation : extractedMetadataWithAllExtractable) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            System.out.println(attributeSet);
        }*/
        super.testAllExtractableExtracted();
    }
}
