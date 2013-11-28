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
package org.dataconservancy.ui.services;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.model.Id;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the interactions of the User Interface with the UI Identifier Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/test-applicationContext.xml" })
public class UiIdentifierServiceTests {

    /**
     * The class under test
     */
    @Autowired
    private IdService underTest;

    /**
     * Contains the identifiers used for testing.
     */
    private Set<Identifier> testIdentifiers = new HashSet<Identifier>();

    /**
     * Collection ID used for testing.
     */
    private Identifier testCollectionId;

    /**
     * DataSet ID used for testing.
     */
    private Identifier testDataSetId;

    /**
     * Package ID used for testing.
     */
    private Identifier testPackageId;

    /**
     * Person ID used for testing.
     */
    private Identifier testPersonId;

    /**
     * DataFile ID used for testing.
     */
    private Identifier testDataFileId;

    /**
     * Static string that might appear in a String form of certain identifier types.
     */
    private static final String ENTITY = "entity";

    /**
     * Static string that might appear in a String form of certain identifier types.
     */
    private static final String ITEM = "item";

    /**
     * Static string that might appear in a String form of certain identifier types.
     */
    private static final String FILE = "file";

    /**
     * Create the identifiers that we'll use in the following tests.  Add each identifier to the
     * {@link #testIdentifiers} {@code Set}.
     */
    @Before
    public void setUpIdentifiers() {
        testCollectionId = underTest.create(Types.COLLECTION.name());
        assertNotNull(testCollectionId);
        testIdentifiers.add(testCollectionId);

        testDataSetId = underTest.create(Types.DATA_SET.name());
        assertNotNull(testDataSetId);
        testIdentifiers.add(testDataSetId);

        testPackageId = underTest.create(Types.PACKAGE.name());
        assertNotNull(testPackageId);
        testIdentifiers.add(testPackageId);

        testPersonId = underTest.create(Types.PERSON.name());
        assertNotNull(testPersonId);
        testIdentifiers.add(testPersonId);

        testDataFileId = underTest.create(Types.DATA_FILE.name());
        assertNotNull(testDataFileId);
        testIdentifiers.add(testDataFileId);
    }

    /**
     * Insures that identifiers of different types all have unique uids.  This test relies on the behavior of
     * Set to only contain unique String objects.
     * 
     * @throws Exception
     */
    @Test
    public void testInsureUniqueUids() throws Exception {
        Set<String> uids = new HashSet<String>();
        for (Identifier id : testIdentifiers) {
            uids.add(id.getUid());
        }

        assertEquals(testIdentifiers.size(), uids.size());
    }

    /**
     * Insures that a Collection Identifier has been created, that the Identifier has the proper type, and that
     * the URL contains the type.
     *
     * @throws Exception
     */
    @Test
    public void testCreateCollectionId() throws Exception {
        assertEquals(Types.COLLECTION.name(), testCollectionId.getType());
        assertTrue(testCollectionId.getUrl().toString().contains(Types.COLLECTION.name().toLowerCase()));
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its uid.
     *
     * @throws Exception
     */
    @Test
    public void testFromUidCollectionId() throws Exception {
        Identifier id = underTest.fromUid(testCollectionId.getUid());
        assertEquals(testCollectionId, id);
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its url.
     *
     * @throws Exception
     */
    @Test
    public void testFromUrlCollectionId() throws Exception {
        Identifier id = underTest.fromUrl(testCollectionId.getUrl());
        assertEquals(testCollectionId, id);
    }


    /**
     * Insures that a DataSet Identifier has been created, that the Identifier has the proper type, and that
     * the URL contains the type.
     *
     * @throws Exception
     */
    @Test
    public void testCreateDataSetId() throws Exception {
        assertEquals(Types.DATA_SET.name(), testDataSetId.getType());
        assertTrue(testDataSetId.getUrl().toString().contains(ITEM));
    }

    /**
     * Insures that a DataSet Identifier can be looked up from the IdService by its uid.
     *
     * @throws Exception
     */
    @Test
    public void testFromUidDataSetId() throws Exception {
        Identifier id = underTest.fromUid(testDataSetId.getUid());
        assertEquals(testDataSetId, id);
    }

    /**
     * Insures that a DataSet Identifier can be looked up from the IdService by its url.
     *
     * @throws Exception
     */
    @Test
    public void testFromUrlDataSetId() throws Exception {
        Identifier id = underTest.fromUrl(testDataSetId.getUrl());
        assertEquals(testDataSetId, id);
    }


    /**
     * Insures that a Package Identifier has been created, that the Identifier has the proper type, and that
     * the URL contains the type.
     *
     * @throws Exception
     */
    @Test
    public void testCreatePackageId() throws Exception {
        assertEquals(Types.PACKAGE.name(), testPackageId.getType());
        assertTrue(testPackageId.getUrl().toString().contains(ENTITY));
    }

    /**
     * Insures that a Package Identifier can be looked up from the IdService by its uid.
     *
     * @throws Exception
     */
    @Test
    public void testFromUidPackageId() throws Exception {
        Identifier id = underTest.fromUid(testPackageId.getUid());
        assertEquals(testPackageId, id);
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its url.
     *
     * @throws Exception
     */
    @Test
    public void testFromUrlPackageId() throws Exception {
        Identifier id = underTest.fromUrl(testPackageId.getUrl());
        assertEquals(testPackageId, id);
    }


    /**
     * Insures that a Person Identifier has been created, that the Identifier has the proper type, and that
     * the URL contains the type.
     *
     * @throws Exception
     */
    @Test
    public void testCreatePersonId() throws Exception {
        assertEquals(Types.PERSON.name(), testPersonId.getType());
        assertTrue(testPersonId.getUrl().toString().contains(Types.PERSON.name().toLowerCase()));
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its uid.
     *
     * @throws Exception
     */
    @Test
    public void testFromUidPersonId() throws Exception {
        Identifier id = underTest.fromUid(testPersonId.getUid());
        assertEquals(testPersonId, id);
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its url.
     *
     * @throws Exception
     */
    @Test
    public void testFromUrlPersonId() throws Exception {
        Identifier id = underTest.fromUrl(testPersonId.getUrl());
        assertEquals(testPersonId, id);
    }


    /**
     * Insures that a Collection Identifier has been created, that the Identifier has the proper type, and that
     * the URL contains the type.
     *
     * @throws Exception
     */
    @Test
    public void testCreateDataFileId() throws Exception {
        assertEquals(Types.DATA_FILE.name(), testDataFileId.getType());
        assertTrue(testDataFileId.getUrl().toString().contains(FILE));
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its uid.
     *
     * @throws Exception
     */
    @Test
    public void testFromUidDataFileId() throws Exception {
        Identifier id = underTest.fromUid(testDataFileId.getUid());
        assertEquals(testDataFileId, id);
    }

    /**
     * Insures that a Collection Identifier can be looked up from the IdService by its url.
     *
     * @throws Exception
     */
    @Test
    public void testFromUrlDataFileId() throws Exception {
        Identifier id = underTest.fromUrl(testDataFileId.getUrl());
        assertEquals(testDataFileId, id);
    }
}
