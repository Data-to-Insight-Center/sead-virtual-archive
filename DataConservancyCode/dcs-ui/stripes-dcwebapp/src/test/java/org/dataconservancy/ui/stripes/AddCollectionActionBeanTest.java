/*
 * Copyright 2013 Johns Hopkins University
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

import static org.dataconservancy.ui.model.Relationship.RelType.ACCEPTS_DEPOSIT;
import static org.dataconservancy.ui.model.Relationship.RelType.AGGREGATES;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_AGGREGATED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_DEPOSITOR_FOR;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;

import java.net.URLEncoder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.apache.commons.io.FileUtils;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.ArchiveDepositInfoDAO;
import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.dao.RelationshipDAO;
import org.dataconservancy.ui.dcpmap.DcpMapper;
import org.dataconservancy.ui.model.Address;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Relationship;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.DepositDocumentResolver;
import org.dataconservancy.ui.services.InMemoryArchiveServiceImpl;
import org.dataconservancy.ui.services.MockArchiveUtil;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.dataconservancy.ui.services.MockArchiveDepositInfoDAO;

/**
 * Tests for the AddCollectionActionBean
 **/
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
public class AddCollectionActionBeanTest extends org.dataconservancy.ui.stripes.BaseActionBeanTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private RelationshipDAO relDao;
    
    @Autowired
    private ProjectDAO projectDao;
    
    @Autowired
    private ArchiveService archiveService;
    
    @Autowired
    private JdbcTemplate template;

    @Autowired
    private DcpMapper mockDatasetMapper;

    @Autowired
    private Profile dataItemProfile;

    @Autowired
    private DcpMapper mockCollectionMapper;

    @Autowired
    private DcpMapper mockMetadataFileMapper;

    @Autowired
    @Qualifier("mockDataFileMapper")
    private DcpMapper mockDatafileMapper;
    
    @Autowired
    private MockArchiveDepositInfoDAO mockArchiveDepositInfoDAO;

    @Autowired
    private org.dataconservancy.profile.api.DcpMapper<RegistryEntry<DcsMetadataFormat>> metadataFormatRegistryEntryMapper;

    @Autowired
    private DepositDocumentResolver mockDepositDocumentResolver;

    @Autowired
    private MockArchiveUtil mockArchiveUtil;

    @Autowired
    @Qualifier("uiIdService")
    private IdService uiIdService;

    private MockHttpSession userSession;
    
    private MockHttpSession adminSession;
    
    private Collection modifiedCollection;
    
    private Collection subCollection;
    
    /**
     * Initialize the mock http session with authenticated user credentials. Tests that re-use this mock session will be
     * already logged in.
     */
    @Before
    public void setUpMockttpSessions() throws Exception {
        
        // Mock a session for a registered, authorized user.
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) userSession
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
        
        modifiedCollection = new Collection();
        modifiedCollection.setId("collectionWithData:/1");
        modifiedCollection.setTitle("Star Wars 2.0");
        modifiedCollection.setSummary("In space...");
        modifiedCollection.setCitableLocator("Nowhere");
        modifiedCollection.setPublicationDate(DateTime.now());
        modifiedCollection.getAlternateIds().add("iWars");
        modifiedCollection.getAlternateIds().add("ID:/2");
        modifiedCollection.addContactInfo(contactInfoOne);
        modifiedCollection.addCreator(creatorOne);
        
        collectionOne.setId("newID");
        collectionOne.addContactInfo(contactInfoOne);
        collectionOne.addCreator(creatorOne);
        
        subCollection = new Collection();
        subCollection.setId("SubCollectionId");
        subCollection.setTitle("Child collection title");
        subCollection.setSummary("Child collection summary.");
        subCollection.addCreator(creatorOne);

        // Put the collection attribute on both sessions
        userSession.setAttribute("collection", new Collection(collectionOne));
        adminSession.setAttribute("collection", new Collection(collectionOne));
    }
    
    @After
    public void emptyProjectDatabase() {
        template.execute("DELETE FROM PROJECT");
    }
    
    // Make sure the starting info remains
    public void checkStartingInfo(Collection collection) throws Exception {
        assertEquals("Expected collection title to be the same as it started.", collectionOne.getTitle(),
                collection.getTitle());
        assertEquals("Expected collection summary to be the same as it started.", collectionOne.getSummary(),
                collection.getSummary());
        assertEquals("Expected collection citable locator to be the same as it started.",
                collectionOne.getCitableLocator(), collection.getCitableLocator());
        assertEquals("Expected collection alternate ids to be the same as it started.",
                collectionOne.getAlternateIds(), collection.getAlternateIds());
        assertTrue("Expected starting contact to be in list: '" + contactInfoOne.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoOne));
    }
    
    // Make sure modified info happens properly
    public void checkModifiedInfo(Collection collection) throws Exception {
        assertEquals("Expected collection title to be modified.", modifiedCollection.getTitle(), collection.getTitle());
        assertEquals("Expected collection summary to be modified.", modifiedCollection.getSummary(),
                collection.getSummary());
        assertEquals("Expected collection citable locator to be modified.", modifiedCollection.getCitableLocator(),
                collection.getCitableLocator());
        
        // The CollectionMapper.toDcp(...) method does not preserve the ordering of alternate IDs when the
        // collectionWithData
        // is persisted in the archive. So after you deposit a Collection to the archive, the retrieved Collection
        // may not have the same order of alternate IDs as the deposited Collection. Therefore you can't check
        // equality between two lists, but you can between two sets.
        final Set<String> modifiedAlternateIds = new HashSet<String>();
        final Set<String> alternateIds = new HashSet<String>();
        modifiedAlternateIds.addAll(modifiedCollection.getAlternateIds());
        alternateIds.addAll(collection.getAlternateIds());
        assertEquals("Expected collection alternate ids to be modified.", modifiedAlternateIds, alternateIds);
        
        assertTrue("Expected starting contact to be in list: '" + contactInfoOne.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoOne));
        
        // Ensure the creator is replaced not appended
        assertEquals("Expected New Creator to be added: ", 1, collection.getCreators().size());
        
        // Make sure the new creator is added to the collection
        assertEquals("Expected New Creator to be modified creator: ", modifiedCollection.getCreators().get(0),
                collection.getCreators().get(0));
        
        // Ensure the fields were properly split
        assertEquals("Expected Family Name to be two names: ", 2, collection.getCreators().get(0)
                .getFamilyNamesAsArray().length);
    }
    
    @Test
    public void testSaveAndDoneContactInfo() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
        ContactInfo contactInfoIn = new ContactInfo();
        Address addressIn = new Address();
        contactInfoIn.setName("Darth Vader");
        contactInfoIn.setRole("Sith Lord");
        addressIn.setStreetAddress("1 Big Chair Drive");
        addressIn.setCity("Control Center");
        addressIn.setZipCode("666");
        addressIn.setCountry("Death Star");
        contactInfoIn.setPhysicalAddress(addressIn);
        contactInfoIn.setEmailAddress("lukeiamyourfather@sith.com");
        contactInfoIn.setPhoneNumber("555-666-7777");
        
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
        assertEquals("Expected forward URL to be the collection form URL.",
                AddCollectionActionBean.COLLECTION_ADD_PATH, rt.getForwardUrl());
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
        
        // assertNull("Expected forward URL to be null.", rt.getForwardUrl());
        // assertEquals("Expected redirect URL to be the collection add URL.", "//" +
        // AddCollectionActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value() +
        // "?displayCollectionForm=", rt.getRedirectUrl());
        // assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        Collection collection = rt.getActionBean(AddCollectionActionBean.class).getCollection();
        
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
        contactInfoIn.setName("Darth Maul");
        contactInfoIn.setRole("Sith Henchperson");
        addressIn.setStreetAddress("1 Evil Face Way");
        addressIn.setCountry("Many Places at Once");
        contactInfoIn.setPhysicalAddress(addressIn);
        contactInfoIn.setEmailAddress("mysaberhastwoends@sith.com");
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
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
        assertEquals("Expected redirect URL to be the contact form URL.", "//"
                + AddCollectionActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                + "?displayContactInfoForm=", rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        Collection collection = rt.getActionBean(AddCollectionActionBean.class).getCollection();
        
        // Make sure the new contact made it
        assertTrue("Expected new contact to be in list: '" + contactInfoIn.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoIn));
        
        checkStartingInfo(collection);
    }
    
    @Test
    public void testDisplayCollectionForm() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
        rt.execute("displayCollectionForm");
        
        assertNull("Expected redirect URL to be null.", rt.getRedirectUrl());
        assertEquals("Expected forward URL to be the collection form URL.",
                AddCollectionActionBean.COLLECTION_ADD_PATH, rt.getForwardUrl());
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
    }
    
    @Test
    public void testDisplayContactInfoForm() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
        rt.addParameter("collection.title", modifiedCollection.getTitle());
        rt.addParameter("collection.summary", modifiedCollection.getSummary());
        rt.addParameter("collection.citableLocator", modifiedCollection.getCitableLocator());
        rt.addParameter("collection.publicationDate", modifiedCollection.getPublicationDate().toString());
        rt.addParameter("collection.alternateIds[0]", (String) modifiedCollection.getAlternateIds().toArray()[0]);
        rt.addParameter("collection.alternateIds[1]", (String) modifiedCollection.getAlternateIds().toArray()[1]);
        rt.addParameter("collection.creators[0].prefixes", modifiedCollection.getCreators().get(0).getPrefixes());
        rt.addParameter("collection.creators[0].givenNames", modifiedCollection.getCreators().get(0).getGivenNames());
        rt.addParameter("collection.creators[0].middleNames", modifiedCollection.getCreators().get(0).getMiddleNames());
        rt.addParameter("collection.creators[0].familyNames", modifiedCollection.getCreators().get(0).getFamilyNames());
        rt.addParameter("collection.creators[0].suffixes", modifiedCollection.getCreators().get(0).getSuffixes());
        rt.execute("displayContactInfoForm");
        
        assertNull("Expected redirect URL to be null.", rt.getRedirectUrl());
        assertEquals("Expected forward URL to be the contact form URL.",
                AddCollectionActionBean.COLLECTION_CONTACT_INFO_PATH, rt.getForwardUrl());
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
        checkModifiedInfo(rt.getActionBean(AddCollectionActionBean.class).getCollection());
    }
    
    @Test
    public void testDisplayMetadataFileForm() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
        rt.addParameter("collection.title", modifiedCollection.getTitle());
        rt.addParameter("collection.summary", modifiedCollection.getSummary());
        rt.addParameter("collection.citableLocator", modifiedCollection.getCitableLocator());
        rt.addParameter("collection.publicationDate", modifiedCollection.getPublicationDate().toString());
        rt.addParameter("collection.alternateIds[0]", (String) modifiedCollection.getAlternateIds().toArray()[0]);
        rt.addParameter("collection.alternateIds[1]", (String) modifiedCollection.getAlternateIds().toArray()[1]);
        rt.addParameter("collection.creators[0].prefixes", modifiedCollection.getCreators().get(0).getPrefixes());
        rt.addParameter("collection.creators[0].givenNames", modifiedCollection.getCreators().get(0).getGivenNames());
        rt.addParameter("collection.creators[0].middleNames", modifiedCollection.getCreators().get(0).getMiddleNames());
        rt.addParameter("collection.creators[0].familyNames", modifiedCollection.getCreators().get(0).getFamilyNames());
        rt.addParameter("collection.creators[0].suffixes", modifiedCollection.getCreators().get(0).getSuffixes());
        rt.execute("displayMetadataFileForm");
        
        assertNull("Expected redirect URL to be null.", rt.getRedirectUrl());
        assertEquals("Expected forward URL to be the metadata form URL.",
                AddCollectionActionBean.COLLECTION_METADATA_FILE_PATH, rt.getForwardUrl());
        assertEquals("Expected status to be 200.", 200, rt.getResponse().getStatus());
        
        checkModifiedInfo(rt.getActionBean(AddCollectionActionBean.class).getCollection());
    }
    
    @Test
    public void testDeleteContactInfo() throws Exception {
        ContactInfo contactInfoIn = new ContactInfo();
        Address addressIn = new Address();
        contactInfoIn.setName("Han Solo");
        contactInfoIn.setRole("Pilot");
        addressIn.setStreetAddress("Millennium Falcon");
        addressIn.setCountry("Space");
        contactInfoIn.setPhysicalAddress(addressIn);
        contactInfoIn.setEmailAddress("ishotfirst@thebar.info");
        
        // Initialize the collection
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
        rt.execute("displayCollectionForm");
        
        Collection collection = rt.getActionBean(AddCollectionActionBean.class).getCollection();
        collection.getContactInfoList().add(contactInfoIn);
        
        // Make sure that the new contact got inserted
        assertTrue("Expected new contact to be in list: '" + contactInfoIn.getName() + "' not in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoIn));
        int index = collection.getContactInfoList().indexOf(contactInfoIn);
        
        rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, userSession);
        rt.addParameter("contactInfoIndex", Integer.toString(index));
        rt.execute("deleteContactInfo");
        
        assertNull("Expected forward URL to be null.", rt.getForwardUrl());
        assertEquals("Expected redirect URL to be the collection form URL.", "//"
                + AddCollectionActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                + "?displayCollectionForm=", rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        // Make sure the new contact got deleted
        assertFalse("Expected new contact to not be in list: " + contactInfoIn.getName() + "' in '"
                + collection.getContactInfoList().toString() + "'.",
                collection.getContactInfoList().contains(contactInfoIn));
        
        checkStartingInfo(collection);
    }
    
    @Test
    public void testAddCollection() throws Exception {
        
        // Fix up the collaborating Project
        Project project = new Project();
        project.setId("project:/1");
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        
        adminSession.setAttribute("projectId", project.getId());
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);
        rt.addParameter("collection.title", modifiedCollection.getTitle());
        rt.addParameter("collection.summary", modifiedCollection.getSummary());
        rt.addParameter("collection.citableLocator", modifiedCollection.getCitableLocator());
        rt.addParameter("collection.publicationDate", modifiedCollection.getPublicationDate().toString());
        rt.addParameter("collection.alternateIds[0]", (String) modifiedCollection.getAlternateIds().toArray()[0]);
        rt.addParameter("collection.alternateIds[1]", (String) modifiedCollection.getAlternateIds().toArray()[1]);
        rt.addParameter("collection.creators[0].prefixes", modifiedCollection.getCreators().get(0).getPrefixes());
        rt.addParameter("collection.creators[0].givenNames", modifiedCollection.getCreators().get(0).getGivenNames());
        rt.addParameter("collection.creators[0].middleNames", modifiedCollection.getCreators().get(0).getMiddleNames());
        rt.addParameter("collection.creators[0].familyNames", modifiedCollection.getCreators().get(0).getFamilyNames());
        rt.addParameter("collection.creators[0].suffixes", modifiedCollection.getCreators().get(0).getSuffixes());
        
        // Create the Collection and deposit the newly created Collection in the archive.
        rt.execute("addCollection");
        
        assertNull("Expected forward URL to be null but it was <" + rt.getForwardUrl() + ">.", rt.getForwardUrl());
        assertEquals("Expected redirect URL to be the view project URL.",
                "//" + ProjectActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                        + "?viewUserProject=&selectedProjectId=" + URLEncoder.encode(project.getId(), "UTF-8"),
                rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        String collectionId = rt.getActionBean(AddCollectionActionBean.class).getCollectionId();
        assertNotNull("Expected collection ID to be non-null.", collectionId);
        
        archiveService.pollArchive();
        
        final List<ArchiveDepositInfo> depositInfoList = archiveService.listDepositInfo(collectionId, null);
        assertEquals("Expected a single ArchiveDepositInfo from a single deposit transaction with the archive.", 1,
                depositInfoList.size());
        String depositId = depositInfoList.get(0).getDepositId();
        assertNotNull("Deposit ID from ArchiveDepositInfo should not be null.", depositId);
        
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(depositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection retrievedCollection = null;
        if (resultIter.hasNext()) {
            retrievedCollection = resultIter.next();
        }
        assertNotNull(retrievedCollection);
        assertNotNull("Expected retrieved collection to be non-null", retrievedCollection);
        assertEquals(
                "Expected the business id of the collection to remain unchanged after being deposited in the archive.",
                collectionId, retrievedCollection.getId());
        
        checkModifiedInfo(retrievedCollection);
    }
    
    /**
     * Verify that a collection can be added using an ID supplied by the client.
     * 
     * @throws Exception
     */
    @Test
    public void testAddCollectionWithSuppliedId() throws Exception {
        
        // Fix up the collaborating Project
        final Project project = new Project();
        project.setId("project:/1");
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        
        adminSession.setAttribute("projectId", project.getId());
        
        // The Collection we are adding; we supply an ID
        final String title = "Foo Collection";
        final String summary = "This is a sample collection.";
        final String id = "foo";
        
        int collectionCount = archiveService.listCollections(null).size();
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);
        rt.addParameter("collection.id", id);
        rt.addParameter("collection.title", title);
        rt.addParameter("collection.summary", summary);
        rt.addParameter("collection.creators[0].familyNames", "Creator");
        
        // Create the Collection and deposit the newly created Collection in the archive.
        rt.execute("addCollection");
        
        archiveService.pollArchive();
        assertEquals("Expected one Collection in the archive!", collectionCount + 1,
                archiveService.listCollections(null).size());
        final String depositId = archiveService.listCollections(null).get(0);
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(depositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection createdCollection = null;
        if (resultIter.hasNext()) {
            createdCollection = resultIter.next();
        }
        assertNotNull(createdCollection);
        
        assertEquals("Expected created collection to have identifier " + id + " but was " + createdCollection.getId(),
                id, createdCollection.getId());
    }
    
    /**
     * Verify that a collection can be added without an ID, and that the server will generate one.
     * 
     * @throws Exception
     */
    @Test
    public void testAddCollectionServerGeneratedId() throws Exception {
        
        // Fix up the collaborating Project
        final Project project = new Project();
        project.setId("project:/1");
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        
        adminSession.setAttribute("projectId", project.getId());
        
        int collectionCount = archiveService.listCollections(null).size();
        
        // The Collection we are adding; we do not supply an ID.
        final String title = "Foo Collection";
        final String summary = "This is a sample collection.";
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);
        rt.addParameter("collection.title", title);
        rt.addParameter("collection.summary", summary);
        rt.addParameter("collection.creators[0].familyNames", "Creator");
        
        // Create the Collection and deposit the newly created Collection in the archive.
        rt.execute("addCollection");
        
        archiveService.pollArchive();
        assertEquals("Expected one Collection in the archive!", collectionCount + 1,
                archiveService.listCollections(null).size());
        final String depositId = archiveService.listCollections(null).get(0);
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(depositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection createdCollection = null;
        if (resultIter.hasNext()) {
            createdCollection = resultIter.next();
        }
        assertNotNull(createdCollection);
        
        assertTrue("Expected created collection to have a server-side generated identifier!",
                createdCollection.getId() != null && !createdCollection.getId().isEmpty());
    }
    
    /**
     * Tests that if cancel is pressed when adding a collection the collection isn't added the user is redirected to the
     * project details page.
     */
    @Test
    public void testAddCollectionCanceled() throws Exception {
        // Fix up the collaborating Project
        Project project = new Project();
        project.setId("project:/1");
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        
        adminSession.setAttribute("projectId", project.getId());
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);
        rt.addParameter("collection.title", modifiedCollection.getTitle());
        rt.addParameter("collection.summary", modifiedCollection.getSummary());
        
        int collectionCount = archiveService.listCollections(null).size();
        
        // Cancel the form
        rt.execute("cancel");
        
        assertNotNull("Expected forward URL to be null.", rt.getRedirectUrl());
        assertEquals("Expected redirect URL to be the view project URL.",
                "//" + ProjectActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                        + "?viewUserProject=&selectedProjectId=" + URLEncoder.encode(project.getId(), "UTF-8"),
                rt.getRedirectUrl());
        assertEquals("Expected status to be 302.", 302, rt.getResponse().getStatus());
        
        archiveService.pollArchive();
        
        assertEquals("Expected no objects in the archive!", collectionCount, archiveService.listCollections(null)
                .size());
    }
    
    /**
     * This test insures that when a Collection is added, the correct relationships are created between the creator of
     * the collection and the collection itself (tested using the {@link RelationshipDAO} but the action bean should be
     * using the {@link org.dataconservancy.ui.services.RelationshipService}).
     */
    @Test
    public void testAddCollectionCreatesTheProperRelationships() throws Exception {
        
        // Fix up the collaborating project
        Project project = new Project();
        final String projectId = "project:/1";
        project.setId(projectId);
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        
        adminSession.setAttribute("projectId", project.getId());
        
        // Assert that no relationships currently exist for the admin or the project
        assertEquals(0, relDao.getRelations(admin.getEmailAddress()).size());
        assertEquals(0, relDao.getRelations(projectId).size());
        
        // Can't do any assertions about the Collection relationship, because the ActionBean is
        // minting the Collection ID, and it hasn't executed yet.
        
        // Test fixup
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);
        rt.addParameter("collection.title", modifiedCollection.getTitle());
        rt.addParameter("collection.summary", modifiedCollection.getSummary());
        rt.addParameter("collection.citableLocator", modifiedCollection.getCitableLocator());
        rt.addParameter("collection.publicationDate", modifiedCollection.getPublicationDate().toString());
        rt.addParameter("collection.alternateIds[0]", (String) modifiedCollection.getAlternateIds().toArray()[0]);
        rt.addParameter("collection.creators[0].familyNames", "Creator");
        
        // Add the Collection (and associated relationships)
        rt.execute("addCollection");
        
        final String collectionId = rt.getActionBean(AddCollectionActionBean.class).getCollectionId();
        assertNotNull("Expected collection ID to be non-null.", collectionId);
        
        // Expected relationships to be added in the RelationshipService
        final Relationship expectedObverseUserRel = new Relationship(admin.getId(), collectionId, IS_DEPOSITOR_FOR);
        final Relationship expectedInverseUserRel = new Relationship(collectionId, admin.getId(), ACCEPTS_DEPOSIT);
        final Relationship expectedObverseProjectRel = new Relationship(projectId, collectionId, AGGREGATES);
        final Relationship expectedInverseProjectRel = new Relationship(collectionId, projectId, IS_AGGREGATED_BY);
        
        // Verify the correct relationships were added.
        assertEquals("Expected four relationships to be added.", 4, relDao.getRelations(collectionId).size());
        assertTrue("Expected " + expectedObverseUserRel + " to be added.",
                relDao.getRelations(collectionId, IS_DEPOSITOR_FOR).contains(expectedObverseUserRel));
        assertTrue("Expected " + expectedInverseUserRel + " to be added.",
                relDao.getRelations(collectionId, ACCEPTS_DEPOSIT).contains(expectedInverseUserRel));
        assertTrue("Expected " + expectedObverseProjectRel + " to be added.",
                relDao.getRelations(collectionId, AGGREGATES).contains(expectedObverseProjectRel));
        assertTrue("Expected " + expectedInverseProjectRel + " to be added.",
                relDao.getRelations(collectionId, IS_AGGREGATED_BY).contains(expectedInverseProjectRel));
    }

    @Test
    public void testPollExpirationOnDepositReturnsCorrectHttpErrorCodeAndMessage() throws Exception {

        // Construct our own ArchiveService instance; this is because Spring will return a Proxied instance
        // of ArchiveService, which Mockito won't be able to spy(...) on.
        final ArchiveService notProxiedArchiveService = new InMemoryArchiveServiceImpl(mockDatasetMapper,
                                                            dataItemProfile,
                                                            mockCollectionMapper,
                                                            mockMetadataFileMapper,
                                                            mockDatafileMapper,
                                                            metadataFormatRegistryEntryMapper,
                                                            mockDepositDocumentResolver,
                                                            mockArchiveUtil,
                                                            uiIdService,
                                                            false, mockArchiveDepositInfoDAO);

        // Create a mock ArchiveService that behaves like the real ArchiveService, except we override the
        // pollArchive() method to do nothing.  Effectively this means that deposit status will never be updated.
        final ArchiveService pollsForever = spy(notProxiedArchiveService);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                log.debug("Invoking a mock ArchiveService.pollArchive(); impl is a no-op, so deposit statuses will " +
                        "never be updated.");
                return null;
            }
        }).when(pollsForever).pollArchive();

        // Inject the mocked archiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext) servletCtx
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("archiveService", pollsForever);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        // Fix up the collaborating Project
        Project project = new Project();
        project.setId("project:/2341");
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        adminSession.setAttribute("projectId", project.getId());

        MockRoundtrip rt = new MockRoundtrip(servletCtx, AddCollectionActionBean.class, adminSession);

        rt.addParameter("collection.title", modifiedCollection.getTitle());
        rt.addParameter("collection.summary", modifiedCollection.getSummary());
        rt.addParameter("collection.citableLocator", modifiedCollection.getCitableLocator());
        rt.addParameter("collection.publicationDate", modifiedCollection.getPublicationDate().toString());
        rt.addParameter("collection.alternateIds[0]", (String) modifiedCollection.getAlternateIds().toArray()[0]);
        rt.addParameter("collection.alternateIds[1]", (String) modifiedCollection.getAlternateIds().toArray()[1]);
        rt.addParameter("collection.creators[0].prefixes", modifiedCollection.getCreators().get(0).getPrefixes());
        rt.addParameter("collection.creators[0].givenNames", modifiedCollection.getCreators().get(0).getGivenNames());
        rt.addParameter("collection.creators[0].middleNames", modifiedCollection.getCreators().get(0).getMiddleNames());
        rt.addParameter("collection.creators[0].familyNames", modifiedCollection.getCreators().get(0).getFamilyNames());
        rt.addParameter("collection.creators[0].suffixes", modifiedCollection.getCreators().get(0).getSuffixes());

        // Attempt to deposit the collection; this should fail because the pollForever archive service will never
        // update the deposit status.
        rt.execute("addCollection");

        final MockHttpServletResponse response = rt.getResponse();
        assertEquals(302, response.getStatus());
    }
    
}
