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

import java.io.File;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.id.api.Types;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.model.Activity;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.CollectionActivityServiceImpl.DataItemCompressionGroup;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 2/10/12
 * Time: 9:28 AM
 * To change this template use File | Settings | File Templates.
 */
@DirtiesDatabase
@DirtiesContext
public class CollectionActivityServiceImplTest extends BaseUnitTest {
    

    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person userRole;

    @Autowired
    @Qualifier("adminUser")
    private Person adminRole;
    
    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    private UserService userService;
    
    private CollectionActivityService underTest;

    private DataItem[] dataItems = new DataItem[11];
    private String[] dataSetDepositIds = new String[11];

    private Activity activity0;
    private Activity activity1;
    private Activity activity2;
    private Activity activity3;
    private Activity activity4;
    private Activity activity5;
    private Activity activity6;
    private Activity activity7;
    private Activity activity8;
    private Activity activity9;

    boolean isObjectsCreated = false;
    

    @Before
    public void setUp() throws Exception {

        if (isObjectsCreated == false){
            //set the date-time to today at midnight - this is done to group 
            //entries by day in the service implementation
            DateTime today = DateTime.now();
            DateTime collectionToday =  new DateTime(today.getYear(), today.getMonthOfYear(),
                        today.getDayOfMonth(), today.getHourOfDay(), today.getMinuteOfHour());
            DateTime dataSetToday =  new DateTime(today.getYear(), today.getMonthOfYear(),
                        today.getDayOfMonth(), today.getHourOfDay(), today.getMinuteOfHour());
            
            underTest = new CollectionActivityServiceImpl(archiveService, userService);

            collectionOne.setDepositDate(collectionToday.minusDays(20));
            collectionOne.setDepositorId(userRole.getId());
            
            collectionNoData.setDepositDate(collectionToday.minusDays(10));
            collectionNoData.setDepositorId(adminRole.getId());
            
            assertEquals(ArchiveDepositInfo.Status.DEPOSITED, archiveService.getDepositStatus(collectionOneDepositID));
            assertEquals(ArchiveDepositInfo.Status.DEPOSITED, archiveService.getDepositStatus(collectionNoDataDepositID));

            String date = "2010-01-01";
            DateTime dt = DateTime.parse(date);
    
            for (Integer i=0; i<11; i++) {
                DataItem ds = new DataItem();
                ds.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
                ds.setDescription("This is test data set number " + i.toString());
                ds.setName("Test Data Set Name " + i.toString());
                if (i<10) {
                    ds.setDepositDate(dataSetToday.minusDays(i % 3));  //deposit the data sets over the last three days
                } else {
                    ds.setDepositDate(null);
                }
                if(i%2 == 0){
                    ds.setDepositorId(userRole.getId()); //even numbered ones deposited by userRole
                } else {
                    ds.setDepositorId(adminRole.getId()); //odd numbered ones deposited by adminRole
                }
                DataFile data_file = new DataFile();
                data_file.setName("Data Set File " + i.toString());
                File tmp = File.createTempFile("dataSetTempFile" + i.toString(), null);
                FileUtils.writeStringToFile(tmp, data_file.getName());
                tmp.deleteOnExit();
                data_file.setSource(tmp.toURI().toURL().toExternalForm());
                data_file.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
                data_file.setSize(tmp.length());

                ds.addFile(data_file);
                dataItems[i] = ds;
            }
    
            //deposit eight data sets to collectionOne
            for (int i=0; i<8; i++){
               dataSetDepositIds[i]=archiveService.deposit(collectionOneDepositID, dataItems[i]);
            }
            
            //deposit three dataItems sets to collectionNoData
            for (int i=8; i<11; i++){
              dataSetDepositIds[i] = archiveService.deposit(collectionNoDataDepositID, dataItems[i]);
            }
            
            archiveService.pollArchive();
    
            //make sure each data set is safely deposited
            for(int i=0; i<11; i++){
                assertNotNull(dataSetDepositIds[i]);
                ArchiveDepositInfo.Status depositStatus = archiveService.getDepositStatus(dataSetDepositIds[i]);
                assertEquals(ArchiveDepositInfo.Status.DEPOSITED, depositStatus);
                assertNotNull(archiveService.retrieveDataSet(dataSetDepositIds[i]));
            }
    
            //and that they are safely associated with collections
            ArchiveSearchResult<DataItem> dataSetResults = archiveService.retrieveDataSetsForCollection(collectionOneDepositID, -1, 0);
            java.util.Collection<DataItem> retrievedDataset = dataSetResults.getResults();
            assertNotNull(retrievedDataset);
            
            assertEquals(8, retrievedDataset.size());
            
            dataSetResults = archiveService.retrieveDataSetsForCollection(collectionNoDataDepositID, -1, 0);
            retrievedDataset = dataSetResults.getResults();
            assertNotNull(retrievedDataset);
            assertEquals(3, retrievedDataset.size());
            
            //set up expected activities resulting from the object setup
            //collectionWithData deposit
            activity0 = new Activity();
            activity0.setActor(userRole);
            activity0.setCount(1);
            activity0.setDateTimeOfOccurrence(collectionToday.minusDays(20));
            activity0.setType(Activity.Type.COLLECTION_DEPOSIT);
            //activity0.setDescription(null);
    
            //collectionNoData deposit
            activity1 = new Activity();
            activity1.setActor(adminRole);
            activity1.setCount(1);
            activity1.setDateTimeOfOccurrence(collectionToday.minusDays(10));
            activity1.setType(Activity.Type.COLLECTION_DEPOSIT);
    
            //datasets 0 and 6: userRole, deposited today, two items
            activity2 = new Activity();
            activity2.setActor(userRole);
            activity2.setCount(2);
            activity2.setDateTimeOfOccurrence(dataSetToday);
            activity2.setType(Activity.Type.DATASET_DEPOSIT);
    
            //dataset 4: userRole, deposited yesterday, one item
            activity3 = new Activity();
            activity3.setActor(userRole);
            activity3.setCount(1);
            activity3.setDateTimeOfOccurrence(dataSetToday.minusDays(1));
            activity3.setType(Activity.Type.DATASET_DEPOSIT);

            //dataset 2: userRole, deposited two days ago, one item
            //dataset 8:
            activity4 = new Activity();
            activity4.setActor(userRole);
            activity4.setCount(1);
            activity4.setDateTimeOfOccurrence(dataSetToday.minusDays(2));
            activity4.setType(Activity.Type.DATASET_DEPOSIT);

            //dataset 3: adminRole,  deposited today, one item
            //dataset 9:
            activity5 = new Activity();
            activity5.setActor(adminRole);
            activity5.setCount(1);
            activity5.setDateTimeOfOccurrence(dataSetToday);
            activity5.setType(Activity.Type.DATASET_DEPOSIT);

            //datasets 1 and 7: adminRole, deposited yesterday, two items
            activity6 = new Activity();
            activity6.setActor(adminRole);
            activity6.setCount(2);
            activity6.setDateTimeOfOccurrence(dataSetToday.minusDays(1));
            activity6.setType(Activity.Type.DATASET_DEPOSIT);

            //dataset5: adminRole, deposited two days ago, one item
            activity7 = new Activity();
            activity7.setActor(adminRole);
            activity7.setCount(1);
            activity7.setDateTimeOfOccurrence(dataSetToday.minusDays(2));
            activity7.setType(Activity.Type.DATASET_DEPOSIT);
            
            //dataset 10 : admin Role, deposited null, one item
            activity8 = new Activity();
            activity8.setActor(userRole);
            activity8.setCount(1);
            activity8.setDateTimeOfOccurrence(null);
            activity8.setType(Activity.Type.DATASET_DEPOSIT);

            isObjectsCreated = true;
        }
    }



