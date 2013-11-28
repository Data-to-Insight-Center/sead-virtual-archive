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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeType;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.Pair;
import org.junit.Before;
import org.junit.Test;

public class BagItManfiestMetadataExtractorTest {
    private static final String SAMPLE_BAG_IT_PAYLOAD_MANIFEST_FILE = "/SampleMetadataFiles/manifest-md5.txt";
    private static final String SAMPLE_BAG_IT_TAG_MANIFEST_FILE = "/SampleMetadataFiles/tagmanifest-md5.txt";
    private static final String MANIFEST_ENTRY = "Manifest-Entry";

    private BagItManifestMetadataExtractor extractor;

    @Before
    public void setUp() {
        extractor = new BagItManifestMetadataExtractor("md5");
    }

    @Test
    public void testParsingPayloadManifest() throws IOException {
        URL url = this.getClass().getResource(SAMPLE_BAG_IT_PAYLOAD_MANIFEST_FILE);

        List<Pair<String, ChecksumImpl>> expected = new ArrayList<Pair<String, ChecksumImpl>>();

        expected.add(new Pair<String, ChecksumImpl>("data/ELOKA/ELOKA-example.bop.xml", new ChecksumImpl("md5",
                "812251865175d4b3a4eeac749feef3a6")));
        expected.add(new Pair<String, ChecksumImpl>("data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk2.mp4",
                new ChecksumImpl("md5", "653d814f5f1bfa459d3a7b4e86f99660")));
        expected.add(new Pair<String, ChecksumImpl>("data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk1.mp4",
                new ChecksumImpl("md5", "83b894013bd41abe981af2fc28a54780")));

        InputStream is = url.openStream();
        List<Pair<String, ChecksumImpl>> result = extractor.parseData(is);
        is.close();

        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void testParsingManifestWithFilenameWhitespace() throws IOException {
        List<Pair<String, ChecksumImpl>> expected = new ArrayList<Pair<String, ChecksumImpl>>();

        expected.add(new Pair<String, ChecksumImpl>("annoying monkey.mp4", new ChecksumImpl("md5",
                "812251865175d4b3a4eeac749feef3a6")));

        String input = "812251865175d4b3a4eeac749feef3a6 annoying monkey.mp4\n";
        
        InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
        List<Pair<String, ChecksumImpl>> result = extractor.parseData(is);
        is.close();

        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testParsingTagManifest() throws IOException {
        URL url = this.getClass().getResource(SAMPLE_BAG_IT_TAG_MANIFEST_FILE);
        
        List<Pair<String, ChecksumImpl>> expected = new ArrayList<Pair<String, ChecksumImpl>>();
        
        expected.add(new Pair<String, ChecksumImpl>(
                "ORE-REM/de7243bc-d12b-11e2-aee4-90a790b0b17a-ReM.xml", new ChecksumImpl("md5",
                        "615d3d3562ffe634b6832fc23db9345f")));
        expected.add(new Pair<String, ChecksumImpl>(
                "ORE-REM/ae15fc6a-3f92-4e58-9297-d254d81359f7-ReM.xml", new ChecksumImpl("md5",
                        "02a5fa0e9b17229d4649ab06509372a3")));
        expected.add(new Pair<String, ChecksumImpl>("bag-info.txt", new ChecksumImpl("md5",
                "67cd77caee30448c66286861d0de85e9")));
        
        InputStream is = url.openStream();
        List<Pair<String, ChecksumImpl>> result = extractor.parseData(is);
        is.close();
        
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testExtractPayloadManifest() {
        URL url = this.getClass().getResource(SAMPLE_BAG_IT_PAYLOAD_MANIFEST_FILE);

        MetadataInstance mi = new FileMetadataInstance(MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID, url);

        Collection<MetadataRepresentation> expected = new ArrayList<MetadataRepresentation>();

        MetadataAttributeSet manifest = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_METADATA);

        manifest.addAttribute((new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                new Pair<String, ChecksumImpl>("data/ELOKA/ELOKA-example.bop.xml", new ChecksumImpl("md5",
                        "812251865175d4b3a4eeac749feef3a6")).toString())));
        manifest.addAttribute((new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                new Pair<String, ChecksumImpl>("data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk2.mp4",
                        new ChecksumImpl("md5", "653d814f5f1bfa459d3a7b4e86f99660")).toString())));
        manifest.addAttribute((new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                new Pair<String, ChecksumImpl>("data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk1.mp4",
                        new ChecksumImpl("md5", "83b894013bd41abe981af2fc28a54780")).toString())));

        AttributeSetMetadataRepresentation metadataRep = new AttributeSetMetadataRepresentation(
                manifest);

        expected.add(metadataRep);

        Collection<MetadataRepresentation> result = extractor.extractMetadata(mi);
        
        checkEquality(expected, result);
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testExtractTagManifest() {
        URL url = this.getClass().getResource(SAMPLE_BAG_IT_TAG_MANIFEST_FILE);

        MetadataInstance mi = new FileMetadataInstance(MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID, url);

        Collection<MetadataRepresentation> expected = new ArrayList<MetadataRepresentation>();

        MetadataAttributeSet manifest = new MetadataAttributeSet(MetadataAttributeSetName.BAGIT_METADATA);

        manifest.addAttribute((new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                new Pair<String, ChecksumImpl>("ORE-REM/de7243bc-d12b-11e2-aee4-90a790b0b17a-ReM.xml",
                        new ChecksumImpl("md5", "615d3d3562ffe634b6832fc23db9345f")).toString())));
        manifest.addAttribute((new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                new Pair<String, ChecksumImpl>("ORE-REM/ae15fc6a-3f92-4e58-9297-d254d81359f7-ReM.xml",
                        new ChecksumImpl("md5", "02a5fa0e9b17229d4649ab06509372a3")).toString())));
        manifest.addAttribute((new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                new Pair<String, ChecksumImpl>("bag-info.txt", new ChecksumImpl("md5",
                        "67cd77caee30448c66286861d0de85e9")).toString())));

        AttributeSetMetadataRepresentation manifestRep = new AttributeSetMetadataRepresentation(manifest);

        expected.add(manifestRep);

        Collection<MetadataRepresentation> result = extractor.extractMetadata(mi);
        
        checkEquality(expected, result);
    }
    
    @SuppressWarnings("rawtypes")
    private void checkEquality(Collection<MetadataRepresentation> expected, Collection<MetadataRepresentation> result) {
        Assert.assertNotNull(result);
        
        Collection<MetadataAttributeSet> expectedAttSets = new HashSet<MetadataAttributeSet>();
        
        for (MetadataRepresentation rep : expected) {
            expectedAttSets.add((MetadataAttributeSet) rep.getRepresentation());
        }
        
        Collection<MetadataAttributeSet> resultAttSets = new HashSet<MetadataAttributeSet>();
        
        for (MetadataRepresentation rep : result) {
            resultAttSets.add((MetadataAttributeSet) rep.getRepresentation());
        }
        
        Assert.assertEquals(expectedAttSets, resultAttSets);
    }

}
