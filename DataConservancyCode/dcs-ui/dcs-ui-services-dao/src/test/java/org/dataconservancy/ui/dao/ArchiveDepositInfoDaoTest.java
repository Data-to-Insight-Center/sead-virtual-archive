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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Type;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class ArchiveDepositInfoDaoTest extends BaseDaoTest {

	@Autowired
	private ArchiveDepositInfoDAO archiveDepositInfoDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static final String ARCHIVE_DEPOSIT_INFO_TBL = "ARCHIVE_DEPOSIT_INFO";
	private ArchiveDepositInfo info;

	@Before
	public void setUp() {
		String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL;

		List<ArchiveDepositInfo> infoList = (List<ArchiveDepositInfo>) jdbcTemplate
				.query(query, new ArchiveDepositInfoRowMapper());
		assertEquals(0, infoList.size());
		
		info = new ArchiveDepositInfo();
		info.setArchiveId("archiveID");
        info.setStateId("stateID");
		info.setDepositId("depositID");
		info.setObjectId("objectID");
		info.setDepositStatus(Status.FAILED);
		info.setObjectType(Type.COLLECTION);
		info.setDepositDateTime(DateTime.now());
		
		query = " INSERT INTO " + ARCHIVE_DEPOSIT_INFO_TBL
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		int insertedRows = jdbcTemplate.update(
				query,
				new Object[] { info.getObjectId(), info.getArchiveId(),
                        info.getStateId(),
						info.getDepositId(),
                        info.getParentDepositId(),
						info.getDepositStatus().toString(),
						info.getObjectType().toString(), info.getDepositDateTime().toDate()});
		assertEquals(1, insertedRows);
	}
	
    @After
    public void cleanUp() {
        String query = "DELETE FROM " + ARCHIVE_DEPOSIT_INFO_TBL
                + " WHERE DEPOSIT_ID = ?";
        int removedRows = jdbcTemplate.update(query,
                new Object[] { info.getDepositId() });
        assertEquals(1, removedRows);

        query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL;
        List<ArchiveDepositInfo> infoList = (List<ArchiveDepositInfo>) jdbcTemplate
                .query(query, new ArchiveDepositInfoRowMapper());
        assertEquals(0, infoList.size());
    }

	@Test
	public void testAdd() {
	    final ArchiveDepositInfo testAddInfo = new ArchiveDepositInfo();
		testAddInfo.setArchiveId("archiveIDtwo");
		testAddInfo.setDepositId("depositIDtwo");
		testAddInfo.setObjectId("objectIDtwo");
        testAddInfo.setStateId("stateIDtwo");
		testAddInfo.setDepositStatus(Status.FAILED);
		testAddInfo.setObjectType(Type.COLLECTION);
        testAddInfo.setDepositDateTime(DateTime.now());
		archiveDepositInfoDao.add(testAddInfo);
		String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL
				+ " WHERE DEPOSIT_ID = ?";
		jdbcTemplate.query(query, new Object[] { testAddInfo.getDepositId() },
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						assertEquals(testAddInfo.getArchiveId(),
								rs.getString("ARCHIVE_ID"));
						assertEquals(testAddInfo.getDepositId(),
								rs.getString("DEPOSIT_ID"));
                        assertEquals(testAddInfo.getStateId(),
                                rs.getString("STATE_ID"));
						assertEquals(testAddInfo.getDepositStatus(),
								Status.valueOf(rs.getString("DEPOSIT_STATUS")));
						assertEquals(testAddInfo.getObjectType(),
								Type.valueOf(rs.getString("OBJECT_TYPE")));
						assertEquals(testAddInfo.getObjectId(),
								rs.getString("OBJECT_ID"));
						assertEquals(testAddInfo.getDepositDateTime(), new DateTime(rs.getTimestamp("DEPOSIT_DATE")));
					}
				});

		query = "DELETE FROM " + ARCHIVE_DEPOSIT_INFO_TBL
				+ " WHERE DEPOSIT_ID = ?";
		int removedRows = jdbcTemplate.update(query,
				new Object[] { testAddInfo.getDepositId() });
		assertEquals(1, removedRows);
	}

    @Test
	public void testLookUp() {
		ArchiveDepositInfo lookedUpItem = archiveDepositInfoDao.lookup(info
				.getDepositId());
		assertEquals(info, lookedUpItem);
	}

	@Test
	public void testUpdate() {
		info.setArchiveId("UpdatedArchiveID");
		info.setObjectId("UpdatedObjectID");
        info.setStateId("UpdatedStateID");
        info.setDepositDateTime(DateTime.now());
		archiveDepositInfoDao.update(info);
		String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL
				+ " WHERE DEPOSIT_ID = ?";
		jdbcTemplate.query(query, new Object[] { info.getDepositId() },
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						assertEquals(info.getArchiveId(),
								rs.getString("ARCHIVE_ID"));
                        assertEquals(info.getStateId(),
                                rs.getString("STATE_ID"));
						assertEquals(info.getDepositId(),
								rs.getString("DEPOSIT_ID"));
						assertEquals(info.getDepositStatus(),
								Status.valueOf(rs.getString("DEPOSIT_STATUS")));
						assertEquals(info.getObjectType(),
								Type.valueOf(rs.getString("OBJECT_TYPE")));
						assertEquals(info.getObjectId(),
								rs.getString("OBJECT_ID"));
                        assertEquals(info.getDepositDateTime(),
                                new DateTime(rs.getTimestamp("DEPOSIT_DATE")));
					}
				});
	}

	@Test
	public void testList() {
	    final ArchiveDepositInfo testAddInfo = new ArchiveDepositInfo();
		testAddInfo.setArchiveId("archiveIDtwo");
		testAddInfo.setDepositId("depositIDtwo");
		testAddInfo.setObjectId("objectIDtwo");
		testAddInfo.setDepositStatus(Status.FAILED);
		testAddInfo.setObjectType(Type.COLLECTION);
        testAddInfo.setDepositDateTime(DateTime.now());
		String query = " INSERT INTO " + ARCHIVE_DEPOSIT_INFO_TBL
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ? )";
		int insertedRows = jdbcTemplate.update(query, new Object[] {
				testAddInfo.getObjectId(), testAddInfo.getArchiveId(),
                testAddInfo.getStateId(),
				testAddInfo.getDepositId(),
                testAddInfo.getParentDepositId(),
				testAddInfo.getDepositStatus().toString(),
				testAddInfo.getObjectType().toString(),
                testAddInfo.getDepositDateTime().toDate()});
		assertEquals(1, insertedRows);

		List<ArchiveDepositInfo> infoList = archiveDepositInfoDao.list(
				Type.COLLECTION, Status.FAILED);

		assertEquals(2, infoList.size());
		assertTrue(infoList.contains(info));
		assertTrue(infoList.contains(testAddInfo));

		query = "DELETE FROM " + ARCHIVE_DEPOSIT_INFO_TBL
				+ " WHERE DEPOSIT_ID = ?";
		int removedRows = jdbcTemplate.update(query,
				new Object[] { testAddInfo.getDepositId() });
		assertEquals(1, removedRows);

	}	
	
	@Test
	public void testListWithStatus()
	{
		List<ArchiveDepositInfo> infoList = archiveDepositInfoDao.list(null, Status.FAILED);
		assertEquals(1, infoList.size());
		assertEquals(info, infoList.get(0));
	}
	
	@Test
	public void testListWithType()
	{
		List<ArchiveDepositInfo> infoList = archiveDepositInfoDao.list(Type.COLLECTION, null);
		assertEquals(1, infoList.size());
		assertEquals(info, infoList.get(0));
	}
	
    @Test
    public void testListForObject() {
        final ArchiveDepositInfo testAddInfo = new ArchiveDepositInfo();
        testAddInfo.setArchiveId("archiveIDtwo");
        testAddInfo.setDepositId("depositIDtwo");
        testAddInfo.setObjectId("objectIDtwo");
        testAddInfo.setDepositStatus(Status.PENDING);
        testAddInfo.setObjectType(Type.COLLECTION);
        testAddInfo.setDepositDateTime(DateTime.now());

        String query = " INSERT INTO " + ARCHIVE_DEPOSIT_INFO_TBL
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int insertedRows = jdbcTemplate.update(query, new Object[] {
                testAddInfo.getObjectId(), testAddInfo.getArchiveId(),
                testAddInfo.getStateId(),
                testAddInfo.getDepositId(),
                testAddInfo.getParentDepositId(),
                testAddInfo.getDepositStatus().toString(),
                testAddInfo.getObjectType().toString(),
                testAddInfo.getDepositDateTime().toDate()});
        assertEquals(1, insertedRows);

        List<ArchiveDepositInfo> infoList = archiveDepositInfoDao
                .listForObject("objectIDtwo", null);

        assertEquals(1, infoList.size());
        assertTrue(infoList.contains(testAddInfo));
        
        infoList = archiveDepositInfoDao
                .listForObject("objectIDtwo", Status.PENDING);

        assertEquals(1, infoList.size());
        assertTrue(infoList.contains(testAddInfo));

        query = "DELETE FROM " + ARCHIVE_DEPOSIT_INFO_TBL
                + " WHERE DEPOSIT_ID = ?";
        int removedRows = jdbcTemplate.update(query,
                new Object[] { testAddInfo.getDepositId() });
        assertEquals(1, removedRows);

    }
        
	@Test
	public void testNotFoundLookUp()
	{
		assertNull(archiveDepositInfoDao.lookup("123123"));
	}
	
	private class ArchiveDepositInfoRowMapper implements
			RowMapper<ArchiveDepositInfo> {
		public ArchiveDepositInfo mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			ArchiveDepositInfo info = new ArchiveDepositInfo();
			info.setArchiveId(rs.getString("ARCHIVE_ID"));
			info.setDepositId(rs.getString("DEPOSIT_ID"));
            info.setStateId(rs.getString("STATE_ID"));
			info.setObjectId(rs.getString("OBJECT_ID"));
			info.setDepositStatus(Status.valueOf(rs.getString("DEPOSIT_STATUS")
					.toUpperCase()));
			info.setObjectType(Type.valueOf(rs.getString("OBJECT_TYPE")
					.toUpperCase()));
			return info;

		}
	}
}
