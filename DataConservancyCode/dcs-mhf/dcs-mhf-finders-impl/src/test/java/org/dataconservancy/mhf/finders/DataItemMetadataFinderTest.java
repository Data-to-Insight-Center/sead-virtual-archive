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
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.DATE_TIME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;
import static org.dataconservancy.mhf.test.support.BuilderTestUtil.newXstreamModelBuilder;
import static org.junit.Assert.*;

/**
 * Insures the correct metadata is found on DataItem business objects
 */
public class DataItemMetadataFinderTest {

    private static final String BUSINESS_ID = "http://foo.bar/1234";
    private static final String DESC = "Data Item Description";
    private static final String NAME = "Data Item Name ";
    private static final String CREATOR_ID = "id:person1";
    private static final String CREATOR_GN = "Willard-boy";
    private static final String CREATOR_SN = "Sirk";
    private static final String DEPOSIT_DATE = "2013-01-29";
    private static final DateTime DEPOSIT_DATETIME = DateTime.parse(DEPOSIT_DATE);

    private MetadataObjectBuilder moBuilder;
    private DataItem dataItemWithCoreMOnly;
    private DataItem dataItemWithSystemCoreM;
    private DataItemMetadataFinder underTest;

    private final MetadataAttributeSet expectedCoreMd = new MetadataAttributeSet(MetadataAttributeSetName.DATAITEM_CORE_METADATA);
    private final MetadataAttributeSet expectedSystemMd =
            new MetadataAttributeSet(MetadataAttributeSetName.SYSTEM_METADATA);

    @Before
    public void setUp() {

        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.BUSINESS_ID, STRING, BUSINESS_ID));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, STRING, DESC));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.TITLE, STRING, NAME));

        expectedSystemMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSITOR_ID, STRING, CREATOR_ID));
        expectedSystemMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSIT_DATE, DATE_TIME, String.valueOf(DEPOSIT_DATETIME)));


        //----- Set up collaborating objects: data item with only core metadata
        dataItemWithCoreMOnly = new DataItem();
        dataItemWithCoreMOnly.setId(BUSINESS_ID);
        dataItemWithCoreMOnly.setDescription(DESC);
        dataItemWithCoreMOnly.setName(NAME);

        //----- Set up collaborating objects: Person as creator
        Person person = new Person();
        person.setId(CREATOR_ID);
        person.setLastNames(CREATOR_SN);
        person.setFirstNames(CREATOR_GN);

        //----- Set up collaborating objects: data item with core and system metadata
        dataItemWithSystemCoreM = new DataItem();
        dataItemWithSystemCoreM.setId(BUSINESS_ID);
        dataItemWithSystemCoreM.setName(NAME);
        dataItemWithSystemCoreM.setDescription(DESC);
        dataItemWithSystemCoreM.setDepositDate(DEPOSIT_DATETIME);
        dataItemWithSystemCoreM.setDepositorId(person.getId());

        //----- Set up collaborating objects: moBuilder
        moBuilder = newXstreamModelBuilder();

        //----- Set up collaborating objects dmf;
        underTest = new DataItemMetadataFinder(moBuilder);

    }

    @Test
    public void testCoreMExtractedOnly() throws MetadataFindingException, IOException {
        Collection<MetadataInstance> metadataInstances = underTest.findMetadata(dataItemWithCoreMOnly);
        assertEquals(2, metadataInstances.size());
        final Iterator<MetadataInstance> iterator = metadataInstances.iterator();
        boolean foundCore = false;
        boolean foundSystem = false;

        while (iterator.hasNext()) {
            MetadataInstance foundMetadataInstance = iterator.next();
            assertTrue(foundMetadataInstance instanceof AttributeSetMetadataInstance);
            AttributeSet attributeSet = moBuilder.buildAttributeSet(foundMetadataInstance.getContent());

            if (attributeSet.getName().equals(MetadataAttributeSetName.DATAITEM_CORE_METADATA)) {
                assertEquals(expectedCoreMd, attributeSet);
                foundCore = true;
            }

            if (attributeSet.getName().equals(MetadataAttributeSetName.SYSTEM_METADATA)) {
                foundSystem = true;
            }
        }

        assertTrue(foundCore);
        assertTrue(foundSystem);
    }

    @Test
    public void testCoreSystemMExtracted () throws MetadataFindingException, IOException {
        Collection<MetadataInstance> metadataInstances = underTest.findMetadata(dataItemWithSystemCoreM);
        assertEquals(2, metadataInstances.size());

        AttributeSet attributeSet;
        int numberOfCoreMetadataInstanceFound = 0;
        int numberOfSystemMetadataInstanceFound = 0;

        for (MetadataInstance metadataInstance : metadataInstances) {
            assertTrue(metadataInstance instanceof AttributeSetMetadataInstance);
            attributeSet = moBuilder.buildAttributeSet(metadataInstance.getContent());

            if (attributeSet.getName().equalsIgnoreCase(MetadataAttributeSetName.DATAITEM_CORE_METADATA)) {
                numberOfCoreMetadataInstanceFound++;
                assertEquals(expectedCoreMd, attributeSet);
            }

            if (attributeSet.getName().equalsIgnoreCase(MetadataAttributeSetName.SYSTEM_METADATA)) {
                numberOfSystemMetadataInstanceFound++;
                assertEquals(expectedSystemMd, attributeSet);
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
        final DataItemMetadataFinder underTest = new DataItemMetadataFinder(newXstreamModelBuilder());

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
    public void testFindMetadataInEmptyDataItemObject() throws Exception {
        final DataItem dataItem = new DataItem();
        final DataItemMetadataFinder underTest = new DataItemMetadataFinder(newXstreamModelBuilder());

        final java.util.Collection<MetadataInstance> instances = underTest.findMetadata(dataItem);

        assertNotNull(instances);
        assertEquals(2, instances.size());
        for (MetadataInstance instance : instances) {
            assertEquals(MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, instance.getFormatId());
            assertNotNull(instance.getContent());
            assertTrue(instance.getContent().read() > -1);
        }
    }
}
