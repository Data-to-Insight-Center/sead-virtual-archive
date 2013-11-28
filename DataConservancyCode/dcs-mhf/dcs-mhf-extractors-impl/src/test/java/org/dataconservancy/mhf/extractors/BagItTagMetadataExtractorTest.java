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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import org.junit.Before;
import org.junit.Test;

public class BagItTagMetadataExtractorTest {
    private final static String sampleBagItInfoFile = "/SampleMetadataFiles/bag-info.txt";
    private final static String sampleBagItFile = "/SampleMetadataFiles/bagit.txt";

    private BagItTagMetadataExtractor extractor;

    @Before
    public void setUp() {
        extractor = new BagItTagMetadataExtractor();
    }

    @Test
    public void testParsingBagIt() throws IOException {
        URL url = this.getClass().getResource(sampleBagItFile);

        Map<String, List<String>> expected = new HashMap<String, List<String>>();

        expected.put("BagIt-Version", Arrays.asList("0.97"));
        expected.put("Tag-File-Character-Encoding", Arrays.asList("UTF-8"));

        InputStream is = url.openStream();
        Map<String, List<String>> result = extractor.parseTags(is);
        is.close();

        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testParsingBagItInfo() throws IOException {
        URL url = this.getClass().getResource(sampleBagItInfoFile);

        Map<String, List<String>> expected = new HashMap<String, List<String>>();

        expected.put("BagIt-Profile-Identifier", Arrays.asList(
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9",
                "http://dataconservancy.org/formats/data-conservancy-pkg-X5"));
        expected.put("Contact-Name", Arrays.asList("Tim DiLauro"));
        expected.put("Contact-Phone", Arrays.asList("123-456-7890"));
        expected.put("Contact-Email", Arrays.asList("timmo@jhu.edu"));
        expected.put("External-Identifier", Arrays.asList("ELOKA002-pkg"));
        expected.put("Bag-Count", Arrays.asList("1 of 1"));
        expected.put("Bag-Group-Identifier", Arrays.asList("ELOKA002"));
        expected.put("PKG-ORE-REM", Arrays
                .asList("ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        expected.put("PKG-BAG-DIR", Arrays.asList("ELOKA002.bag/"));
        expected.put("Payload-Oxum", Arrays.asList("1190371083.25"));
        expected.put("Bagging-Date", Arrays.asList("2013-06-11"));
        expected.put("Bag-Size", Arrays.asList("1.1 GB"));

        InputStream is = url.openStream();
        Map<String, List<String>> result = extractor.parseTags(is);
        is.close();

        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testExtractionBagIt() {
        URL url = this.getClass().getResource(sampleBagItFile);

        MetadataInstance mi = new FileMetadataInstance(
                MetadataFormatId.BAGIT_TAG_FORMAT_ID, url);

        Collection<MetadataRepresentation> expected = new ArrayList<MetadataRepresentation>();

        MetadataAttributeSet bagit = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_METADATA);
        MetadataAttributeSet bagit_dc = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);

        add(bagit, "BagIt-Version", "0.97");
        add(bagit, "Tag-File-Character-Encoding", "UTF-8");

        AttributeSetMetadataRepresentation bagit_rep = new AttributeSetMetadataRepresentation(
                bagit);
        AttributeSetMetadataRepresentation bagit_dc_rep = new AttributeSetMetadataRepresentation(
                bagit_dc);

        expected.add(bagit_rep);
        expected.add(bagit_dc_rep);

        Collection<MetadataRepresentation> result = extractor
                .extractMetadata(mi);
        
        check_equality(expected, result);
    }
    
    @SuppressWarnings("rawtypes")
    private void check_equality(Collection<MetadataRepresentation> expected, Collection<MetadataRepresentation> result) {        
        Assert.assertNotNull(result);
        
        Collection<MetadataAttributeSet> unwrapped_expected = new HashSet<MetadataAttributeSet>();
        
        for (MetadataRepresentation rep: expected) {
            unwrapped_expected.add((MetadataAttributeSet) rep.getRepresentation());
        }
        
        Collection<MetadataAttributeSet> unwrapped_result = new HashSet<MetadataAttributeSet>();

        for (MetadataRepresentation rep: result) {
            unwrapped_result.add((MetadataAttributeSet) rep.getRepresentation());
        }
        
        Assert.assertEquals(unwrapped_expected, unwrapped_result);
    }

    private void add(MetadataAttributeSet set, String name, String... values) {
        for (String value : values) {
            set.addAttribute(new MetadataAttribute(name,
                    MetadataAttributeType.STRING, value));
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testExtractionBagItInfo() {
        URL url = this.getClass().getResource(sampleBagItInfoFile);

        MetadataInstance mi = new FileMetadataInstance(
                MetadataFormatId.BAGIT_TAG_FORMAT_ID, url);

        Collection<MetadataRepresentation> expected = new ArrayList<MetadataRepresentation>();

        MetadataAttributeSet bagit = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_METADATA);
        MetadataAttributeSet bagit_dc = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);

        add(bagit_dc, "BagIt-Profile-Identifier", "http://dataconservancy.org/formats/data-conservancy-pkg-0.9",
                "http://dataconservancy.org/formats/data-conservancy-pkg-X5");
        add(bagit, "Contact-Name", "Tim DiLauro");
        add(bagit, "Contact-Phone", "123-456-7890");
        add(bagit, "Contact-Email", "timmo@jhu.edu");
        add(bagit, "External-Identifier", "ELOKA002-pkg");
        add(bagit, "Bag-Count", "1 of 1");
        add(bagit, "Bag-Group-Identifier", "ELOKA002");
        add(bagit_dc, "PKG-ORE-REM", "ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml");
        add(bagit_dc, "PKG-BAG-DIR", "ELOKA002.bag/");
        add(bagit, "Payload-Oxum", "1190371083.25");
        add(bagit, "Bagging-Date", "2013-06-11");
        add(bagit, "Bag-Size", "1.1 GB");

        AttributeSetMetadataRepresentation bagit_rep = new AttributeSetMetadataRepresentation(
                bagit);
        AttributeSetMetadataRepresentation bagit_dc_rep = new AttributeSetMetadataRepresentation(
                bagit_dc);

        expected.add(bagit_rep);
        expected.add(bagit_dc_rep);

        Collection<MetadataRepresentation> result = extractor
                .extractMetadata(mi);
        
        check_equality(expected, result);
    }
}
