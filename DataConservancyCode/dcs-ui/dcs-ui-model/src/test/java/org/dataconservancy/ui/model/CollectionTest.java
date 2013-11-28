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

package org.dataconservancy.ui.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class CollectionTest
        extends BaseUnitTest {

    @Autowired
    private Collection collectionTwo;

    @Autowired
    private Collection collectionThree;
    
    private Collection collectionFour;
    
    private Collection collectionFive;

    private List<ContactInfo> contactInfoList;

    private List<ContactInfo> differentContactInfoList;

    private List<MetadataFile> metadataFiles;

    private List<MetadataFile> differentMetadataFiles;

    private List<PersonName> creators;

    private List<PersonName> differentCreators;

    @Before
    public void setUp() {
        contactInfoList = new ArrayList<ContactInfo>();
        contactInfoList.add(new ContactInfo("Boss",
                                            "Monkey Little",
                                            "bananas@aremine.com",
                                            "555555555",
                                            new Address("10 H st.",
                                                        "Btown",
                                                        "MD",
                                                        "52695",
                                                        "USA")));
        contactInfoList.add(new ContactInfo("Boss2",
                                            "Monkey Little2",
                                            "banana2s@aremine.com",
                                            "555555555",
                                            new Address("12 H st.",
                                                        "Btown",
                                                        "MD",
                                                        "52895",
                                                        "USA")));

        differentContactInfoList = new ArrayList<ContactInfo>();
        differentContactInfoList.add(new ContactInfo("Different Boss",
                                                     "Monkey Little",
                                                     "bananas@aremine.com",
                                                     "555555555",
                                                     new Address("16 H st.",
                                                                 "Btown",
                                                                 "MD",
                                                                 "52895",
                                                                 "USA")));

        ArrayList<String> metadataFileIds = new ArrayList<String>();
        metadataFileIds.add("id1");
        metadataFileIds.add("id2");
      
        metadataFiles = new ArrayList<MetadataFile>();
        metadataFiles.add(new MetadataFile("id1",
                                           "fileOne",
                                           "XML version 1.0",
                                           "file",
                                           "\\",
                                           "MetadataId1",
                                           collectionWithData.id));
        metadataFiles.add(new MetadataFile("id2",
                                           "fileTwo",
                                           "FOO",
                                           "file",
                                           "c:\test\\",
                                           "MetadataId1",
                                           collectionWithData.id));

        creators = new ArrayList<PersonName>();
        creators.add(new PersonName("Mr.", "Test", "P.", "Person", "Jr."));
        String[] givenNames = {"Test", "Monkey"};
        String[] middleNames = {"Banana"};
        String[] familyNames = {"Primate", "Chimp"};
        creators.add(new PersonName("Mr.",
                                    givenNames,
                                    middleNames,
                                    familyNames,
                                    "Jr."));

        differentCreators = new ArrayList<PersonName>();
        differentCreators.add(new PersonName("Dr.",
                                             "Strange",
                                             "",
                                             "Love",
                                             "P.H.D."));


        collectionWithData.setContactInfoList(contactInfoList);
		collectionWithData.setCreators(creators);
        collectionWithData.setDepositorId(user.getId());

        collectionTwo.setTitle(collectionWithData.getTitle());
        collectionTwo.setSummary(collectionWithData.getSummary());
        collectionTwo.setCitableLocator(collectionWithData.getCitableLocator());
        collectionTwo.setContactInfoList(contactInfoList);
        collectionTwo.setId(collectionWithData.getId());
        collectionTwo.setPublicationDate(collectionWithData
                .getPublicationDate());
        collectionTwo.setAlternateIds(collectionWithData.getAlternateIds());
        collectionTwo.setCreators(creators);
        collectionTwo.setDepositorId(user.getId());
        collectionTwo.setDepositDate(collectionWithData.getDepositDate());
        collectionTwo.setParentProjectId(projectOne.getId());
        
        collectionThree.setTitle(collectionWithData.getTitle());
        collectionThree.setSummary(collectionWithData.getSummary());
        collectionThree.setCitableLocator(collectionWithData
                .getCitableLocator());
        collectionThree.setContactInfoList(contactInfoList);
        collectionThree.setId(collectionWithData.getId());
        collectionThree.setPublicationDate(collectionWithData
                .getPublicationDate());
        collectionThree.setAlternateIds(collectionWithData.getAlternateIds());
        collectionThree.setCreators(creators);
        collectionThree.setDepositorId(user.getId());
        collectionThree.setDepositDate(collectionWithData.getDepositDate());
        collectionThree.setParentProjectId(projectOne.getId());
        
        // Making a copy of collection three to use for collection four to test subCollection behavior.
        collectionFour = new Collection(collectionThree);
        
        collectionFive = new Collection();
        collectionFive.setId("collectionFive:id");
        collectionFive.setParentId(collectionFour.getId());
        collectionFive.setTitle("SubCollectionTitle!!");
        collectionFive.setSummary("Collection to be used as a Sub-collection to collection three.");
        collectionFive.setCitableLocator(collectionWithData.getCitableLocator());
        collectionFive.setContactInfoList(contactInfoList);
        collectionFive.setPublicationDate(collectionWithData.getPublicationDate());
        collectionFive.setAlternateIds(collectionWithData.getAlternateIds());
        collectionFive.setCreators(creators);
        collectionFive.setDepositorId(user.getId());
        collectionFive.setDepositDate(collectionWithData.getDepositDate());
        
        List<String> childrenIds = new ArrayList<String>();
        childrenIds.add(collectionFive.getId());
        collectionFour.setChildrenIds(childrenIds);
    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        //assertTrue(collectionWithData.equals(collectionWithData));
        assertFalse(collectionWithData.equals(collectionNoData));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(collectionWithData.equals(collectionTwo));
        assertTrue(collectionTwo.equals(collectionWithData));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(collectionWithData.equals(collectionTwo));
        assertTrue(collectionTwo.equals(collectionThree));
        assertTrue(collectionWithData.equals(collectionThree));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(collectionWithData.equals(collectionTwo));
        assertTrue(collectionWithData.equals(collectionTwo));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(collectionWithData.equals(null));
    }

    /**
     * Tests equality between Collection objects, collectionWithData created
     * using the copy constructor.
     */
    @Test
    public void testEqualityUsingCopyConstructor() {
        Collection copyOne = new Collection(collectionWithData);

        // transitive
        assertTrue(collectionWithData.equals(copyOne));
        assertTrue(copyOne.equals(collectionThree));
        assertTrue(collectionWithData.equals(collectionThree));

        // symmetric
        assertTrue(collectionWithData.equals(copyOne));
        assertTrue(copyOne.equals(collectionWithData));

        // consistent
        assertTrue(collectionWithData.equals(copyOne));
        assertTrue(collectionWithData.equals(copyOne));

        // nullity
        assertFalse(copyOne.equals(null));
    }

    @Test
    public void testSetOfCollectionsEqual() {
        Set<Collection> collections1 = new HashSet<Collection>();
        Set<Collection> collections2 = new HashSet<Collection>();

        collections1.add(collectionWithData);
        collections1.add(collectionNoData);

        collections2.add(collectionNoData);
        collections2.add(collectionWithData);

        assertEquals(collections1, collections2);
    }
    
    @Test
    public void testParentChildRelationship() {
        // Confirm that collectionFive is a subCollection.
        assertTrue(collectionFive.getParentId() != null);
        
        // Confirm that collectionFive belongs to a valid parent.
        assertEquals(collectionFive.getParentId(), collectionFour.getId());
        
        // Confirm that collectionFour references collectionFive as ac child.
        assertTrue(collectionFour.getChildrenIds().contains(collectionFive.getId()));
    }

}
