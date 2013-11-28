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

package org.dataconservancy.ui.test.support.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.model.Address;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.test.support.BaseSpringAwareTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:/org/dataconservancy/ui/config/test-applicationContext.xml",
        "classpath*:/org/dataconservancy/ui/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/config/applicationContext.xml"})
public abstract class BaseModelTest extends BaseSpringAwareTest {
    
    protected Project projectOne;
    
    protected Project projectTwo;
    
    protected Collection collectionNoData;
    
    protected Collection collectionWithData;
    
    protected Collection collectionOne;
    
    protected DataItem dataItemOne;
    
    protected DataItem dataItemTwo;
    
    protected DataFile dataFileOne;
    
    protected final static String DATA_FILE_ONE_CONTENT = "test";
    
    protected DataFile dataFileTwo;
    
    protected final static String DATA_FILE_TWO_CONTENT = "test two";
    
    protected String collectionNoDataDepositID;
    
    protected String collectionWithDataDepositID;
    
    protected String collectionOneDepositID;
    
    protected String dataItemOneDepositID;
    
    protected String dataItemTwoDepositID;
    
    protected DateTime collectionNoDataDepositDate;
    
    protected DateTime collectionWithDataDepositDate;
    
    protected DateTime collectionOneDepositDate;
    
    protected DateTime dataItemOneDepositDate;
    
    protected DateTime dataItemTwoDepositDate;
    
    protected ContactInfo contactInfoOne;
    
    protected ContactInfo contactInfoTwo;
    
    protected DcsMetadataFormat metadataFormatOne;
    
    protected DcsMetadataFormat metadataFormatTwo;
    
    protected String metadataFileOneDepositID;
    protected String metadataFileTwoDepositID;
    
    protected MetadataFile metadataFileOne;
    
    protected final static String METADATA_FILE_ONE_CONTENT = "metadata one";
    
    protected MetadataFile metadataFileTwo;
    
    protected final static String METADATA_FILE_TWO_CONTENT = "metadata two";
    
    protected PersonName creatorOne;
    
    protected PersonName creatorTwo;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    protected Person user;
    
    @Autowired
    @Qualifier("adminUser")
    protected Person admin;
    
    @Autowired
    @Qualifier("defaultUser")
    protected Person unauthorizedUser;
    
    @Autowired
    @Qualifier("unapprovedRegisteredUser")
    protected Person pendingUser;
    
    protected Person newUser;
    
