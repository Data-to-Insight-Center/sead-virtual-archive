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

public class TIFFDNMetadataExtractorTest extends BaseEXIFMetadataExtractorTest {
    private static final String tiffWithSpatialTemporalPath = "/SampleImageFiles/sample-iptc-exif-geo.tiff";
    private static final String pathToJpgFileWithKeywordCopyright = "/SampleImageFiles/sample_exif_keyword_copyright.TIF";
    //private static final String pathToJpgFileWithAllExtractableMD = "/SampleImageFiles/sample-iptc-exif-geo.tiff";

    private TIFFDNMetadataExtractor tiffExtractor;
    private MetadataInstance instanceWithSpatialTemporalMD;
    private MetadataInstance instanceWithKeywordCopyrightMD;
    //private MetadataInstance instanceWithAllExtractableMD;


    @Before
    public void setUp() throws URISyntaxException, FileNotFoundException {

        tiffExtractor = new TIFFDNMetadataExtractor(BuilderTestUtil.newXstreamModelBuilder(),
                BuilderTestUtil.newXstreamAttributeValueBuilder());

        //Loading files
        URL url_tiffFileWithGPSSpatialTemporal =  this.getClass().getResource(tiffWithSpatialTemporalPath);

        URL url_tiffWithKeywordCopyright =  this.getClass().getResource(pathToJpgFileWithKeywordCopyright);

        /*
        url =  this.getClass().getResource(pathToJpgFileWithAllExtractableMD);
        File tiffWithAllExtractableMD = new File(url.toURI());
        */

        instanceWithSpatialTemporalMD = new FileMetadataInstance(MetadataFormatId.TIFF_FORMAT_ID, url_tiffFileWithGPSSpatialTemporal);
        instanceWithKeywordCopyrightMD = new FileMetadataInstance(MetadataFormatId.TIFF_FORMAT_ID, url_tiffWithKeywordCopyright);
        //instanceWithAllExtractableMD = new FileMetadataInstance(MetadataFormatId.TIFF_FORMAT_ID, new FileInputStream(tiffWithAllExtractableMD));

        extractedMetadataWithSpatialTemporal = tiffExtractor.extractMetadata(instanceWithSpatialTemporalMD);
        extractedMetadataWithKeywordCopyright = tiffExtractor.extractMetadata(instanceWithKeywordCopyrightMD);
        //extractedMetadataWithAllExtractable = tiffExtractor.extractMetadata(instanceWithAllExtractableMD);

    }

    /**
     * Test that temporal metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "sample-iptc-exif-geo.tiff".
     * <p/>
     * DONOT remove sample-iptc-exif-geo.tiff from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testTemporalMetadataExtracted() {
        super.testTemporalMetadataExtracted();
    }


    /**
     * Test that spatial metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "sample-iptc-exif-geo.tiff".
     * <p/>
     * DONOT remove sample-iptc-exif-geo.tiff from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testSpatialMetadataExtracted() {
        super.testSpatialMetadataExtracted();
    }

    /**
     * Test that copyright metadata is NOT one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "sample-iptc-exif-geo.tiff", and that attempt to extract copyright metadata from a file, which
     * does not contain it does not cause extractor to crash.
     * <p/>
     * DONOT remove sample-iptc-exif-geo.tiff from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testNoCopyrightMetadataExtracted() {
        super.testNoCopyrightMetadataExtracted();
    }

    /**
     * Test that keyword metadata is NOT one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "sample-iptc-exif-geo.tiff", and that attempt to extract keyword metadata from a file, which
     * does not contain it, does not cause extractor to crash.
     * <p/>
     * DONOT remove sample-iptc-exif-geo.tiff from "resources/SampleImageFiles/" folder. Running this test against other files may produce
     * unexpected results.
     */
    @Test
    public void testNoKeywordMetadataExtracted() {
        super.testNoKeywordMetadataExtracted();
    }

    /**
     * Test that copyright metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "sample_exif_keyword_copyright.TIF".
     * <p/>
     * DONOT remove sample_exif_keyword_copyright.TIF from "resources/SampleImageFiles/" folder. Running this test against other
     * files may produce unexpected results.
     */
    @Test
    public void testCopyrightMetadataExtracted() {
        super.testCopyrightMetadataExtracted();
    }

    /**
     * Test that keyword metadata is one of the attributeSets in the extractedMetadataWithSpatialTemporal collection of metadata
     * representation extracted from file "sample_exif_keyword_copyright.TIF".
     * <p/>
     * DONOT remove sample_exif_keyword_copyright.TIF from "resources/SampleImageFiles/" folder. Running this test against other
     * files may produce unexpected results.
     */
    @Test
    public void testKeywordMetadataExtracted() {
        super.testKeywordMetadataExtracted();
    }

}
