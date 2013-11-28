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

package org.dataconservancy.mhf.finders;

import org.dataconservancy.mhf.finder.api.MetadataFindingException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.AttributeSetMetadataInstance;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.DATE_TIME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.PERSON_NAME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;
import static org.dataconservancy.mhf.test.support.BuilderTestUtil.newXstreamAttributeValueBuilder;
import static org.dataconservancy.mhf.test.support.BuilderTestUtil.newXstreamModelBuilder;
import static org.junit.Assert.*;

/**
 * Insures that the correct metadata is discovered on the Collection business object.
 */
public class CollectionMetadataFinderTest {
    private Collection collection;

    private CollectionMetadataFinder underTest;
    private MetadataObjectBuilder moBuilder;
    private AttributeValueBuilder avBuilder;

    private final static String SUMMARY = "Collection Summary";
    private final static String TITLE = "Collection Title";
    private final static String DEPOSIT_DATE = "2013-01-13";
    private final static String PUBLICATION_DATE = "2013-01-15";
    private final static String CITABLE_LOCATOR = "http://dx.doi.org/1234";
    private final static String DEPOSITOR_ID = "depositorId";
    private final static DateTime DEPOSIT_DATETIME = DateTime.parse(DEPOSIT_DATE);
    private final static DateTime PUBLICATION_DATETIME = DateTime.parse(PUBLICATION_DATE);
    private final static String COLLECTION_ID = "collectionId";
    private final static String ALT_ID1 = "altId1";
    private final static String ALT_ID2 = "altId2";
    private final static String CREATOR_GN = "Foo";
    private final static String CREATOR_SN = "Bar";
    private final static String CREATOR_MN = "Baz";

    private final List<String> altIds = new ArrayList<String>();
    private final MetadataAttributeSet expectedCoreMd = new MetadataAttributeSet(MetadataAttributeSetName.COLLECTION_CORE_METADATA);
    private final MetadataAttributeSet expectedSystemMd = new MetadataAttributeSet(MetadataAttributeSetName.SYSTEM_METADATA);


