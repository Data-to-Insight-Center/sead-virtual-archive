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

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.ui.model.Address;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Tests for the AddCollectionActionBean
 */
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
public class UpdateCollectionActionBeanTest extends BaseActionBeanTest {
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person instanceAdmin;
    
    @Autowired
    private ArchiveService archiveService;
    
    private CollectionBizService collectionBizService;
    private AuthorizationService authorizationService;
    private UpdateCollectionActionBean underTest;
    
    @Autowired
    private JdbcTemplate template;
    
    private MockHttpSession registeredUserSession;
    
    private MockHttpSession adminSession;
    
    /**
     * Initialize the mock http session with authenticated user credentials. Tests that re-use this mock session will be
     * already logged in.
     */
    @Before
    public void setUpForUpdateCollectionActionBeanTest() throws Exception {
        
        ArchiveSearchResult<Collection> result = archiveService.retrieveCollection(collectionWithDataDepositID);
        collectionWithData = result.getResults().iterator().next();
        
        // Mock a session for a registered, authorized user.
        registeredUserSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", registeredUserSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) registeredUserSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        // Mock a session for a system-wide admin user
        adminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        ctx = (SecurityContext) adminSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        // mock behavior of the collection biz service to return deposited collection;
        collectionBizService = mock(CollectionBizService.class);
        Collection depositedCollection = archiveService.retrieveCollection(collectionWithDataDepositID).getResults()
                .iterator().next();
        
        when(collectionBizService.getCollection(eq(collectionWithData.getId()), any(Person.class))).thenReturn(
                depositedCollection);
        assertEquals(collectionWithData, collectionBizService.getCollection(collectionWithData.getId(), admin));
        
        // mock authorization service
        authorizationService = mock(AuthorizationService.class);
        when(authorizationService.canUpdateCollection(user, collectionWithData)).thenReturn(false);
        when(authorizationService.canUpdateCollection(admin, collectionWithData)).thenReturn(true);
        
        assertTrue(authorizationService.canUpdateCollection(admin, collectionWithData));
        assertFalse(authorizationService.canUpdateCollection(user, collectionWithData));
        
        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext) servletCtx
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("authorizationService", authorizationService);
        springContext.getBeanFactory().registerSingleton("collectionBizService", collectionBizService);
        
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);
        
        // Put the collection attribute on both sessions
        registeredUserSession.setAttribute("collection", new Collection(collectionWithData));
        adminSession.setAttribute("collection", new Collection(collectionWithData));
    }
    
    /**
     * Test remove an existing Alternate id from the collection
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAlternateId() throws Exception {
        // Initialize the collection
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("displayCollectionUpdateForm");
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        String alternateId = collection.getAlternateIds().get(0);
        
        assertNotNull(alternateId);
        
        rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collection.getId());
        rt.addParameter("selectedAlternateIdIndex", Integer.toString(0));
        rt.execute("deleteAlternateId");
        
        assertNull("Expected forward URL to be null.", rt.getForwardUrl());
        assertEquals(
                "Expected redirect URL to be the collection form URL.",
                "//"
                        + UpdateCollectionActionBean.class.getAnnotation(
                                net.sourceforge.stripes.action.UrlBinding.class).value()
                        + "?displayCollectionUpdateForm=&collectionId="
                        + URLEncoder.encode(collection.getId(), "UTF-8"), rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        // Make sure the existing alternate id got deleted
        assertFalse("Expected contact to not be in list: " + alternateId + "' in '"
                + collection.getContactInfoList().toString() + "'.", collection.getAlternateIds().contains(alternateId));
        
        assertEquals("Expected collection title to be the same as it started.", this.collectionWithData.getTitle(),
                collection.getTitle());
        assertEquals("Expected collection summary to be the same as it started.", this.collectionWithData.getSummary(),
                collection.getSummary());
        assertEquals("Expected collection citable locator to be the same as it started.",
                this.collectionWithData.getCitableLocator(), collection.getCitableLocator());
        for (ContactInfo ci : this.collectionWithData.getContactInfoList()) {
            assertTrue(collection.getContactInfoList().contains(ci));
        }
        for (PersonName creator : this.collectionWithData.getCreators()) {
            assertTrue(collection.getCreators().contains(creator));
        }
        
    }
    
    /**
     * Test removing an existing creator from the collection
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveCreator() throws Exception {
        // Initialize the collection
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("displayCollectionUpdateForm");
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        PersonName creatorToBeRemoved = collection.getCreators().get(0);
        
        assertNotNull(creatorToBeRemoved);
        
        rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collection.getId());
        rt.addParameter("selectedCreatorIndex", Integer.toString(0));
        rt.execute("deleteCreator");
        
        assertNull("Expected forward URL to be null.", rt.getForwardUrl());
        assertEquals(
                "Expected redirect URL to be the collection form URL.",
                "//"
                        + UpdateCollectionActionBean.class.getAnnotation(
                                net.sourceforge.stripes.action.UrlBinding.class).value()
                        + "?displayCollectionUpdateForm=&collectionId="
                        + URLEncoder.encode(collection.getId(), "UTF-8"), rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        // Make sure the new contact got deleted
        assertFalse("Expected contact to not be in list: " + creatorToBeRemoved.getFamilyNames() + "' in '"
                + collection.getCreators().toString() + "'.", collection.getCreators().contains(creatorToBeRemoved));
        
        // check the rest of the fields
        assertEquals("Expected collection title to be the same as it started.", this.collectionWithData.getTitle(),
                collection.getTitle());
        assertEquals("Expected collection summary to be the same as it started.", this.collectionWithData.getSummary(),
                collection.getSummary());
        assertEquals("Expected collection citable locator to be the same as it started.",
                this.collectionWithData.getCitableLocator(), collection.getCitableLocator());
        for (String altId : this.collectionWithData.getAlternateIds()) {
            assertTrue(collection.getAlternateIds().contains(altId));
        }
        for (ContactInfo ci : this.collectionWithData.getContactInfoList()) {
            assertTrue(collection.getContactInfoList().contains(ci));
        }
    }
    
    /**
     * Test updating the collection as an instance admin. Expects success. Update to collection metadata fields is
     * considered successful when - the fields has update value before going to the CollectionBizService update method -
     * the fields retain their updated value after up a "roundtrip" update is made and are display appropriately on
     * CollectionDetails page - its membership remains the same
     */
    @Test
    public void testUpdateCollectionWithPermission() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("displayUpdateCollectionForm");
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        
        // changing collection to update
        String newAlternateId = "id:updated";
        
        collection.getAlternateIds().remove(0);
        collection.getAlternateIds().add(newAlternateId);
        collection.getContactInfoList().remove(0);
        collection.getCreators().add(
                new PersonName("Mr.", "New first names", "New middle names", "New Family names", ""));
        
        // Set up the post request with updated parameter
        rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        // Set singular fields
        rt.addParameter("collectionId", collection.getId());
        rt.addParameter("collection.title", collection.getTitle());
        rt.addParameter("collection.summary", collection.getSummary());
        rt.addParameter("collection.citableLocator", collection.getCitableLocator());
        rt.addParameter("collection.publicationDate", collection.getPublicationDate().toString());
        // Set alternate ids
        rt.addParameter("collection.alternateIds[0]", (String) collection.getAlternateIds().toArray()[0]);
        // Set creators
        // creator #1
        rt.addParameter("collection.creators[0].prefixes", creatorOne.getPrefixes());
        rt.addParameter("collection.creators[0].givenNames", creatorOne.getGivenNames());
        rt.addParameter("collection.creators[0].middleNames", creatorOne.getMiddleNames());
        rt.addParameter("collection.creators[0].familyNames", creatorOne.getFamilyNames());
        rt.addParameter("collection.creators[0].suffixes", creatorOne.getSuffixes());
        // creator #2
        rt.addParameter("collection.creators[1].prefixes", creatorTwo.getPrefixes());
        rt.addParameter("collection.creators[1].givenNames", creatorTwo.getGivenNames());
        rt.addParameter("collection.creators[1].middleNames", creatorTwo.getMiddleNames());
        rt.addParameter("collection.creators[1].familyNames", creatorTwo.getFamilyNames());
        rt.addParameter("collection.creators[1].suffixes", creatorTwo.getSuffixes());
        
        // set contact info
        rt.addParameter("collection.contactInfoList[0].name", contactInfoOne.getName());
        rt.addParameter("collection.contactInfoList[0].role", contactInfoOne.getRole());
        rt.addParameter("collection.contactInfoList[0].physicalAddress.streetAddress", contactInfoOne
                .getPhysicalAddress().getStreetAddress());
        rt.addParameter("collection.contactInfoList[0].physicalAddress.city", contactInfoOne.getPhysicalAddress()
                .getCity());
        rt.addParameter("collection.contactInfoList[0].physicalAddress.country", contactInfoOne.getPhysicalAddress()
                .getCountry());
        rt.addParameter("collection.contactInfoList[0].emailAddress", contactInfoOne.getEmailAddress());
        rt.addParameter("collection.contactInfoList[0].phoneNumber", contactInfoOne.getPhoneNumber());
        
        rt.execute("updateCollection");
        // Expect user to be forwarded to collection details page
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        // viewing collection details page
        rt = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, adminSession);
        rt.setParameter("selectedCollectionId", collection.getId());
        rt.execute("viewCollectionDetails");
        assertEquals(200, rt.getResponse().getStatus());
        Collection updatedCollection = rt.getActionBean(UserCollectionsActionBean.class).getCollection(
                collection.getId());
        assertEquals(collection, updatedCollection);
        assertFalse(collection.equals(this.collectionWithData));
    }
    
    /**
     * Test attempting to load the update form Expect return code 200 - the form should load publically, but the update
     * itself
     */
    @Test
    public void testUpdateCollectionWithNoPermission() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, registeredUserSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("displayUpdateCollectionForm");
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
    }
    
    // Make sure the starting info remains
    public void checkStartingInfo(Collection collection) throws Exception {
        assertEquals("Expected collection title to be the same as it started.", this.collectionWithData.getTitle(),
                collection.getTitle());
        assertEquals("Expected collection summary to be the same as it started.", this.collectionWithData.getSummary(),
                collection.getSummary());
        assertEquals("Expected collection citable locator to be the same as it started.",
                this.collectionWithData.getCitableLocator(), collection.getCitableLocator());
        for (String altId : this.collectionWithData.getAlternateIds()) {
            assertTrue(collection.getAlternateIds().contains(altId));
        }
        for (PersonName creator : this.collectionWithData.getCreators()) {
            assertTrue(collection.getCreators().contains(creator));
        }
        assertTrue("Expected starting contact to be in list: '" + contactInfoOne.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoOne));
    }
    
    @Test
    public void testDisplayCollectionUpdateForm() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("displayUpdateCollectionForm");
        
        assertNull("Expected redirect URL to be null.", rt.getRedirectUrl());
        assertEquals("Expected forward URL to be the collection form URL.",
                UpdateCollectionActionBean.COLLECTION_UPDATE_PATH, rt.getForwardUrl());
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        
        assertEquals(this.collectionWithData, collection);
    }
    
    @Test
    public void testSaveAndDoneContactInfo() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        ContactInfo contactInfoIn = new ContactInfo();
        Address addressIn = new Address();
        contactInfoIn.setName("Timmo Rider");
        contactInfoIn.setRole("King of the mountain");
        addressIn.setStreetAddress("2 CompuTrainer Dr.");
        addressIn.setCity("Boulder");
        addressIn.setZipCode("-9999");
        addressIn.setCountry("Cyclist country");
        contactInfoIn.setPhysicalAddress(addressIn);
        contactInfoIn.setEmailAddress("ThatMountainIsMine@ridingisfun.com");
        contactInfoIn.setPhoneNumber("1-800-NO-CELLP");
        
        rt.addParameter("contactInfo.name", contactInfoIn.getName());
        rt.addParameter("contactInfo.role", contactInfoIn.getRole());
        rt.addParameter("contactInfo.physicalAddress.streetAddress", contactInfoIn.getPhysicalAddress()
                .getStreetAddress());
        rt.addParameter("contactInfo.physicalAddress.city", contactInfoIn.getPhysicalAddress().getCity());
        rt.addParameter("contactInfo.physicalAddress.state", contactInfoIn.getPhysicalAddress().getState());
        rt.addParameter("contactInfo.physicalAddress.zipCode", contactInfoIn.getPhysicalAddress().getZipCode());
        rt.addParameter("contactInfo.physicalAddress.country", contactInfoIn.getPhysicalAddress().getCountry());
        rt.addParameter("contactInfo.emailAddress", contactInfoIn.getEmailAddress());
        rt.addParameter("contactInfo.phoneNumber", contactInfoIn.getPhoneNumber());
        rt.execute("saveAndDoneContactInfo");
        
        assertNull("Expected redirect URL to be null.", rt.getRedirectUrl());
        assertEquals(
                "Expected forward URL to be the collection form URL.",
                UpdateCollectionActionBean.COLLECTION_UPDATE_PATH + "?collectionId="
                        + URLEncoder.encode(collectionWithData.getId(), "UTF-8"), rt.getForwardUrl());
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        
        // Make sure the new contact made it
        assertTrue("Expected new contact to be in list: '" + contactInfoIn.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoIn));
        
        checkStartingInfo(collection);
    }
    
    @Test
    public void testSaveAndAddMoreContactInfo() throws Exception {
        ContactInfo contactInfoIn = new ContactInfo();
        Address addressIn = new Address();
        contactInfoIn.setName("Willard Brown");
        contactInfoIn.setRole("Couch Monitor");
        addressIn.setStreetAddress("Couch Arm way");
        addressIn.setCountry("Howell");
        contactInfoIn.setPhysicalAddress(addressIn);
        contactInfoIn.setEmailAddress("WillardRulesCouch@monitor.com");
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        
        rt.addParameter("contactInfo.name", contactInfoIn.getName());
        rt.addParameter("contactInfo.role", contactInfoIn.getRole());
        rt.addParameter("contactInfo.physicalAddress.streetAddress", contactInfoIn.getPhysicalAddress()
                .getStreetAddress());
        rt.addParameter("contactInfo.physicalAddress.city", contactInfoIn.getPhysicalAddress().getCity());
        rt.addParameter("contactInfo.physicalAddress.state", contactInfoIn.getPhysicalAddress().getState());
        rt.addParameter("contactInfo.physicalAddress.zipCode", contactInfoIn.getPhysicalAddress().getZipCode());
        rt.addParameter("contactInfo.physicalAddress.country", contactInfoIn.getPhysicalAddress().getCountry());
        rt.addParameter("contactInfo.emailAddress", contactInfoIn.getEmailAddress());
        rt.addParameter("contactInfo.phoneNumber", contactInfoIn.getPhoneNumber());
        rt.execute("saveAndAddMoreContactInfo");
        
        assertNull("Expected forward URL to be null.", rt.getForwardUrl());
        assertEquals(
                "Expected redirect URL to be the contact form URL.",
                "//"
                        + UpdateCollectionActionBean.class.getAnnotation(
                                net.sourceforge.stripes.action.UrlBinding.class).value()
                        + "?displayContactInfoForm=&collectionId="
                        + URLEncoder.encode(collectionWithData.getId(), "UTF-8"), rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        
        // Make sure the new contact made it
        assertTrue("Expected new contact to be in list: '" + contactInfoIn.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoIn));
        
        checkStartingInfo(collection);
    }
    
    @Test
    public void testDeleteContactInfo() throws Exception {
        
        // Initialize the collection
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collectionWithData.getId());
        rt.execute("displayCollectionUpdateForm");
        
        Collection collection = rt.getActionBean(UpdateCollectionActionBean.class).getCollection();
        ContactInfo contactInfo = collection.getContactInfoList().get(0);
        
        assertNotNull(contactInfo);
        
        rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", collection.getId());
        rt.addParameter("selectedContactInfoIndex", Integer.toString(0));
        rt.execute("deleteContactInfo");
        
        assertNull("Expected forward URL to be null.", rt.getForwardUrl());
        assertEquals(
                "Expected redirect URL to be the collection form URL.",
                "//"
                        + UpdateCollectionActionBean.class.getAnnotation(
                                net.sourceforge.stripes.action.UrlBinding.class).value()
                        + "?displayCollectionUpdateForm=&collectionId="
                        + URLEncoder.encode(collection.getId(), "UTF-8"), rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        // Make sure the new contact got deleted
        assertFalse("Expected contact to not be in list: " + contactInfo.getName() + "' in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfo));
        
        assertEquals("Expected collection title to be the same as it started.", this.collectionWithData.getTitle(),
                collection.getTitle());
        assertEquals("Expected collection summary to be the same as it started.", this.collectionWithData.getSummary(),
                collection.getSummary());
        assertEquals("Expected collection citable locator to be the same as it started.",
                this.collectionWithData.getCitableLocator(), collection.getCitableLocator());
        for (String altId : this.collectionWithData.getAlternateIds()) {
            assertTrue(collection.getAlternateIds().contains(altId));
        }
    }
    
    /**
     * Attempt to update non existing collection. Expects Exception
     */
    
    @Test
    public void testUpdateNonExistingCollection() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UpdateCollectionActionBean.class, adminSession);
        rt.addParameter("collectionId", "id:NonExistingCollection");
        rt.execute("displayUpdateCollectionForm");
        assertEquals("Expected status to be 404.", 404, rt.getResponse().getStatus());
    }
    
}
