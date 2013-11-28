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

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_CLASS;

import java.io.IOException;
import java.util.List;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration(locations = { "classpath*:/org/dataconservancy/ui/config/test-applicationContext.xml",
        "classpath*:/org/dataconservancy/access/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/ui/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/mhf/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/registry/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/model/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/packaging/config/applicationContext.xml"})
@DirtiesDatabase(AFTER_CLASS)
@DirtiesContext
public abstract class BaseUnitTest extends BaseModelTest {

    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ArchiveService archiveService;
    
    @Before
    public final void baseSetupServices() throws RelationshipConstraintException, IOException, ArchiveServiceException {
        //Set up the project ids and the relationships with there admins.
        projectOne.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
        projectService.create(projectOne);

        relationshipService.addAdministratorToProject(projectOne, admin);
        
        projectTwo.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
       
        projectService.create(projectTwo);

        relationshipService.addAdministratorToProject(projectTwo, admin);
    
        //Set up the collection Ids and their relationships to projects, and depositors
        collectionWithData.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        collectionWithData.setParentProjectId(projectOne.getId());
        
        relationshipService.addCollectionToProject(collectionWithData, projectOne);
        relationshipService.addDepositorToCollection(user, collectionWithData);
        
        collectionNoData.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        collectionOne.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        collectionOne.setParentProjectId(projectOne.getId());
        
        relationshipService.addCollectionToProject(collectionOne, projectOne);
        relationshipService.addDepositorToCollection(user, collectionOne);

        //Setup the data item id and their relationships to files and collections
        dataItemOne.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
        dataFileOne.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
        dataItemOne.setParentId(collectionWithData.getId());

        relationshipService.addDataSetToCollection(dataItemOne, collectionWithData);
        relationshipService.addDataFileToDataSet(dataFileOne, dataItemOne);

        dataItemTwo.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
        dataItemTwo.setParentId(collectionWithData.getId());
        
        dataFileTwo.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());

        relationshipService.addDataSetToCollection(dataItemTwo, collectionWithData);
        relationshipService.addDataFileToDataSet(dataFileTwo, dataItemTwo);
     
        //Create a new user not in the system to use for adding user tests
        newUser.setId(idService.create(Types.PERSON.name()).getUrl().toString());
        
        //Deposit all the collections into the archive
        collectionWithDataDepositID = archiveService.deposit(collectionWithData);  
        collectionNoDataDepositID = archiveService.deposit(collectionNoData);
        collectionOneDepositID = archiveService.deposit(collectionOne);
        
        archiveService.pollArchive(); 
        
        //Setup the deposit dates for the collections
        List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(collectionWithData.getId(), Status.DEPOSITED);
        collectionWithDataDepositDate = depositInfo.get(0).getDepositDateTime();
        
        depositInfo = archiveService.listDepositInfo(collectionNoData.getId(), Status.DEPOSITED);
        collectionNoDataDepositDate = depositInfo.get(0).getDepositDateTime();
        
        depositInfo = archiveService.listDepositInfo(collectionOne.getId(), Status.DEPOSITED);
        collectionOneDepositDate = depositInfo.get(0).getDepositDateTime();
        
        //Deposit the data items into the archive.
        dataItemOneDepositID = archiveService.deposit(collectionWithDataDepositID, dataItemOne);
        dataItemTwoDepositID = archiveService.deposit(collectionWithDataDepositID, dataItemTwo);
        
        archiveService.pollArchive();
        
        //Setup the deposit dates for the archive.
        depositInfo = archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED);
        dataItemOneDepositDate = depositInfo.get(0).getDepositDateTime();
        
        depositInfo = archiveService.listDepositInfo(dataItemTwo.getId(), Status.DEPOSITED);
        dataItemTwoDepositDate = depositInfo.get(0).getDepositDateTime();
        
        //Remove the old metadata file ids from the collection add the newly minted ones
        relationshipService.removeMetadataFileFromBusinessObject(metadataFileOne.getId(), collectionWithData.getId());
        metadataFileOne.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        metadataFileOne.setParentId(collectionWithData.getId());
        relationshipService.addMetadataFileToBusinessObject(metadataFileOne.getId(), collectionWithData.getId());
        
        relationshipService.removeMetadataFileFromBusinessObject(metadataFileTwo.getId(), collectionWithData.getId());
        metadataFileTwo.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        metadataFileTwo.setParentId(collectionWithData.getId());
        relationshipService.addMetadataFileToBusinessObject(metadataFileTwo.getId(), collectionWithData.getId());
        
        //Deposit the metadata files into the archive
        metadataFileOneDepositID = archiveService.deposit(collectionWithDataDepositID, metadataFileOne);
        metadataFileTwoDepositID = archiveService.deposit(collectionWithDataDepositID, metadataFileTwo);
        
        archiveService.pollArchive();
        
        //Update the archive to point to it's new metadata files, and update it's deposit date
        collectionWithDataDepositID = archiveService.deposit(collectionWithData);
        
        archiveService.pollArchive();
        
        depositInfo = archiveService.listDepositInfo(collectionWithData.getId(), Status.DEPOSITED);
        collectionWithDataDepositDate = depositInfo.get(0).getDepositDateTime();        
    }
}