    @Before
    public void setUp() {
                //----- Set up collaborating objects: moBuilder
        moBuilder = newXstreamModelBuilder();
        avBuilder = newXstreamAttributeValueBuilder();
        //-------Setup collaborating objects: COLLECTION
        collection = new Collection();

        // Set fields on the Collection
        collection.setId(COLLECTION_ID);
        collection.setSummary(SUMMARY);
        collection.setTitle(TITLE);
        collection.setDepositDate(DEPOSIT_DATETIME);
        collection.setPublicationDate(PUBLICATION_DATETIME);
        collection.setCitableLocator(CITABLE_LOCATOR);
        collection.setDepositorId(DEPOSITOR_ID);
        altIds.add(ALT_ID1);
        altIds.add(ALT_ID2);
        collection.setAlternateIds(altIds);
        PersonName name1 = new PersonName(null, CREATOR_GN, CREATOR_MN, CREATOR_SN, null);
        collection.addCreator(name1);


        // Core metadata: the expected core metadata attribute set

        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.BUSINESS_ID, STRING, COLLECTION_ID));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, STRING, SUMMARY));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.TITLE, STRING, TITLE));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.PUBLICATION_DATE, DATE_TIME,
                String.valueOf(PUBLICATION_DATETIME)));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, STRING, ALT_ID1));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, STRING, ALT_ID2));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avBuilder.buildPersonName(name1, baos);
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.CREATOR, PERSON_NAME, baos.toString()));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.CITABLE_LOCATOR, STRING,
                CITABLE_LOCATOR));


        // System metadata: the expected system metadata attribute set
        expectedSystemMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSIT_DATE, DATE_TIME, String.valueOf(DEPOSIT_DATETIME)));
        expectedSystemMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSITOR_ID, STRING, DEPOSITOR_ID));



        //----- Set up collaborating objects: cmf
        underTest = new CollectionMetadataFinder(moBuilder, avBuilder);

    }

    /**
     * Insures that the CollectionMetadataFinder finds the proper metadata on the Collection configured in the
     * {@link #setUp()} method.  Expect to have SystemMetadataInstance and CoreMetadataInstance with the proper
     * attributes and values.
     *
     * @throws MetadataFindingException
     * @throws MalformedURLException
     */
    @Test
    public void testFindAllMetadataInCollection() throws MetadataFindingException, IOException {
        java.util.Collection<MetadataInstance> metadataInstances = underTest.findMetadata(collection);
        assertEquals(2, metadataInstances.size());

        int numberOfCoreMetadataInstanceFound = 0;
        int numberOfSystemMetadataInstanceFound = 0;

        for (MetadataInstance metadataInstance : metadataInstances) {
            if (metadataInstance instanceof AttributeSetMetadataInstance) {
                AttributeSet attributeSet;
                attributeSet = moBuilder.buildAttributeSet(metadataInstance.getContent());

                if (attributeSet.getName().equals(MetadataAttributeSetName.COLLECTION_CORE_METADATA)) {
                    numberOfCoreMetadataInstanceFound++;
                    assertEquals(expectedCoreMd, attributeSet);
                }

                if (attributeSet.getName().equals(MetadataAttributeSetName.SYSTEM_METADATA)) {
                    numberOfSystemMetadataInstanceFound++;
                    assertEquals(expectedSystemMd, attributeSet);
                }
            }
        }

        assertEquals(1, numberOfCoreMetadataInstanceFound);
        assertEquals(1, numberOfSystemMetadataInstanceFound);
    }

    /**
     * Insures that attempting to find metadata on an object that isn't of type org.dataconservancy.ui.model.Collection
     * will fail with a MetadataFindingException, with an IAE as a cause.
     *
     * @throws Exception
     */
    @Test
    public void testFindMetadataInFooObject() throws Exception {
        final CollectionMetadataFinder underTest = new CollectionMetadataFinder(newXstreamModelBuilder(), newXstreamAttributeValueBuilder());

        Throwable caught = null;
        Throwable cause = null;

        try {
            underTest.findMetadata(new Object());
        } catch (Exception e) {
            caught = e;
            cause = e.getCause();
        }

        assertNotNull(caught);
        assertNotNull(cause);
        assertEquals(MetadataFindingException.class, caught.getClass());
        assertEquals(IllegalArgumentException.class, cause.getClass());
    }

    /**
     * Insures that attempting to find metadata on a collection with no state doesn't break the finder.
     *
     * @throws Exception
     */
    @Test
    public void testFindMetadataInEmptyCollectionObject() throws Exception {
        final Collection collection = new Collection();
        final CollectionMetadataFinder underTest = new CollectionMetadataFinder(newXstreamModelBuilder(), newXstreamAttributeValueBuilder());

        final java.util.Collection<MetadataInstance> instances = underTest.findMetadata(collection);

        assertNotNull(instances);
        assertEquals(2, instances.size());
        for (MetadataInstance instance : instances) {
            assertEquals(MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, instance.getFormatId());
            assertNotNull(instance.getContent());
            assertTrue(instance.getContent().read() > -1);
        }
    }

    /**
     * Insures that at least one metadata instance is returned by the finder, that their format IDs are set properly,
     * and that at least each instance's inputstream contains one byte.
     *
     * @throws Exception
     */
    @Test
    public void testFindMetadataInCollection() throws Exception {
        final Collection collection = new Collection();
        collection.setDepositorId(DEPOSITOR_ID);
        List<String> altIds = new ArrayList<String>();
        collection.setAlternateIds(altIds);
        altIds.add("altId1");
        altIds.add("altId2");
        collection.setCitableLocator(CITABLE_LOCATOR);
        collection.setPublicationDate(DateTime.parse("2020-01-01"));
        collection.setSummary("A collection summary.");
        collection.setTitle(TITLE);
        collection.setId("collectionId");

        final CollectionMetadataFinder underTest = new CollectionMetadataFinder(moBuilder, avBuilder);

        final java.util.Collection<MetadataInstance> instances = underTest.findMetadata(collection);

        assertNotNull(instances);
        assertTrue(instances.size() > 0);

        for (MetadataInstance instance : instances) {
            assertNotNull(instance.getContent());
            assertTrue(instance.getContent().read() > -1);
            assertEquals(MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, instance.getFormatId());
        }
    }


}
