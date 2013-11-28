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

package org.dataconservancy.ui.api;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_CLASS;

import java.io.IOException;
import java.util.List;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
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
@DirtiesDatabase(AFTER_CLASS)
@DirtiesContext
@ContextConfiguration(locations = {"classpath*:/org/dataconservancy/mhf/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/registry/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/access/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/model/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/packaging/config/applicationContext.xml"})
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
        projectOne.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
        projectService.create(projectOne);

        relationshipService.addAdministratorToProject(projectOne, admin);
        
        projectTwo.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
       
        projectService.create(projectTwo);

        relationshipService.addAdministratorToProject(projectTwo, admin);
    
        metadataFileTwo.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());

        collectionWithData.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        
        relationshipService.addCollectionToProject(collectionWithData, projectOne);
        relationshipService.addDepositorToCollection(user, collectionWithData);
        
        collectionNoData.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
 

        collectionOne.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());

        
        relationshipService.addCollectionToProject(collectionWithData, projectOne);
        relationshipService.addDepositorToCollection(user, collectionWithData);

        dataItemOne.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
        dataFileOne.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());

        relationshipService.addDataSetToCollection(dataItemOne, collectionWithData);
        relationshipService.addDataFileToDataSet(dataFileOne, dataItemOne);

        dataItemTwo.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());

        dataFileTwo.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());

        relationshipService.addDataSetToCollection(dataItemTwo, collectionWithData);
        relationshipService.addDataFileToDataSet(dataFileTwo, dataItemTwo);
     
        //Create a new user not in the system to use for adding user tests
        newUser.setId(idService.create(Types.PERSON.name()).getUrl().toString());
        newUser.setRegistrationStatus(RegistrationStatus.PENDING);
        
        collectionWithDataDepositID = archiveService.deposit(collectionWithData);  
        collectionNoDataDepositID = archiveService.deposit(collectionNoData);
        collectionOneDepositID = archiveService.deposit(collectionOne);
        
        archiveService.pollArchive(); 
        List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(collectionWithData.getId(), Status.DEPOSITED);
        collectionWithDataDepositDate = depositInfo.get(0).getDepositDateTime();
        
        depositInfo = archiveService.listDepositInfo(collectionNoData.getId(), Status.DEPOSITED);
        collectionNoDataDepositDate = depositInfo.get(0).getDepositDateTime();
        
        depositInfo = archiveService.listDepositInfo(collectionOne.getId(), Status.DEPOSITED);
        collectionOneDepositDate = depositInfo.get(0).getDepositDateTime();
        
        dataItemOneDepositID = archiveService.deposit(collectionWithDataDepositID, dataItemOne);
        
        dataItemTwoDepositID = archiveService.deposit(collectionWithDataDepositID, dataItemTwo);
        
        archiveService.pollArchive();
        
        depositInfo = archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED);
        dataItemOneDepositDate = depositInfo.get(0).getDepositDateTime();
        
        depositInfo = archiveService.listDepositInfo(dataItemTwo.getId(), Status.DEPOSITED);
        dataItemTwoDepositDate = depositInfo.get(0).getDepositDateTime();
    }
}

