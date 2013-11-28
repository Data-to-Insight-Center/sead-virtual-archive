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
package org.dataconservancy.ui.stripes;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.ArchiveServiceImpl;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.RelationshipServiceImpl;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.hibernate.mapping.MetaAttributable;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ARCHIVE_PROBLEM;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DOES_NOT_EXIST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_EMPTY_OR_INVALID_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_RETRIEVING_PROJECT_FOR_COLLECTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CollectionSplashActionBeanTest extends BaseActionBeanTest {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private RelationshipService relationshipService;


    MockHttpSession adminSession;

    @Before
    public void setUpActionBean() throws Exception {
        adminSession = authenticateUser(admin);

        //Set up the collection
        relationshipService.addCollectionToProject(collectionOne, projectOne);
    }
    
    @Test
    @DirtiesDatabase
    public void testStatusCodeAdmin() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionOne.getId());
        rt.execute();

        //Verify 200 for administrative access to this page
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testStatusCodeAnon() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class);
        rt.addParameter("collectionId", collectionOne.getId());
        rt.execute();

        //Verify 200 for anonymous access to this page
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    @Test
    @DirtiesDatabase
    public void testNoCollectionId() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class);
        rt.execute();

        //Verify 404 and error message for a request without a collectionId
        assertEquals(404, rt.getResponse().getStatus());
        assertEquals(rt.getActionBean(CollectionSplashActionBean.class).messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID), rt.getResponse().getErrorMessage());
    }

    @Test
    @DirtiesDatabase
    public void testBadCollectionId() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class);
        rt.addParameter("collectionId", "Bogus!");
        rt.execute();

        //Verify 404 and error string for a collection that doesn't exist
        assertEquals(404, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(CollectionSplashActionBean.class).messageKeys.getProperty(MSG_KEY_COLLECTION_DOES_NOT_EXIST), "Bogus!"), rt.getResponse().getErrorMessage());
    }

    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testPollArchiveException() throws Exception {
        String expectedExceptionString = "Archive exception from testPollArchiveException";
        
        // Create a mock archive service to inject into the
        // CollectionSplashActionBean for testing exception handling
        ArchiveService mockArchiveService = mock(ArchiveServiceImpl.class);
        doThrow(new ArchiveServiceException(expectedExceptionString)).when(mockArchiveService).pollArchive();

        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("archiveService", mockArchiveService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        //Perform the mock round trip
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class);
        rt.addParameter("collectionId", collectionOne.getId());
        rt.execute();

        //Verify 500 status and the archive error string
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(CollectionSplashActionBean.class).messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), expectedExceptionString), rt.getResponse().getErrorMessage());
    }

    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testRetrieveCollectionException() throws Exception {
        String expectedExceptionString = "Archive exception from testRetrieveCollectionException";

        // Create a mock archive service to inject into the
        // CollectionSplashActionBean for testing exception handling
        ArchiveService mockArchiveService = mock(ArchiveService.class);
        when(mockArchiveService.listDepositInfo(collectionOne.getId(), ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(archiveService.listDepositInfo(collectionOne.getId(), ArchiveDepositInfo.Status.DEPOSITED));

        CollectionBizService mockCollectionBizService = mock(CollectionBizService.class);
        when(mockCollectionBizService.getCollection(collectionOne.getId(), null)).thenThrow(new BizInternalException(expectedExceptionString));

        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("archiveService", mockArchiveService);
        springContext.getBeanFactory().registerSingleton("collectionBizService", mockCollectionBizService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        //Perform the mock round trip
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class);
        rt.addParameter("collectionId", collectionOne.getId());
        rt.execute();

        //Verify 500 status and the archive error string
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(CollectionSplashActionBean.class).messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), expectedExceptionString), rt.getResponse().getErrorMessage());
    }

    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testGetProjectForCollectionException() throws Exception {
        //Collection stored doesn't have deposit date, set to null so our mock matches
        ArchiveSearchResult<Collection> result = archiveService.retrieveCollection(collectionOneDepositID);
        collectionOne = result.getResults().iterator().next();
        
        String expectedExceptionString = "Archive exception from testGetProjectForCollectionException";

        // Create a mock relationship service to inject into the
        // CollectionSplashActionBean for testing exception handling
        RelationshipService mockRelationshipService = mock(RelationshipServiceImpl.class);
        when(mockRelationshipService.getProjectForCollection(collectionOne)).thenThrow(new RelationshipConstraintException(expectedExceptionString));

        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("relationshipService", mockRelationshipService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        //Perform the mock round trip
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionSplashActionBean.class);
        rt.addParameter("collectionId", collectionOne.getId());
        rt.execute();

        //Verify 500 status and the relationship error string
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(rt.getActionBean(CollectionSplashActionBean.class).messageKeys.getProperty(MSG_KEY_ERROR_RETRIEVING_PROJECT_FOR_COLLECTION), rt.getResponse().getErrorMessage());
    }
}
