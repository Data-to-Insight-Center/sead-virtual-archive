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

import org.dataconservancy.ui.test.support.BaseSpringAwareTest;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.dataconservancy.ui.model.Package;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 1/27/12
 * Time: 8:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class PackageDAOJdbcImplTest extends BaseDaoTest {


	@Autowired
	private PackageDAOJdbcImpl packageDao;

	private JdbcTemplate jdbcTemplate;
    private static final String PACKAGE_TBL = "PACKAGE";
    private static final String FILE_DATA_TABLE = "PACKAGE_FILE_DATA";
	private static final Package packageOne = new Package();
    
    @Before
	public void setUp()
	{
		jdbcTemplate = packageDao.getJdbcTemplate();
        String query = "SELECT COUNT(*) FROM " + PACKAGE_TBL;

        if (jdbcTemplate.queryForInt(query) > 0) {
            final StringBuilder msg = new StringBuilder(PACKAGE_TBL + " was not empty: \n");
            jdbcTemplate.query("SELECT * FROM " + PACKAGE_TBL, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    msg.append("ID: [").append(rs.getString(1)).append("] ");
                    msg.append("Type: [").append(rs.getString(2)).append("] ");
                    msg.append("File Name: [").append(rs.getString(3)).append("] ");
                    msg.append("\n");
                }
            });
            fail(msg.toString());
        }
		assertEquals(0, jdbcTemplate.queryForInt(query));

		packageOne.setId("PkgDAOTestpackage1");
        packageOne.setPackageType(Package.PackageType.ZIP);
		packageOne.setPackageFileName("package1.zip");


        packageOne.addFile("PkgDAOTestFileId1", "file1.data");
        packageOne.addFile("PkgDAOTestFileId2", "file2.data");
        packageOne.addFile("PkgDAOTestFileId3", "file3.data");
               
		query  = " INSERT INTO " + PACKAGE_TBL + " VALUES (?, ?, ?)";
		int insertedPackageRows = jdbcTemplate.update(query, new Object[]{packageOne.getId(),
				packageOne.getPackageType().toString(),
				packageOne.getPackageFileName()});

		assertEquals(1, insertedPackageRows);

        int insertedFileDataRows=0;
        query = " INSERT INTO " + FILE_DATA_TABLE + " VALUES (?, ?, ?)";
        for(String key : packageOne.getFileData().keySet()){
           insertedFileDataRows  += jdbcTemplate.update(query, new Object[]{ packageOne.getId(), key, packageOne.getFileData().get(key)});
        }
        assertEquals(3, insertedFileDataRows);
	}

    @Test
	public void testSelect()
	{
		Package retrievedPackage = packageDao.selectPackage(packageOne.getId());
		assertEquals(packageOne, retrievedPackage);
	}

    @Test
    @DirtiesDatabase
    public void testSelectAll(){
        Package packageTwo = new Package();
        packageTwo.setId("PkgDAOTestpackage2");
        packageTwo.setPackageType(Package.PackageType.ZIP);
        packageTwo.setPackageFileName("package2.zip");


        packageTwo.getFileData().put("PkgDAOTestFileId4", "file4.data");
        packageTwo.getFileData().put("PkgDAOTestFileId5", "file5.data");
        packageTwo.getFileData().put("PkgDAOTestFileId6", "file6.data");

		packageDao.insertPackage(packageTwo);
        List<Package> packages = packageDao.selectPackage();
        assertEquals(2, packages.size());

        String query = "SELECT COUNT(*) FROM " + PACKAGE_TBL;
		assertEquals(2, jdbcTemplate.queryForInt(query));
        query = "SELECT COUNT(*) FROM " + FILE_DATA_TABLE;
        assertEquals(6, jdbcTemplate.queryForInt(query));
    }
    
    
   	@Test
	@DirtiesDatabase
	public void testInsert()
	{
        Package packageTwo = new Package();
        packageTwo.setId("kgDAOTestpackage3");
        packageTwo.setPackageType(Package.PackageType.ZIP);
        packageTwo.setPackageFileName("package3.zip");


        packageTwo.getFileData().put("PkgDAOTestFileId7", "file7.data");
        packageTwo.getFileData().put("PkgDAOTestFileId8", "file8.data");
        packageTwo.getFileData().put("PkgDAOTestFileId9", "file9.data");

		packageDao.insertPackage(packageTwo);
		Package retrievedPackage = packageDao.selectPackage(packageTwo.getId());
		assertEquals(packageTwo, retrievedPackage);
		String query = "DELETE FROM " + PACKAGE_TBL + " WHERE OBJECT_ID = ?";
		int removedRows= jdbcTemplate.update(query, new Object[] { packageTwo.getId()});
		assertEquals(1, removedRows);
	}


	@Test
	public void testUpdate()
	{
		String newPackageFileName = "package1new";
		packageOne.setPackageFileName(newPackageFileName);
		packageDao.updatePackage(packageOne);
		Package retrievedPackage = packageDao.selectPackage(packageOne.getId());
		assertEquals(packageOne, retrievedPackage);
		assertEquals(newPackageFileName, retrievedPackage.getPackageFileName());
	}

	@Test
	public void testDelete()
	{
		packageDao.deletePackage(packageOne.getId());
		Package retrievedPackage = packageDao.selectPackage(packageOne.getId());
		assertNull(retrievedPackage);
 
        String query = "SELECT COUNT(*) FROM " + PACKAGE_TBL;
		assertEquals(0, jdbcTemplate.queryForInt(query));
        query = "SELECT COUNT(*) FROM " + FILE_DATA_TABLE;
        assertEquals(0, jdbcTemplate.queryForInt(query));
	}
    
 	@After
	public void cleanUp()
	{
        String query = "DELETE FROM " + PACKAGE_TBL;
        jdbcTemplate.execute(query);

		query = "SELECT COUNT(*) FROM " + PACKAGE_TBL;
		assertEquals(0, jdbcTemplate.queryForInt(query));

        query = "DELETE FROM " + FILE_DATA_TABLE;
        jdbcTemplate.execute(query);

        query = "SELECT COUNT(*) FROM " + FILE_DATA_TABLE;
        assertEquals(0, jdbcTemplate.queryForInt(query));

	}
}
