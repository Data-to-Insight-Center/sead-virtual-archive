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


import static junit.framework.Assert.assertEquals;

import org.dataconservancy.ui.dao.PackageDAO;
import org.dataconservancy.ui.exceptions.PackageException;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Package;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 1/30/12
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageServiceImplTest extends BaseUnitTest {

    @Autowired
	private PackageDAO packageDao;
    
    @Autowired
    private JdbcTemplate template;
    
    private Package packageOne = new Package();
    private Package packageTwo = new Package();
    private DataItem dataSetThree = new DataItem();
    
    private Logger log =  LoggerFactory.getLogger(this.getClass());
    
    private PackageService underTest;
    
    private static final String PACKAGE_TABLE = "PACKAGE";
    private static final String FILE_DATA_TABLE = "PACKAGE_FILE_DATA";

    static final String PACKAGE_TABLE_ROW_COUNT_QUERY =
            "SELECT count(*) FROM " + PACKAGE_TABLE;

    static final String PACKAGE_TABLE_DELETE_ALL_ROWS_QUERY =
            "DELETE FROM " + PACKAGE_TABLE;

    static final String FILE_DATA_TABLE_ROW_COUNT_QUERY =
            "SELECT count(*) FROM " + FILE_DATA_TABLE;

    static final String FILE_DATA_TABLE_DELETE_ALL_ROWS_QUERY =
            "DELETE FROM " + FILE_DATA_TABLE;


    @Before
    public void setup(){
        dataSetThree.setId("PkgServiceTestdataSetId3");
        dataSetThree.setName("PkgServiceTestDataSetThree");
        
        packageOne.setId("PkgServiceTestpackage1");
        packageOne.setPackageType(Package.PackageType.ZIP);
		packageOne.setPackageFileName("package1.zip");
        packageOne.addFile(dataItemOne.getId(), dataItemOne.getName());
        packageOne.addFile(dataItemTwo.getId(), dataItemTwo.getName());
        
        packageTwo.setId("PkgServiceTestpackage2");
        packageTwo.setPackageType(Package.PackageType.SIMPLE_FILE);
        packageTwo.setPackageFileName("dataSet2");
        packageTwo.addFile(dataSetThree.getId(), dataSetThree.getName());

        template.execute(PACKAGE_TABLE_DELETE_ALL_ROWS_QUERY);
        template.execute(FILE_DATA_TABLE_DELETE_ALL_ROWS_QUERY);
        assertEquals(0, template.queryForInt(PACKAGE_TABLE_ROW_COUNT_QUERY));
        assertEquals(0, template.queryForInt(FILE_DATA_TABLE_ROW_COUNT_QUERY));
        underTest = new PackageServiceImpl(packageDao);
    }

    @After
    public void tearDown(){
        template.execute(PACKAGE_TABLE_DELETE_ALL_ROWS_QUERY);
        template.execute(FILE_DATA_TABLE_DELETE_ALL_ROWS_QUERY);
        assertEquals(0, template.queryForInt(PACKAGE_TABLE_ROW_COUNT_QUERY));
        assertEquals(0, template.queryForInt(FILE_DATA_TABLE_ROW_COUNT_QUERY));
    }


    @Test
    public void getTest(){
      packageDao.insertPackage(packageOne);
      Package retrievedPackage = underTest.get(packageOne.getId());
      assertEquals(packageOne, retrievedPackage);    
    }

    @Test
    public void createTest(){
        Package createdPackage = new Package();
      try{  
        createdPackage = underTest.create(packageTwo);
      } catch (PackageException e){
         log.error("Could not create package " + packageTwo.getId(), e); 
      }    
      Package retrievedPackage = packageDao.selectPackage(createdPackage.getId());
	  assertEquals(retrievedPackage, createdPackage);
      assertEquals(packageTwo, retrievedPackage);
    }

    @Test
    public void updateTest(){
      Package updatedPackage = packageTwo;
      packageDao.insertPackage(updatedPackage);
      updatedPackage.setPackageFileName("updated1.zip");
      updatedPackage.addFile(dataSetThree.getId(), dataSetThree.getName());
      updatedPackage.removeFile(dataItemOne.getId());
      try{
      underTest.update(updatedPackage);
      } catch (PackageException e){
         log.error("Could not update package " + packageTwo.getId(), e);
      }
      Package retrievedPackage = packageDao.selectPackage(updatedPackage.getId());
      assertEquals(updatedPackage, retrievedPackage);  
    }

    @Test
    public void removeTest(){
      packageDao.insertPackage(packageOne);
      underTest.remove(packageOne);
      assertEquals(0, packageDao.selectPackage().size());
    }

    @Test
    public void findAllPackagesTest(){
       assertEquals(0,underTest.findAllPackages().size());
       packageDao.insertPackage(packageOne);
       packageDao.insertPackage(packageTwo);
       assertEquals(2, underTest.findAllPackages().size());
    }
    
}