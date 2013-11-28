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
import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.EZIDService;
import org.dataconservancy.ui.util.EZIDCollectionMetadataGeneratorImpl;
import org.dataconservancy.ui.util.EZIDMetadata;
import org.dataconservancy.ui.util.EZIDMetadataGenerator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * tests methods in the CitableLocatorActionBean
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CitableLocatorActionBeanTest extends BaseActionBeanTest {

    static final String HOME_CITABLE_LOCATOR_PATH = "/pages/citable_locator_confirmation.jsp";

    private MockHttpSession userSession;
    private MockHttpSession adminSession;
    private MockHttpSession projectAdminSession;

    private AuthorizationService authorizationService;
    private EZIDService ezidService;
    private CollectionBizService collectionBizService;
    private EZIDMetadataGenerator<Collection> metadataGenerator;


    @Autowired
    private ArchiveService archiveService;

    private static final String newEzid = "newEZID";
    private static final String nonExistingCollectionId = "id:collection:DoesNotExist";

    private Collection noPubDateCollection;
    private Collection noCreatorCollection;

    @Before
    public void setup() throws Exception {
        //set up Collections
        collectionWithData = new Collection();
        collectionWithData.setId("I AM A COLLECTION ID FOR CITABLE LOCATOR ACTION BEAN");
        collectionWithData.setSummary("Collection 1 summary");
        collectionWithData.setTitle("brief title");
        collectionWithData.setPublicationDate(DateTime.now());

        PersonName collectionCreator = new PersonName("Dr.", "Do", "Very", "Little", "Yippe");
        collectionWithData.addCreator(collectionCreator);

        noPubDateCollection = new Collection(collectionWithData);
        noPubDateCollection.setId("NoPubID");
        noPubDateCollection.setDepositDate(null);

        noCreatorCollection = new Collection(collectionWithData);
        noCreatorCollection.setId("NoCreatorId");
        noCreatorCollection.setCreators(null);

        //set up mock services
        //Mock the requisite services
        authorizationService = mock(AuthorizationService.class);
        when(authorizationService.canUpdateCollection(user, collectionWithData)).thenReturn(true);
        when(authorizationService.canUpdateCollection(user, noPubDateCollection)).thenReturn(true);
        when(authorizationService.canUpdateCollection(user, noCreatorCollection)).thenReturn(true);
        when(authorizationService.canUpdateCollection(admin, collectionWithData)).thenReturn(true);
        when(authorizationService.canUpdateCollection(admin, noPubDateCollection)).thenReturn(true);
        when(authorizationService.canUpdateCollection(admin, noCreatorCollection)).thenReturn(true);
        when(authorizationService.canUpdateCollection(unauthorizedUser, collectionWithData)).thenReturn(false);
        when(authorizationService.canUpdateCollection(unauthorizedUser, noPubDateCollection)).thenReturn(false);
        when(authorizationService.canUpdateCollection(unauthorizedUser, noCreatorCollection)).thenReturn(false);

        collectionBizService = mock(CollectionBizService.class);
        when(collectionBizService.getCollection(eq(collectionWithData.getId()), Matchers.<Person>any(Person.class))).thenReturn(collectionWithData);
        when(collectionBizService.getCollection(eq(noPubDateCollection.getId()), Matchers.<Person>any(Person.class))).thenReturn(noPubDateCollection);
        when(collectionBizService.getCollection(eq(noCreatorCollection.getId()), Matchers.<Person>any(Person.class))).thenReturn(noCreatorCollection);
        when(collectionBizService.getCollection(eq(nonExistingCollectionId), Matchers.<Person>any(Person.class))).thenReturn(null);

        ezidService = mock(EZIDService.class);
        when(ezidService.createID(any(EZIDMetadata.class))).thenReturn(newEzid);

        metadataGenerator = mock(EZIDCollectionMetadataGeneratorImpl.class);
        when(metadataGenerator.generateMetadata(collectionWithData)).thenReturn(new EZIDMetadata());
        when(metadataGenerator.generateMetadata(noPubDateCollection)).thenThrow(new EZIDMetadataException());
        when(metadataGenerator.generateMetadata(noCreatorCollection)).thenThrow(new EZIDMetadataException());

        setUpUserSessions();

        GenericWebApplicationContext springContext = (GenericWebApplicationContext) servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("authorizationService", authorizationService);
        springContext.getBeanFactory().registerSingleton("collectionBizService", collectionBizService);
        springContext.getBeanFactory().registerSingleton("ezidService", ezidService);
        springContext.getBeanFactory().registerSingleton("ezidCollectionMetadataGenerator", metadataGenerator);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);
    }

    private void setUpUserSessions() throws Exception {
        // Mock a session for a random registered, authorized unauthorizedUser.
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", unauthorizedUser.getEmailAddress());
        rt.setParameter("j_password", unauthorizedUser.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) userSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(unauthorizedUser.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());

        // Mock a session for a system-wide admin unauthorizedUser
        adminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        ctx = (SecurityContext) adminSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());

        // Mock a session for a project admin unauthorizedUser
        projectAdminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", projectAdminSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        ctx = (SecurityContext) projectAdminSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
    }

    /**
     * Asserts that the default handler is what we expect it to be
     */
    @Test
    public void testDefaultHandler() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute();
        assertEquals(CitableLocatorActionBean.HOME_CITABLE_LOCATOR_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());

        String id = rt.getActionBean(CitableLocatorActionBean.class).getReservedCitableLocator();
        assertEquals(id, newEzid);
        verify(collectionBizService).getCollection(collectionWithData.getId(), admin);
        verify(authorizationService).canUpdateCollection(admin, collectionWithData);
        verify(metadataGenerator).generateMetadata(Matchers.<Collection>any(Collection.class));
        verify(ezidService).createID(any(EZIDMetadata.class));
    }

    /**
     * Asserts that an authorized unauthorizedUser can reserve a DOI for a collectionWithData
     *
     * @throws Exception
     */
    @Test
    public void testReserveCitableLocatorByAuthorizedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("reserveDOI");
        assertEquals(200, rt.getResponse().getStatus());

        String id = rt.getActionBean(CitableLocatorActionBean.class).getReservedCitableLocator();
        assertEquals(id, newEzid);
        verify(authorizationService).canUpdateCollection(admin, collectionWithData);
        verify(collectionBizService).getCollection(collectionWithData.getId(), admin);
        verify(metadataGenerator).generateMetadata(Matchers.<Collection>any(Collection.class));
        verify(ezidService).createID(any(EZIDMetadata.class));
    }

    /**
     * Asserts that an authorized unauthorizedUser can confirm a DOI for a collectionWithData
     *
     * @throws Exception
     */
    @Test
    public void testConfirmCitableLocatorByAuthorizedUser() throws Exception {

        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.addParameter("reservedCitableLocator", newEzid);

        //Assert that the collectionWithData's citableLocator field is not set
        assertNull(collectionWithData.getCitableLocator());

        rt.execute("confirmDOI");
        assertEquals(302, rt.getResponse().getStatus());

        String id = rt.getActionBean(CitableLocatorActionBean.class).getReservedCitableLocator();
        assertEquals(id, newEzid);
        verify(authorizationService).canUpdateCollection(admin, collectionWithData);
        verify(ezidService).saveID(newEzid);
        verify(collectionBizService).getCollection(collectionWithData.getId(), admin);

        //assert that collectionWithData's citableLocator is set after mocked roundtrip
        assertNotNull(collectionWithData.getCitableLocator());
        assertEquals(newEzid, collectionWithData.getCitableLocator());
        //assert that the collectionBizService.updateCollection method is called with the collectionWithData with the set citableLocator
        verify(collectionBizService).updateCollection(collectionWithData,admin);
    }


    /**
     * Asserts that an authorised unauthorizedUser can cancel the assignment of a DOI for a collectionWithData
     *
     * @throws Exception
     */
    @Test
    public void testCancelCitableLocatorByAuthorizedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.addParameter("reservedCitableLocator", newEzid);

        rt.execute("cancel");
        assertEquals(302, rt.getResponse().getStatus());
        verify(authorizationService).canUpdateCollection(admin, collectionWithData);
        verify(collectionBizService, times(1)).getCollection(collectionWithData.getId(), admin);
        verify(ezidService).deleteID(newEzid);
    }

    /**
     * Asserts that an unauthorized unauthorizedUser cannot reserve a DOI for a collectionWithData
     *
     * @throws Exception
     */
    @Test
    public void testReserveCitableLocatorByUnauthorizedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, userSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("reserveDOI");
        assertEquals(403, rt.getResponse().getStatus());
        verify(authorizationService).canUpdateCollection(unauthorizedUser, collectionWithData);
        verifyZeroInteractions(metadataGenerator);
        verifyZeroInteractions(ezidService);
        verify(authorizationService, times(1)).canUpdateCollection(unauthorizedUser, collectionWithData);
        verify(collectionBizService, times(1)).getCollection(collectionWithData.getId(), unauthorizedUser);
    }

    /**
     * Asserts that unauthorizedUser could not request EZID for collectionWithData with out publication date
     *
     * @throws Exception
     */
    @Test
    public void testReserveCitableLocatorWithNoPublicationDate() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", noPubDateCollection.getId());
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("reserveDOI");
        assertEquals(500, rt.getResponse().getStatus());
        verifyZeroInteractions(ezidService);
        verify(metadataGenerator, times(1)).generateMetadata(noPubDateCollection);
        verify(authorizationService, times(1)).canUpdateCollection(admin, noPubDateCollection);
        verify(collectionBizService, times(1)).getCollection(noPubDateCollection.getId(), admin);
    }
    /**
     * Asserts that unauthorizedUser could not request EZID for collectionWithData with out creator
     *
     * @throws Exception
     */
    @Test
    public void testReserveCitableLocatorWithNoCreator() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", noCreatorCollection.getId());
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("reserveDOI");
        assertEquals(500, rt.getResponse().getStatus());
        verifyZeroInteractions(ezidService);
        verify(metadataGenerator, times(1)).generateMetadata(noCreatorCollection);
        verify(authorizationService, times(1)).canUpdateCollection(admin, noCreatorCollection);
        verify(collectionBizService, times(1)).getCollection(noCreatorCollection.getId(), admin);
    }

    /**
     * Asserts that an unauthorized unauthorizedUser cannot confirm a DOI for a collectionWithData
     *
     * @throws Exception
     */
    @Test
    public void testConfirmCitableLocatorByUnauthorizedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, userSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("confirmDOI");
        assertEquals(403, rt.getResponse().getStatus());
        verifyZeroInteractions(metadataGenerator);
        verifyZeroInteractions(ezidService);
        verify(authorizationService, times(1)).canUpdateCollection(unauthorizedUser, collectionWithData);
        verify(collectionBizService, times(1)).getCollection(collectionWithData.getId(), unauthorizedUser);
    }

    /**
     * Asserts that an unauthorized unauthorizedUser cannot cancel the assignment for a DOI for a collectionWithData
     *
     * @throws Exception
     */
    @Test
    public void testCancelCitableLocatorByUnauthorizedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, userSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("cancel");
        assertEquals(403, rt.getResponse().getStatus());
        verifyZeroInteractions(metadataGenerator);
        verifyZeroInteractions(ezidService);
        verify(authorizationService, times(1)).canUpdateCollection(unauthorizedUser, collectionWithData);
        verify(collectionBizService, times(1)).getCollection(collectionWithData.getId(), unauthorizedUser);
    }

    /**
     * Asserts that attempt to reserve ezid citable locator for a non-existing collectionWithData will results in a 404 returned code
     */
    @Test
    public void testReserveEzidForNonExistingCollection() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", nonExistingCollectionId);
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("reserveDOI");
        assertEquals(404, rt.getResponse().getStatus());
        verifyZeroInteractions(metadataGenerator);
        verifyZeroInteractions(ezidService);
        verifyZeroInteractions(authorizationService);
        verify(collectionBizService, times(1)).getCollection(nonExistingCollectionId, admin);
    }

    /**
     * Asserts that attempt to confirm ezid citable locator for a non-existing collectionWithData will results in a 404 returned code
     */
    @Test
    public void testConfirmEzidForNonExistingCollection() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", nonExistingCollectionId);
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("confirmDOI");
        assertEquals(404, rt.getResponse().getStatus());
        verifyZeroInteractions(metadataGenerator);
        verifyZeroInteractions(ezidService);
        verifyZeroInteractions(authorizationService);
        verify(collectionBizService, times(1)).getCollection(nonExistingCollectionId, admin);
    }

    /**
     * Asserts that attempt to cancel ezid citable locator for a non-existing collectionWithData will results in a 404 returned code
     */
    @Test
    public void testCancelEzidForNonExistingCollection() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CitableLocatorActionBean.class, adminSession);
        rt.addParameter("collectionId", nonExistingCollectionId);
        rt.addParameter("reservedCitableLocator", newEzid);
        rt.execute("cancel");
        assertEquals(404, rt.getResponse().getStatus());
        verifyZeroInteractions(metadataGenerator);
        verifyZeroInteractions(ezidService);
        verifyZeroInteractions(authorizationService);
        verify(collectionBizService, times(1)).getCollection(nonExistingCollectionId, admin);
    }
}