    @Test
    public void testRetrieveActivitiesForCollectionByDate(){

        //collection 1
        List<Activity> actualActivitiesList = underTest.retrieveActivitiesForCollectionByDate(collectionOne);
             
        assertEquals(7, actualActivitiesList.size());
        assertEquals(actualActivitiesList.get(1).getDateTimeOfOccurrence().getZone(), activity2.getDateTimeOfOccurrence().getZone());

        assertTrue(actualActivitiesList.contains(activity0));
        assertFalse(actualActivitiesList.contains(activity1));
        assertTrue(actualActivitiesList.contains(activity2));
        assertTrue(actualActivitiesList.contains(activity3));
        assertTrue(actualActivitiesList.contains(activity4));
        assertTrue(actualActivitiesList.contains(activity5));
        assertTrue(actualActivitiesList.contains(activity6));
        assertTrue(actualActivitiesList.contains(activity7));
        assertFalse(actualActivitiesList.contains(activity8));



        //now we worry about the order - should be latest ones first
        //we don't know what order different users will occur if multiple users deposit
        //on the same date
        //today
        assertTrue((actualActivitiesList.get(0).equals(activity2) && actualActivitiesList.get(1).equals(activity5))
            ||   (actualActivitiesList.get(0).equals(activity5) && actualActivitiesList.get(1).equals(activity2)));
        //one day ago
        assertTrue((actualActivitiesList.get(2).equals(activity3) && actualActivitiesList.get(3).equals(activity6))
            ||   (actualActivitiesList.get(2).equals(activity6) && actualActivitiesList.get(3).equals(activity3)));
        //two days ago
        assertTrue((actualActivitiesList.get(4).equals(activity4) && actualActivitiesList.get(5).equals(activity7))
                || (actualActivitiesList.get(4).equals(activity7) && actualActivitiesList.get(5).equals(activity4)));
        //collection deposit should be last
        assertEquals(actualActivitiesList.get(6), activity0);

        actualActivitiesList = underTest.retrieveActivitiesForCollectionByDate(collectionNoData);
        assertEquals(4, actualActivitiesList.size());

        assertFalse(actualActivitiesList.contains(activity0));
        assertTrue(actualActivitiesList.contains(activity1));
        assertFalse(actualActivitiesList.contains(activity2));
        assertFalse(actualActivitiesList.contains(activity3));
        assertTrue(actualActivitiesList.contains(activity4));
        assertTrue(actualActivitiesList.contains(activity5));
        assertFalse(actualActivitiesList.contains(activity6));
        assertFalse(actualActivitiesList.contains(activity7));
        assertTrue(actualActivitiesList.contains(activity8));


        //now we worry about the order - should be latest ones first
        //today
        assertEquals(activity5, actualActivitiesList.get(0));
        //two days ago
        assertEquals(activity4, actualActivitiesList.get(1));
        //collection deposit should be next to last
        assertEquals(activity1, actualActivitiesList.get(2));
        //null date should be last
        assertEquals(activity8, actualActivitiesList.get(3));
    }


    @Test
    public void testDataItemCompressionGroupHandlesNullDatesGracefully() {
        DataItemCompressionGroup group = 
            ((CollectionActivityServiceImpl)underTest).new DataItemCompressionGroup(null);

        group.addDeposit(null);

        assertEquals(1,group.getAllDepositDates().size());
        assertEquals(2,group.getDailyDepositCount(null));
        assertNull(group.getLastDepositDate(null));
    }

}
