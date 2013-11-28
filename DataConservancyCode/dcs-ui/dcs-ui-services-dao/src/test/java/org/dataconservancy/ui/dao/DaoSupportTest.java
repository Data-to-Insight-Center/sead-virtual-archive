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

package org.dataconservancy.ui.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 */
public class DaoSupportTest extends BaseDaoTest {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private PersonDAOJbdcImpl personDao;

    @Autowired
    private ProjectDAOJdbcImpl projectDao;

    @Test
    public void testSimplePerson() throws Exception {
        Person p = new Person();
        p.setId("id:dellcomputer");
        p.setFirstNames("Dell");
        p.setLastNames("Computer");
        p.setPrefix("Mr.");
        p.setSuffix("II");
        p.setMiddleNames("Spiderish");
        p.setPreferredPubName("D. Computer");
        p.setBio("Some bio for the user.");
        p.setWebsite("www.somewebsite.com");
        p.setEmailAddress("foo@boo.com");
        p.setPassword("Mypass");
        p.setPhoneNumber("555-555-5555");
        p.setJobTitle("Dell Scientist");
        p.setDepartment("Dell Department");
        p.setCity("Baltimore");
        p.setState("Maryland");
        p.setInstCompany("Dell Institution/Company");
        p.setInstCompanyWebsite("www.DellInstitutionCompany.com");
        p.setRegistrationStatus(RegistrationStatus.APPROVED);
        p.setRoles(Arrays.asList(Role.ROLE_USER));
        p.setExternalStorageLinked(false);
        p.setDropboxAppKey("SomeKey");
        p.setDropboxAppSecret("SomeSecret");

        personDao.insertPerson(p);

        // Just looking to not get any exceptions here, and see that
        // all types are handled.
        final StringBuilder tableDump =
                DaoSupport.dumpTable(template, "PERSON");
        assertNotNull(tableDump);
        assertFalse(tableDump.toString().contains("Unhandled SQL type"));
    }

    @Test
    public void testSimpleProject() throws Exception {
        Project p = new Project();
        p.setDescription(this.getClass().getName());
        p.setStartDate(DateTime.now());
        p.setEndDate(DateTime.now());
        p.setFundingEntity("Foo Funding Entity");
        p.setId("foo project id");
        p.setName("foo project name");
        p.setPublisher("blubba publisher");
        p.setStorageAllocated(100L);
        p.setStorageUsed(10L);

        projectDao.insertProject(p);

        // Just looking to not get any exceptions here, and see that
        // all types are handled.
        final StringBuilder tableDump =
                DaoSupport.dumpTable(template, "PROJECT");
        assertNotNull(tableDump);
        assertFalse(tableDump.toString().contains("Unhandled SQL type"));
    }

}
