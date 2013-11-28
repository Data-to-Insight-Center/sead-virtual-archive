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
package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.FAILED;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.PENDING;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.COLLECTION;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.DATASET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ArchiveDepositInfoDAOJdbcImplTest extends BaseDaoTest {

    @Autowired
    @Qualifier("uiDataSource")
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate template;

    private static final String ROW_COUNT_QUERY = "SELECT count(*) FROM " +
            ArchiveDepositInfoDAOJdbcImpl.ARCHIVE_DEPOSIT_INFO_TBL;

    private static final String DELETE_ALL_QUERY = "DELETE FROM " +
            ArchiveDepositInfoDAOJdbcImpl.ARCHIVE_DEPOSIT_INFO_TBL;

    private final List<ArchiveDepositInfo> EMPTY_LIST = Collections.emptyList();
    private ArchiveDepositInfo adi2Version1;
    private ArchiveDepositInfo adi2Version2;
    private ArchiveDepositInfo adi2Version3;
    private ArchiveDepositInfo adiVersion1;
    private ArchiveDepositInfo adiVersion2;
    private ArchiveDepositInfo adiVersion3;
    private ArchiveDepositInfo adi3Version1;
    private ArchiveDepositInfo adi3Version2;
    private ArchiveDepositInfo adi3Version3;

    @Before
    public void setUp() {
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));

    }

    @After
    public void tearDown() {
        template.execute(DELETE_ALL_QUERY);
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));
    }

    /**
     * Add a Archive Deposit Info (ADI), verify it exists with lookup, and equals the added ADI.
     *
     * @throws Exception
     */

    @Test
    public void testAdd() throws Exception {
        final String depositId = "bar";
        final ArchiveDepositInfo adi = newAdi(null, depositId, PENDING, DateTime.now(), "objectId", COLLECTION);
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);

        // Verify assumptions
        assertNull("Expected the DAO to be empty.", underTest.lookup(depositId));
        underTest.add(adi);

        assertEquals(adi, underTest.lookup(depositId));
    }

    @Test
    public void testAddWithParent() throws Exception {
        final String depositId = "bar";
        final String parentDepositId = "foo";
        final ArchiveDepositInfo adi = newAdi(null, depositId, PENDING, DateTime.now(), "objectId", COLLECTION, parentDepositId);
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);

        // Verify assumptions
        assertNull("Expected the DAO to be empty.", underTest.lookup(depositId));
        underTest.add(adi);

        assertEquals(adi, underTest.lookup(depositId));
    }


    /**
     * Add a Archive Deposit Info (ADI), verify it exists with lookup, and equals the added ADI.
     * Update the adi, and verify that it equals what is in the DAO.
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        final String depositId = "bar";
        final ArchiveDepositInfo adi = newAdi(null, depositId, PENDING, DateTime.now(), "objectId", COLLECTION);
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId));

        underTest.add(adi);

        assertEquals(adi, underTest.lookup(depositId));

        adi.setArchiveId("archiveId");
        adi.setDepositStatus(DEPOSITED);

        underTest.update(adi);

        assertEquals(adi, underTest.lookup(depositId));
    }

    /**
     * Add a Archive Deposit Info (ADI), verify it exists with lookup, and equals the added ADI.
     * Update the adi, and verify that it equals what is in the DAO.
     *
     * @throws Exception
     */
    @Test
    public void testUpdateWithParent() throws Exception {
        final String depositId = "bar";
        final String parentId = "foo";
        final ArchiveDepositInfo adi = newAdi(null, depositId, PENDING, DateTime.now(), "objectId", COLLECTION, parentId);
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId));

        underTest.add(adi);

        assertEquals(adi, underTest.lookup(depositId));

        adi.setArchiveId("archiveId");
        adi.setDepositStatus(DEPOSITED);

        underTest.update(adi);

        assertEquals(adi, underTest.lookup(depositId));
    }

    /**
     * Add a Archive Deposit Info (ADI), verify it exists with lookup, and equals the added ADI.
     *
     * @throws Exception
     */
    @Test
    public void testLookup() throws Exception {
        final String depositId = "bar";
        final ArchiveDepositInfo adi = newAdi(null, depositId, PENDING, DateTime.now(), "objectId", COLLECTION);
        ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId));

        underTest.add(adi);

        assertEquals(adi, underTest.lookup(depositId));
    }

    /**
     * Add a Archive Deposit Info (ADI), verify it exists with lookup, and equals the added ADI.
     *
     * @throws Exception
     */
    @Test
    public void testLookupWithParent() throws Exception {
        final String depositId = "bar";
        final String parentDepositId = "foo";
        final ArchiveDepositInfo adi = newAdi(null, depositId, PENDING, DateTime.now(), "objectId", COLLECTION, parentDepositId);
        ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId));

        underTest.add(adi);

        assertEquals(adi, underTest.lookup(depositId));
    }

    @Test
    public void testList() throws Exception {
        final String depositId1 = "bar";
        final String objectId1 = "foo";
        final ArchiveDepositInfo.Status status1 = PENDING;
        final ArchiveDepositInfo.Type type1 = COLLECTION;


        final ArchiveDepositInfo adiOne = newAdi(null, depositId1, status1, DateTime.now().minusSeconds(5), objectId1, type1);

        final String depositId2 = "baz";
        final String objectId2 = "bug";
        final ArchiveDepositInfo.Status status2 = PENDING;
        final ArchiveDepositInfo.Type type2 = COLLECTION;

        final ArchiveDepositInfo adiTwo = newAdi(null, depositId2, status2, DateTime.now(), objectId2, type2);
        ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId1));
        assertNull(underTest.lookup(depositId2));

        underTest.add(adiOne);
        underTest.add(adiTwo);

        assertEquals(adiOne, underTest.lookup(depositId1));
        assertEquals(adiTwo, underTest.lookup(depositId2));

        List<ArchiveDepositInfo> expected = new ArrayList<ArchiveDepositInfo>();
        expected.add(adiTwo);
        expected.add(adiOne);

        assertEquals(expected, underTest.list(COLLECTION));
        assertEquals(EMPTY_LIST, underTest.list(DATASET));

        assertEquals(expected, underTest.list(PENDING));
        assertEquals(EMPTY_LIST, underTest.list(DEPOSITED));
        assertEquals(EMPTY_LIST, underTest.list(FAILED));


        assertEquals(expected, underTest.list(COLLECTION, PENDING));
        assertEquals(EMPTY_LIST, underTest.list(COLLECTION, DEPOSITED));
        assertEquals(EMPTY_LIST, underTest.list(COLLECTION, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, DEPOSITED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, PENDING));

        assertEquals(expected, underTest.list(COLLECTION, null));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, null));


        assertEquals(expected, underTest.list(null, PENDING));
        assertEquals(EMPTY_LIST, underTest.list(null, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(null, DEPOSITED));

        assertEquals(expected, underTest.list(null, null));
    }

    @Test
    public void testListDifferentTypes() throws Exception {
        final String depositId1 = "bar";
        final String objectId1 = "foo";
        final ArchiveDepositInfo.Status status1 = PENDING;
        final ArchiveDepositInfo.Type type1 = COLLECTION;

        final ArchiveDepositInfo adiOne = newAdi(null, depositId1, status1, DateTime.now(), objectId1, type1);

        final String depositId2 = "baz";
        final String objectId2 = "bug";
        final ArchiveDepositInfo.Status status2 = PENDING;
        final ArchiveDepositInfo.Type type2 = DATASET;

        final ArchiveDepositInfo adiTwo = newAdi(null, depositId2, status2, DateTime.now(), objectId2, type2);
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId1));
        assertNull(underTest.lookup(depositId2));

        underTest.add(adiOne);
        underTest.add(adiTwo);

        assertEquals(adiOne, underTest.lookup(depositId1));
        assertEquals(adiTwo, underTest.lookup(depositId2));

        List<ArchiveDepositInfo> expectedCollection = new ArrayList<ArchiveDepositInfo>();
        expectedCollection.add(adiOne);
        List<ArchiveDepositInfo> expectedDataset = new ArrayList<ArchiveDepositInfo>();
        expectedDataset.add(adiTwo);
        List<ArchiveDepositInfo> expectedAll = new ArrayList<ArchiveDepositInfo>();
        expectedAll.add(adiTwo);
        expectedAll.add(adiOne);

        assertEquals(expectedCollection, underTest.list(COLLECTION));
        assertEquals(expectedDataset, underTest.list(DATASET));

        assertEquals(expectedAll, underTest.list(PENDING));
        assertEquals(EMPTY_LIST, underTest.list(FAILED));
        assertEquals(EMPTY_LIST, underTest.list(DEPOSITED));

        assertEquals(expectedCollection, underTest.list(COLLECTION, PENDING));
        assertEquals(expectedDataset, underTest.list(DATASET, PENDING));

        assertEquals(EMPTY_LIST, underTest.list(COLLECTION, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(COLLECTION, DEPOSITED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, DEPOSITED));

        assertEquals(expectedDataset, underTest.list(DATASET, null));
        assertEquals(expectedCollection, underTest.list(COLLECTION, null));
        assertEquals(expectedAll, underTest.list(null, null));
    }

    @Test
    public void testListDifferentTypesAndStatus() throws Exception {
        final String depositId1 = "bar";
        final String objectId1 = "foo";
        final ArchiveDepositInfo.Status status1 = PENDING;
        final ArchiveDepositInfo.Type type1 = COLLECTION;

        final ArchiveDepositInfo adiOne = newAdi(null, depositId1, status1, DateTime.now(), objectId1, type1);

        final String depositId2 = "baz";
        final String objectId2 = "bug";
        final ArchiveDepositInfo.Status status2 = FAILED;
        final ArchiveDepositInfo.Type type2 = DATASET;

        final ArchiveDepositInfo adiTwo = newAdi(null, depositId2, status2, DateTime.now(), objectId2, type2);
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        assertNull(underTest.lookup(depositId1));
        assertNull(underTest.lookup(depositId2));

        underTest.add(adiOne);
        underTest.add(adiTwo);

        assertEquals(adiOne, underTest.lookup(depositId1));
        assertEquals(adiTwo, underTest.lookup(depositId2));

        List<ArchiveDepositInfo> expectedCollection = new ArrayList<ArchiveDepositInfo>();
        expectedCollection.add(adiOne);
        List<ArchiveDepositInfo> expectedDataset = new ArrayList<ArchiveDepositInfo>();
        expectedDataset.add(adiTwo);
        List<ArchiveDepositInfo> expectedAll = new ArrayList<ArchiveDepositInfo>();
        expectedAll.addAll(expectedDataset);
        expectedAll.addAll(expectedCollection);

        assertEquals(expectedCollection, underTest.list(COLLECTION));
        assertEquals(expectedDataset, underTest.list(DATASET));

        assertEquals(expectedCollection, underTest.list(PENDING));
        assertEquals(expectedDataset, underTest.list(FAILED));
        assertEquals(EMPTY_LIST, underTest.list(DEPOSITED));

        assertEquals(expectedCollection, underTest.list(COLLECTION, PENDING));
        assertEquals(EMPTY_LIST, underTest.list(COLLECTION, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(COLLECTION, DEPOSITED));

        assertEquals(expectedDataset, underTest.list(DATASET, FAILED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, DEPOSITED));
        assertEquals(EMPTY_LIST, underTest.list(DATASET, PENDING));

        assertEquals(expectedDataset, underTest.list(DATASET, null));
        assertEquals(expectedCollection, underTest.list(COLLECTION, null));
        assertEquals(expectedAll, underTest.list(null, null));
    }

    @Test
    public void testListForObject() throws Exception {
        assertTrue(true);
    }

    private void setUpVersionTests(ArchiveDepositInfoDAOJdbcImpl underTest) throws Exception {
        final String objectId1 = "objectID";

        final String depositId1 = "depositId1";
        final String depositId2 = "depositId2";
        final String depositId3 = "depositId3";
        final ArchiveDepositInfo.Status status2 = FAILED;

        adiVersion1 = newAdi(null, depositId1, DEPOSITED, DateTime.now(), objectId1, DATASET);
        Thread.sleep(1000);
        adiVersion2 = newAdi(null, depositId2, DEPOSITED, DateTime.now(), objectId1, DATASET);
        Thread.sleep(1000);
        adiVersion3 = newAdi(null, depositId3, DEPOSITED, DateTime.now(), objectId1, DATASET);
        Thread.sleep(1000);

        final String objectId2 = "objectID2";
        final String depositId21 = "depositId21";
        final String depositId22 = "depositId22";
        final String depositId23 = "depositId23";

        adi2Version1 = newAdi(null, depositId21, DEPOSITED, DateTime.now(), objectId2, DATASET);
        Thread.sleep(1000);
        adi2Version2 = newAdi(null, depositId22, DEPOSITED, DateTime.now(), objectId2, DATASET);
        Thread.sleep(1000);
        adi2Version3 = newAdi(null, depositId23, FAILED, DateTime.now(), objectId2, DATASET);
        Thread.sleep(1000);

        final String objectId3 = "objectID3";
        final String depositId31 = "depositId31";
        final String depositId32 = "depositId32";
        final String depositId33 = "depositId33";

        adi3Version1 = newAdi(null, depositId31, DEPOSITED, DateTime.now(), objectId3, COLLECTION);
        Thread.sleep(1000);
        adi3Version2 = newAdi(null, depositId32, DEPOSITED, DateTime.now(), objectId3, COLLECTION);
        Thread.sleep(1000);
        adi3Version3 = newAdi(null, depositId33, FAILED, DateTime.now(), objectId3, COLLECTION);
        Thread.sleep(1000);
        assertNull(underTest.lookup(depositId1));
        assertNull(underTest.lookup(depositId2));
        assertNull(underTest.lookup(depositId3));
        assertNull(underTest.lookup(depositId21));
        assertNull(underTest.lookup(depositId22));
        assertNull(underTest.lookup(depositId23));

        underTest.add(adiVersion1);
        underTest.add(adiVersion2);
        underTest.add(adiVersion3);
        underTest.add(adi2Version1);
        underTest.add(adi2Version2);
        underTest.add(adi2Version3);
        underTest.add(adi3Version1);
        underTest.add(adi3Version2);
        underTest.add(adi3Version3);
    }

    @Test
    public void testListByStatusWithMultipleVersions() throws Exception {
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);
        setUpVersionTests(underTest);

        List<ArchiveDepositInfo> returnedList = underTest.list(DEPOSITED);
        assertEquals(7, returnedList.size());
        assertTrue(returnedList.contains(adiVersion3));
        assertTrue(returnedList.contains(adi2Version2));
        assertTrue(returnedList.contains(adi3Version2));

        returnedList = underTest.list(FAILED);
        assertEquals(2, returnedList.size());
        assertTrue(returnedList.contains(adi3Version3));
        assertTrue(returnedList.contains(adi2Version3));
    }

    @Test
    public void testListByTypeWithMultipleVersions() throws Exception {
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);

        setUpVersionTests(underTest);
        //Test list by type
        List<ArchiveDepositInfo> returnedList = underTest.list(DATASET);
        assertEquals(6, returnedList.size());
        assertTrue(returnedList.contains(adi2Version3));
        assertTrue(returnedList.contains(adiVersion3));

        returnedList = underTest.list(COLLECTION);
        assertEquals(3, returnedList.size());
        assertEquals(adi3Version3, returnedList.get(0));
    }

    @Test
    public void testListByTypeAndStatusWithMultipleVersion() throws Exception {
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);

        setUpVersionTests(underTest);
        List<ArchiveDepositInfo> returnedList = underTest.list(null, null);
        //List<ArchiveDepositInfo> returnedList = underTest.list(COLLECTION, DEPOSITED);
        assertEquals(9, returnedList.size());
        assertTrue(returnedList.contains(adiVersion3));
        assertTrue(returnedList.contains(adi2Version3));
        assertTrue(returnedList.contains(adi3Version3));

        returnedList = underTest.list(COLLECTION, DEPOSITED);
        assertEquals(2, returnedList.size());
        assertTrue(returnedList.contains(adi3Version2));

        returnedList = underTest.list(DATASET, FAILED);
        assertEquals(1, returnedList.size());
        assertTrue(returnedList.contains(adi2Version3));
    }

    @Test
    public void testListByObjectIdAndStatusWithMultipleVersions() throws Exception {
        final ArchiveDepositInfoDAOJdbcImpl underTest = new ArchiveDepositInfoDAOJdbcImpl(dataSource);

        setUpVersionTests(underTest);
        List<ArchiveDepositInfo> returnedList = underTest.listForObject(adi2Version1.getObjectId(), null);
        assertEquals(3, returnedList.size());
        assertEquals(adi2Version3, returnedList.get(0) );
        assertEquals(adi2Version2, returnedList.get(1) );
        assertEquals(adi2Version1, returnedList.get(2) );

        returnedList = underTest.listForObject(adi2Version1.getObjectId(), DEPOSITED);
        assertEquals(2, returnedList.size());
        assertEquals(adi2Version2, returnedList.get(0) );
        assertEquals(adi2Version1, returnedList.get(1) );

        returnedList = underTest.listForObject(adi2Version1.getObjectId(), FAILED);
        assertEquals(1, returnedList.size());
        assertEquals(adi2Version3, returnedList.get(0) );
    }


    /**
     * Creates a new ArchiveDepositInfo instance, using the supplied parameters.  If a parameter is {@code null}, it
     * isn't set on the ArchiveDepositInfo.
     *
     * @param aId the archive id
     * @param dId the deposit (archival transaction) id
     * @param s   the status
     * @param dt  the date time of deposit
     * @param oId the object (business) id
     * @param t   the type
     * @return the newly created ArchiveDepositInfo
     */
    private ArchiveDepositInfo newAdi(String aId, String dId, ArchiveDepositInfo.Status s, DateTime dt, String oId,
                                      ArchiveDepositInfo.Type t) {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();

        if (aId != null && aId.trim().length() != 0) {
            adi.setArchiveId(aId);
        }

        if (dId != null && dId.trim().length() != 0) {
            adi.setDepositId(dId);
        }

        if (s != null) {
            adi.setDepositStatus(s);
        }

        if (dt != null) {
            adi.setDepositDateTime(dt);
        }

        if (oId != null && oId.trim().length() != 0) {
            adi.setObjectId(oId);
        }

        if (t != null) {
            adi.setObjectType(t);
        }

        return adi;
    }

    /**
     * Creates a new ArchiveDepositInfo instance, using the supplied parameters.  If a parameter is {@code null}, it
     * isn't set on the ArchiveDepositInfo.
     *
     * @param aId the archive id
     * @param dId the deposit (archival transaction) id
     * @param s   the status
     * @param dt  the date time of deposit
     * @param oId the object (business) id
     * @param t   the type
     * @param parentDepositId the parent deposit id
     * @return the newly created ArchiveDepositInfo
     */
    private ArchiveDepositInfo newAdi(String aId, String dId, ArchiveDepositInfo.Status s, DateTime dt, String oId,
                                      ArchiveDepositInfo.Type t, String parentDepositId) {
        ArchiveDepositInfo adi = newAdi(aId, dId, s, dt, oId, t);
        adi.setParentDepositId(parentDepositId);
        return adi;
    }

}

