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
import java.util.List;

import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.junit.Before;
import org.junit.Test;

import org.joda.time.DateTime;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 5/17/12 Time: 11:29 AM To change
 * this template use File | Settings | File Templates.
 */
@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class BopTest
        extends BaseUnitTest {

    private Bop oneA = new Bop();

    private Bop oneB = new Bop();

    private Bop oneC = new Bop();

    private Bop different = new Bop();

    @Before
    public void setUp() {

        Collection col3 =
                new Collection("col3:title",
                               "col3:summary",
                               "col3:id",
                               "col3:citablelocator",
                               new DateTime(),
                               new ArrayList<ContactInfo>(),
//                               new ArrayList<String>(),
                               new ArrayList<String>(),
                               new ArrayList<PersonName>(),
                               "",
                               new DateTime(), null, projectOne.getId(),
                               new ArrayList<String>());

        user.setRegistrationStatus(RegistrationStatus.BLACK_LISTED);

        oneA.addCollection(collectionWithData, collectionNoData);
        oneA.addProject(projectOne, projectTwo);
        oneA.addPerson(admin, pendingUser);
        oneA.addDropboxModel(dropboxModel);

        oneB.addCollection(collectionWithData, collectionNoData);
        oneB.addProject(projectOne, projectTwo);
        oneB.addPerson(admin, pendingUser);
        oneB.addDropboxModel(dropboxModel);

        oneC.addCollection(collectionWithData, collectionNoData);
        oneC.addProject(projectOne, projectTwo);
        oneC.addPerson(admin, pendingUser);
        oneC.addDropboxModel(dropboxModel);

        Project proj3 = new Project();
        proj3.setId("1");
        List<String> numbers = new ArrayList<String>();
        numbers.add("Award1");
        numbers.add("Award2");
        proj3.setNumbers(numbers);
        proj3.setName("Award1Name");
        proj3.setDescription("This is award 1 description");
        proj3.setPublisher("This is publisher 1");

        DropboxModel dropboxModel2 = new DropboxModel();
        dropboxModel2.setDeleted(true);

        different.addCollection(col3);
        different.addProject(proj3);
        different.addPerson(user);
        different.addDropboxModel(dropboxModel2);

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(oneA.equals(oneA));
        assertFalse(oneA.equals(different));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(oneA.equals(oneC));
        assertTrue(oneC.equals(oneA));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(oneA.equals(oneB));
        assertTrue(oneB.equals(oneC));
        assertTrue(oneA.equals(oneC));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(oneA.equals(oneB));
        assertTrue(oneA.equals(oneB));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(oneA.equals(null));
    }

}
