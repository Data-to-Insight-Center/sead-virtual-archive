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
package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

import static org.dataconservancy.ui.dao.MetadataFormatPropertiesDaoJdbcImpl.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests to insure that the MetadataFormatPropertiesDaoJdbcImpl persists the MetadataFormatProperties object properly.
 * <p/>
 * Please see the {@link MetadataFormatPropertiesDaoJdbcImpl} Javadoc, because the DAO is not responsible for persisting
 * all of the information contained in a MetadataFormatProperties instance.
 */
public class MetadataFormatPropertiesDaoJdbcImplTest extends BaseDaoTest {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate template;

    private MetadataFormatPropertiesDaoJdbcImpl mdfDao;

    private final String DELETE_ALL_QUERY = "DELETE FROM " + METADATA_PROPERTIES_TABLE;

    private final String ROW_COUNT_QUERY = "SELECT count(*) FROM " + METADATA_PROPERTIES_TABLE;

    @Before
    public void setUp() {
        template.execute(DELETE_ALL_QUERY);
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));
        mdfDao = new MetadataFormatPropertiesDaoJdbcImpl(template);
    }

    @After
    public void tearDown() {
        template.execute(DELETE_ALL_QUERY);
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));
    }

    /**
     * Insures that the initial state of the properties object, and the flags of that object, are false, and 0x00,
     * respectively.  The rest of the unit tests don't have to test this assumption.
     *
     * @throws Exception
     */
    @Test
    public void testInitialStateOfFlags() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        assertFalse(props.isAppliesToCollection());
        assertFalse(props.isAppliesToProject());
        assertFalse(props.isAppliesToItem());
        assertFalse(props.isValidates());
        assertTrue(props.isActive()); // this isn't persisted as a byte flag, but we test it here to
                                      // verify the state of the object

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertEquals(0x00, flags & APPLIES_TO_COLLECTION_FLAG);
        assertEquals(0x00, flags & APPLIES_TO_ITEM_FLAG);
        assertEquals(0x00, flags & APPLIES_TO_PROJECT_FLAG);
        assertEquals(0x00, flags & VALIDATES_FLAG);
        assertEquals(0x00, flags);
    }

    /**
     * Insures that when the properties apply to the collection, the proper flag is set.
     *
     * @throws Exception
     */
    @Test
    public void testModifyCollectionFlag() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setAppliesToCollection(true);

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertTrue(props.isAppliesToCollection());
        assertEquals(APPLIES_TO_COLLECTION_FLAG, flags);
    }

    /**
     * Insures that when the properties apply to the project, the proper flag is set.
     *
     * @throws Exception
     */
    @Test
    public void testModifyProjectFlag() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setAppliesToProject(true);

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertTrue(props.isAppliesToProject());
        assertEquals(APPLIES_TO_PROJECT_FLAG, flags);
    }

    /**
     * Insures that when the properties apply to the item, the proper flag is set.
     *
     * @throws Exception
     */
    @Test
    public void testModifyItemFlag() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setAppliesToItem(true);

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertTrue(props.isAppliesToItem());
        assertEquals(APPLIES_TO_ITEM_FLAG, flags);
    }

    /**
     * Insures that when the properties isValidates is true, the proper flag is set.
     *
     * @throws Exception
     */
    @Test
    public void testModifyValidatesFlag() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setValidates(true);

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertTrue(props.isValidates());
        assertEquals(VALIDATES_FLAG, flags);
    }

    /**
     * Tests that when multiple properties are set on the properties object, the flags are set correctly.
     *
     * @throws Exception
     */
    @Test
    public void testModifyMultipleFlags() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setValidates(true);
        props.setAppliesToProject(true);

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertEquals(VALIDATES_FLAG, flags & VALIDATES_FLAG);
        assertEquals(APPLIES_TO_PROJECT_FLAG, flags & APPLIES_TO_PROJECT_FLAG);

        // another way of doing the bit math
        assertEquals(flags, VALIDATES_FLAG | APPLIES_TO_PROJECT_FLAG);

        // another way to make sure other flags aren't set
        assertEquals(0x00, APPLIES_TO_COLLECTION_FLAG & flags);
        assertEquals(0x00, APPLIES_TO_ITEM_FLAG & flags);
    }

    /**
     * Tests that when all of the properties are set, the flags are set as expected.
     *
     * @throws Exception
     */
    @Test
    public void testModifyAllFlags() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setValidates(true);
        props.setAppliesToProject(true);
        props.setAppliesToItem(true);
        props.setAppliesToCollection(true);

        byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        assertTrue(props.isAppliesToCollection());
        assertTrue(props.isAppliesToItem());
        assertTrue(props.isAppliesToProject());
        assertTrue(props.isValidates());
        assertEquals(VALIDATES_FLAG, flags & VALIDATES_FLAG);
        assertEquals(APPLIES_TO_PROJECT_FLAG, flags & APPLIES_TO_PROJECT_FLAG);
        assertEquals(APPLIES_TO_ITEM_FLAG, flags & APPLIES_TO_ITEM_FLAG);
        assertEquals(APPLIES_TO_COLLECTION_FLAG, flags & APPLIES_TO_COLLECTION_FLAG);

        // another way of doing the bit math
        assertEquals(flags, VALIDATES_FLAG | APPLIES_TO_PROJECT_FLAG | APPLIES_TO_COLLECTION_FLAG |
                APPLIES_TO_ITEM_FLAG);

        // another way to make sure all flags are set
        assertEquals(0x0f, flags); // 15
    }

    /**
     * Test marking a record as deleted or deactivated.
     * @throws Exception
     */
    @Test
    public void testDeactivateRecord() throws Exception {
        MetadataFormatProperties props = new MetadataFormatProperties();
        props.setFormatId("MFPTest:id");
        props.setValidates(true);
        props.setAppliesToProject(true);
        props.setAppliesToItem(true);
        props.setAppliesToCollection(true);
        props.setActive(true);

        mdfDao.add(props);

        MetadataFormatProperties retrievedMFP = mdfDao.get(props.getFormatId());
        assertTrue(retrievedMFP.isActive());

        //deactivate a record
        props.setActive(false);
        mdfDao.update(props);

        retrievedMFP = mdfDao.get(props.getFormatId());
        assertFalse(retrievedMFP.isActive());
    }

    /**
     * Tests that when setting no flags on a new properties object, the properties object state is unchanged.
     *
     * @throws Exception
     */
    @Test
    public void testSetNoFlags() throws Exception {
        // We know the initial state of this object from testInitialStateOfFlags(); all properties are false.
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = 0x00;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertFalse(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertFalse(props.isValidates());
        assertFalse(props.isAppliesToCollection());
    }

    /**
     * Tests that when setting no flags on a properties object with state, the properties object state is reset to
     * initial state.
     *
     * @throws Exception
     */
    @Test
    public void testResetFlags() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        props.setAppliesToItem(true);
        props.setAppliesToProject(true);
        props.setAppliesToCollection(true);
        props.setValidates(true);

        final byte flags = 0x00;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertFalse(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertFalse(props.isValidates());
        assertFalse(props.isAppliesToCollection());

        final MetadataFormatProperties newProps = new MetadataFormatProperties();
        assertEquals(newProps, props);
    }

    /**
     * When the appliesToCollection flag is set, the proper property value is set.
     *
     * @throws Exception
     */
    @Test
    public void testSetAppliesToCollectionFlag() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = APPLIES_TO_COLLECTION_FLAG;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertFalse(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertFalse(props.isValidates());
        assertTrue(props.isAppliesToCollection());
    }

    /**
     * When the appliesToItem flag is set, the proper property value is set.
     *
     * @throws Exception
     */
    @Test
    public void testSetAppliesToItemFlag() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = APPLIES_TO_ITEM_FLAG;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertTrue(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertFalse(props.isValidates());
        assertFalse(props.isAppliesToCollection());
    }

    /**
     * When the appliesToProject flag is set, the proper property value is set.
     *
     * @throws Exception
     */
    @Test
    public void testSetAppliesToProjectFlag() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = APPLIES_TO_PROJECT_FLAG;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertFalse(props.isAppliesToItem());
        assertTrue(props.isAppliesToProject());
        assertFalse(props.isValidates());
        assertFalse(props.isAppliesToCollection());
    }

    /**
     * When the validates flag is set, the proper property value is set.
     *
     * @throws Exception
     */
    @Test
    public void testSetValidatesFlag() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = VALIDATES_FLAG;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertFalse(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertTrue(props.isValidates());
        assertFalse(props.isAppliesToCollection());
    }

    /**
     * Shows that properties will get set to false on the properties object, even if they were
     * true going in.
     *
     * @throws Exception
     */
    @Test
    public void testSetFlagsOnPropertiesObjectWithState() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();

        // Set some flags on props.
        props.setAppliesToItem(true);
        props.setAppliesToProject(true);

        // But only set the VALIDATES_FLAG when calling the flags method.
        final byte flags = VALIDATES_FLAG;
        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        // Only the VALIDATES_FLAG should be true, the other should be set to false.
        assertFalse(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertTrue(props.isValidates());
        assertFalse(props.isAppliesToCollection());
    }

    /**
     * Shows that setting multiple properties with multiple flags works.
     *
     * @throws Exception
     */
    @Test
    public void testSetMultipleFlags() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = VALIDATES_FLAG | APPLIES_TO_ITEM_FLAG;
        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertTrue(props.isAppliesToItem());
        assertFalse(props.isAppliesToProject());
        assertTrue(props.isValidates());
        assertFalse(props.isAppliesToCollection());
    }

    /**
     * Shows that setting all properties by setting all flags works.
     *
     * @throws Exception
     */
    @Test
    public void testSetAllFlags() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = VALIDATES_FLAG | APPLIES_TO_ITEM_FLAG | APPLIES_TO_COLLECTION_FLAG | APPLIES_TO_PROJECT_FLAG;
        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);

        assertTrue(props.isAppliesToItem());
        assertTrue(props.isAppliesToProject());
        assertTrue(props.isValidates());
        assertTrue(props.isAppliesToCollection());
    }

    @Test
    public void testSetFlagsRoundTrip() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final byte flags = VALIDATES_FLAG | APPLIES_TO_ITEM_FLAG;

        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props);
        assertEquals(flags, MetadataFormatPropertiesDaoJdbcImpl.flags(props));
    }

    @Test
    public void testGetFlagsRoundTrip() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();

        final byte flags = MetadataFormatPropertiesDaoJdbcImpl.flags(props);
        final MetadataFormatProperties props2 = new MetadataFormatProperties();
        MetadataFormatPropertiesDaoJdbcImpl.flags(flags, props2);
        assertEquals(props, props2);
    }

    @Test
    public void testAddAndGetProperties() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final String id = "fooId";
        props.setFormatId(id);
        mdfDao.add(props);
        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));
        assertEquals(props, mdfDao.get(id));
    }

    @Test
    public void testListProperties() throws Exception {
        final MetadataFormatProperties one = new MetadataFormatProperties();
        final MetadataFormatProperties two = new MetadataFormatProperties();
        one.setFormatId("1");
        two.setFormatId("2");
        one.setActive(true);
        two.setActive(true);
        mdfDao.add(one);
        mdfDao.add(two);

        assertEquals(2, template.queryForInt(ROW_COUNT_QUERY));
        assertEquals(Arrays.asList(one, two), mdfDao.list());
    }

    @Test
    public void testListOnlyActiveProperties() throws Exception {
        final MetadataFormatProperties one = new MetadataFormatProperties();
        final MetadataFormatProperties two = new MetadataFormatProperties();
        final MetadataFormatProperties three = new MetadataFormatProperties();
        one.setFormatId("1");
        two.setFormatId("2");
        three.setFormatId("3");
        one.setActive(true);
        two.setActive(true);
        three.setActive(false);
        mdfDao.add(one);
        mdfDao.add(two);
        mdfDao.add(three);

        assertEquals(3, template.queryForInt(ROW_COUNT_QUERY));
        assertEquals(Arrays.asList(one, two), mdfDao.list(true));
    }

    @Test
    public void testAddAndGetPreservesFlags() throws Exception {
        final MetadataFormatProperties props = new MetadataFormatProperties();
        props.setFormatId("foo");
        props.setValidates(true);
        props.setAppliesToItem(true);


        mdfDao.add(props);

        assertEquals(props, mdfDao.get("foo"));
    }
}