    @Before
    public final void baseSetup() throws IOException {
        projectOne = new Project();
        projectOne.setId("id:projectOne");
        List<String> numbers = new ArrayList<String>();
        numbers.add("1");
        numbers.add("2");
        projectOne.setNumbers(numbers);
        projectOne.setName("Project One");
        projectOne.setDescription("THIS IS Project 1 DESCRIPTION");
        projectOne.setPublisher("THIS IS PUBLISHER 1");
        projectOne.addPi(admin.getId());
        
        projectOne.setStartDate(new DateTime("2010-05-26"));
        projectOne.setEndDate(new DateTime("2016-05-26"));
        
        projectOne.setStorageAllocated(1000000000000L);
        projectOne.setStorageUsed(500000000000L);
        projectOne.setFundingEntity("Self");
        
        projectTwo = new Project();
        projectTwo.setId("id:projectTwo");
        List<String> numbers2 = new ArrayList<String>();
        numbers2.add("3");
        numbers2.add("4");
        projectTwo.setNumbers(numbers2);
        projectTwo.setName("Project Two");
        projectTwo.setDescription("THIS IS Project 2 DESCRIPTION");
        projectTwo.setPublisher("THIS IS PUBLISHER 2");
        projectTwo.addPi(admin.getId());
        
        projectTwo.setStartDate(new DateTime("2010-05-26"));
        projectTwo.setEndDate(new DateTime("2016-05-26"));
        
        projectTwo.setStorageAllocated(1000000000000L);
        projectTwo.setStorageUsed(500000000000L);
        projectTwo.setFundingEntity("Self");
        
        creatorOne = new PersonName("Dr.", "String Blue", "Wrinkles", "Beanert McGee", "Jr.");
        creatorTwo = new PersonName("Dr.", "Pinto", "Brown", "Beanert", "Sr.");
        
        collectionWithData = new Collection();
        collectionWithData.setId("id:collectionWithData");        
        collectionWithData.setParentProjectId(projectOne.getId());
        metadataFormatOne = new DcsMetadataFormat();
        metadataFormatOne.setId("dc:format:metadata/CGDSMFGDC");
        metadataFormatOne.setName("MetadataName");
        metadataFormatOne.setVersion("MetadataVersion");
        
        metadataFormatTwo = new DcsMetadataFormat();
        metadataFormatTwo.setId("dc:format:metadata/TaxonX");
        metadataFormatTwo.setName("New metadata format");
        metadataFormatTwo.setVersion("VersionTwo");
        
        java.io.File metadataTmp = java.io.File.createTempFile("testMetadataFile", null);
        metadataTmp.deleteOnExit();
        
        PrintWriter mdOut = new PrintWriter(metadataTmp);
        
        mdOut.println(METADATA_FILE_ONE_CONTENT);
        mdOut.close();
        
        metadataFileOne = new MetadataFile();
        metadataFileOne.setId("id:MetadataFileOne");
        metadataFileOne.setName("MetadataOne");
        metadataFileOne.setSource(metadataTmp.toURI().toURL().toExternalForm());
        metadataFileOne.setMetadataFormatId(metadataFormatOne.getId());
        metadataFileOne.setPath(metadataTmp.getParent());
        metadataFileOne.setFormat("FORMAT:ONE");
        metadataFileOne.setParentId(collectionWithData.getId());
        Resource r = new UrlResource(metadataFileOne.getSource());
        metadataFileOne.setSize(r.contentLength());
        
        java.io.File metadataTmpTwo = java.io.File.createTempFile("testMetadataFileTwo", null);
        metadataTmpTwo.deleteOnExit();
        
        PrintWriter mdOutTwo = new PrintWriter(metadataTmpTwo);
        
        mdOutTwo.println(METADATA_FILE_TWO_CONTENT);
        mdOutTwo.close();
        metadataFileTwo = new MetadataFile();
        metadataFileTwo.setId("id:MetadataFileTwo");
        metadataFileTwo.setName("MetadataTwo");
        metadataFileTwo.setSource(metadataTmp.toURI().toURL().toExternalForm());
        metadataFileTwo.setMetadataFormatId(metadataFormatTwo.getId());
        metadataFileTwo.setPath(metadataTmpTwo.getParent());
        metadataFileTwo.setFormat("FORMAT:TWO");
        metadataFileTwo.setParentId(collectionWithData.getId());
        r = new UrlResource(metadataFileTwo.getSource());
        metadataFileTwo.setSize(r.contentLength());
        
        contactInfoOne = new ContactInfo();
        contactInfoOne.setEmailAddress("Email@address.com");
        contactInfoOne.setName("Monkey See");
        contactInfoOne.setPhoneNumber("5555555555");
        contactInfoOne.setRole("bosses");
        
        Address address = new Address();
        address.setStreetAddress("1 Empire Circle");
        address.setCity("Galactic Republic");
        address.setCountry("Coruscant");
        contactInfoOne.setPhysicalAddress(address);
        contactInfoOne.setEmailAddress("iamevil@senate.gov");
        
        contactInfoTwo = new ContactInfo();
        contactInfoTwo.setEmailAddress("contact@address.com");
        contactInfoTwo.setName("Mokey Do");
        contactInfoTwo.setPhoneNumber("0987654321");
        contactInfoTwo.setRole("chimp");
        
        collectionWithData.setTitle("Collection with data");
        collectionWithData.setSummary("Long ago in a galaxy far far away...");
        collectionWithData.setCitableLocator("http://foo");
        collectionWithData.setPublicationDate(new DateTime(2013, 4, 23, 0, 0));
        collectionWithData.getAlternateIds().add("Alt. Collection");
        
        collectionWithData.addContactInfo(contactInfoOne);
        collectionWithData.addContactInfo(contactInfoTwo);
        collectionWithData.addCreator(creatorOne);
        collectionWithData.addCreator(creatorTwo);

        
        // Set the deposit date on the object in this form, to match the ui.
        collectionWithData.setDepositDate(new DateTime(2013, 4, 24, 0, 0));
        collectionWithData.setDepositorId(admin.getId());
        
        collectionNoData = new Collection();
        collectionNoData.setId("id:collectionNoData");
        collectionNoData.setTitle("Collection No Data");
        collectionNoData.setSummary("Empty collectionWithData no data");
        collectionNoData.setPublicationDate(new DateTime(2012, 4, 23, 0, 0));
        collectionNoData.setDepositDate(new DateTime(2012, 4, 24, 0, 0));
        collectionNoData.setParentProjectId(projectOne.getId());
        
        collectionOne = new Collection();
        collectionOne.setId("id:CollectionOne");
        collectionOne.setTitle("Collection");
        collectionOne.setSummary("Collection with limited metadata and not attached to any objects");
        collectionOne.setDepositDate(new DateTime(2012, 4, 24, 0, 0));
        collectionOne.setParentProjectId(projectOne.getId());
        
        collectionWithDataDepositDate = DateTime.now();
        
        collectionNoDataDepositDate = DateTime.now();
        
        collectionOneDepositDate = DateTime.now();
        
        dataItemOne = new DataItem();
        dataItemOne.setName("Data Item One");
        dataItemOne.setDescription("data item one description");
        dataItemOne.setId("id:dataItemOne");
        dataItemOne.setDepositDate(new DateTime(2012, 4, 24, 0, 0));
        dataItemOne.setParentId(collectionWithData.getId());
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        dataFileOne = new DataFile();
        dataFileOne.setId("id:dataFileOne");
        dataFileOne.setParentId(dataItemOne.getId());
        dataFileOne.setSource(tmp.toURI().toURL().toExternalForm());
        dataFileOne.setName("Data file one");
        
        r = new UrlResource(dataFileOne.getSource());
        dataFileOne.setSize(r.contentLength());
        dataFileOne.setPath(tmp.getParent());
        dataItemOne.addFile(dataFileOne);
        
        dataItemTwo = new DataItem();
        dataItemTwo.setName("Data Item Two");
        dataItemTwo.setDescription("data item two description");
        dataItemTwo.setId("id:dataItemTwo");
        dataItemTwo.setParentId(collectionWithData.getId());
        java.io.File tmpTwo = java.io.File.createTempFile("testFileTwo", null);
        tmpTwo.deleteOnExit();
        
        out = new PrintWriter(tmpTwo);
        
        out.println(DATA_FILE_TWO_CONTENT);
        out.close();
        
        dataFileTwo = new DataFile();
        dataFileTwo.setId("id:dataFileTwo");
        dataFileTwo.setParentId(dataItemTwo.getId());
        dataFileTwo.setSource(tmpTwo.toURI().toURL().toExternalForm());
        dataFileTwo.setName("Data file two");
        
        Resource rTwo = new UrlResource(dataFileTwo.getSource());
        dataFileTwo.setSize(rTwo.contentLength());
        dataFileTwo.setPath(tmpTwo.getParent());
        dataItemTwo.addFile(dataFileTwo);
        dataItemTwo.setDepositDate(new DateTime(2013, 4, 24, 0, 0));
        
        dataItemOneDepositDate = DateTime.now();
        
        // Create a new user not in the system to use for adding user tests
        newUser = new Person();
        newUser.setId("id:newUser");
        newUser.setFirstNames("John Jones");
        newUser.setLastNames("Doe Does");
        newUser.setPrefix("Mr.");
        newUser.setSuffix("II");
        newUser.setMiddleNames("Jack Jacky");
        newUser.setPreferredPubName("J. Doe");
        newUser.setPassword("password");
        newUser.setEmailAddress("jdoe1@jhu.edu");
        newUser.setPhoneNumber("1234567890");
        newUser.setJobTitle("New Job Title");
        newUser.setDepartment("New Department");
        newUser.setCity("Baltimore");
        newUser.setState("Maryland");
        newUser.setInstCompany("New Institution/Company");
        newUser.setInstCompanyWebsite("www.NewInstitutionCompany.com");
        newUser.setBio("Some bio for the user.");
        newUser.setWebsite("www.somewebsite.com");
        newUser.setRegistrationStatus(RegistrationStatus.APPROVED);
        newUser.setExternalStorageLinked(false);
        newUser.setDropboxAppKey("SomeKey");
        newUser.setDropboxAppSecret("SomeSecret");
    }
}
