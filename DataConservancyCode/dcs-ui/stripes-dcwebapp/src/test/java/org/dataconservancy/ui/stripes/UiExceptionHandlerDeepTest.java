/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.ui.stripes;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class UiExceptionHandlerDeepTest extends org.dataconservancy.ui.stripes.BaseActionBeanTest {
    
    @Autowired
    @Qualifier("archiveService")
    private ArchiveService archiveService;
    
    @Autowired
    @Qualifier("eventManager")
    private EventManager eventManager;
    
    @Autowired
    @Qualifier("eventManagerExecutorService")
    private ExecutorService executorService;
    
    @DirtiesContext
    @DirtiesDatabase
    @Test
    public void testDepositWithExceptionFiresEvent() throws Exception {
        // Create a session
        MockHttpSession adminSession = authenticateUser(admin);
        
        // Create a project
        MockRoundtrip projectRT = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        projectRT.addParameter("project.name", "testDepositWithExceptionFiresEventProject");
        projectRT.addParameter("project.description", "asdf");
        projectRT.addParameter("project.fundingEntity", "fdsa");
        projectRT.addParameter("projectAdminIDList[0]", "admin");
        projectRT.addParameter("project.numbers[0]", "5555");
        projectRT.addParameter("project.startDate", "01/01/01");
        projectRT.addParameter("project.endDate", "02/02/02");
        projectRT.execute("userProjectAdded");
        
        String projectId = projectRT.getActionBean(ProjectActionBean.class).getProject().getId();
        
        adminSession.setAttribute("project_id", projectId);
        
        // Create a collection
        MockRoundtrip collectionRT = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);
        collectionRT.addParameter("collection.title", "testDepositWithExceptionFiresEventCollection");
        collectionRT.addParameter("collection.summary", "asdf");
        collectionRT.addParameter("projectId", projectId);
        collectionRT.addParameter("collection.creators[0].familyNames", "Creator");
        collectionRT.execute("addCollection");
        
        archiveService.pollArchive();
        String collectionId = collectionRT.getActionBean(AddCollectionActionBean.class).getCollectionId();
        String depositId = archiveService.listDepositInfo(collectionId, ArchiveDepositInfo.Status.DEPOSITED).get(0)
                .getDepositId();
        
        // Create a mock archive service to inject into the DepositActionBean for the last bit
        // This is a bit like spying, but mockito can't spy on final classes <sadface>
        ArchiveService mockArchiveService = mock(ArchiveService.class);
        when(mockArchiveService.listDepositInfo(collectionId, ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(
                archiveService.listDepositInfo(collectionId, ArchiveDepositInfo.Status.DEPOSITED));
        when(mockArchiveService.retrieveCollection(depositId)).thenReturn(archiveService.retrieveCollection(depositId));
        when(mockArchiveService.deposit(anyString(), any(DataItem.class))).thenThrow(
                new ArchiveServiceException("Deposit Exception from testDepositWithExceptionFiresEvent"));
        
        // Inject the mockArchiveService into the spring context and add the mock listener
        GenericWebApplicationContext springContext = (GenericWebApplicationContext) servletCtx
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("archiveService", mockArchiveService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);
        
        // Create a mocked EventListener and add it to the event manager
        EventListener mockEventListener = mock(EventListener.class);
        eventManager.addListener(mockEventListener);
        
        // Create a temp file
        File tempFile = File.createTempFile("testDepositWithExceptionFiresEventTempFile", null);
        tempFile.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tempFile);
        out.println("testDepositWithExceptionFiresEventCollection");
        out.close();
        
        DataItem dataSet = new DataItem();
        dataSet.setName(tempFile.getName());
        
        // Create a MockRoundtrip to use (with a throw-always archive service)
        MockRoundtrip depositRT = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        depositRT.addParameter("currentCollectionId", collectionId);
        depositRT.addParameter("uploadedFile", tempFile.getPath());
        // Making sure the page is redirected properly after the deposit.
        depositRT.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        depositRT.execute("deposit");
        
        verify(mockArchiveService).deposit(anyString(), any(DataItem.class));
        
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        verify(mockEventListener).handleEvent(Matchers.any(EventContext.class), Matchers.any(Event.class));
    }
}
