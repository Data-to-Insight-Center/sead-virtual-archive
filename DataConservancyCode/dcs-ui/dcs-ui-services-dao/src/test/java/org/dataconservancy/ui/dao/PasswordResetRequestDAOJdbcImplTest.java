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

package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.PasswordResetRequest;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Testing that the PasswordResetRequestDAOJdbcImpl handles PasswordResetRequest Objects correctly.
 *
 */
public class PasswordResetRequestDAOJdbcImplTest extends BaseDaoTest{

    private static final String TABLE = "PASSWORD_RESET";

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate template;

    private PasswordResetRequestDAO passwordResetRequestDao;

    @Before
    public void setUp() {
        template.execute("DELETE FROM " + TABLE);
        passwordResetRequestDao = new PasswordResetRequestDAOJdbcImpl(template);
    }

    @After
    public void tearDown() {
        template.execute("DELETE FROM " + TABLE);
    }

    @Test
    public void testAdd() {
        PasswordResetRequest prr = new PasswordResetRequest();
        prr.setId("id:cow");
        prr.setRequestDate(DateTime.now());
        prr.setUserEmailAddress("moo@cow.com");

        passwordResetRequestDao.add(prr);

        String query = "SELECT * FROM " + TABLE;
        List<PasswordResetRequest> result = (List<PasswordResetRequest>) template.query(query, new Object[] {},
                new PasswordResetRequestRowMapper());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(prr));
    }

    @Test
    public void testList() {
        List<PasswordResetRequest> result = passwordResetRequestDao.list();

        assertNotNull(result);
        assertEquals(0, result.size());

        PasswordResetRequest prr = new PasswordResetRequest();
        prr.setId("id:cow");
        prr.setRequestDate(DateTime.now());
        prr.setUserEmailAddress("moo@cow.com");

        passwordResetRequestDao.add(prr);

        result = passwordResetRequestDao.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(prr));
    }

    @Test
    public void testGet() {
        DateTime requestDate = DateTime.now();
        String id = "id:foo";
        String emailAddress = "moo@cow.com";
        PasswordResetRequest expectedPasswordResetRequest = new PasswordResetRequest(id, requestDate, emailAddress);
        template.execute("INSERT INTO " + TABLE + " VALUES ('" + id +"', '" + requestDate.getMillis() +"', '" + emailAddress + "')");
        assertEquals(1, template.queryForInt("SELECT count(*) FROM " + TABLE));

        assertEquals(expectedPasswordResetRequest, passwordResetRequestDao.get(id));
    }

    @Test
    public void testGetNonExistentPasswordResetRequest() {
        assertNull(passwordResetRequestDao.get("non-existent prrcipline id"));
    }

    @Test
    public void testDelete(){
        DateTime requestDate = DateTime.now();
        String id = "id:foo";
        String emailAddress = "moo@cow.com";
        PasswordResetRequest expectedPasswordResetRequest = new PasswordResetRequest(id, requestDate, emailAddress);
        template.execute("INSERT INTO " + TABLE + " VALUES ('" + id +"', '" + requestDate.getMillis() +"', '" + emailAddress + "')");
        assertEquals(1, template.queryForInt("SELECT count(*) FROM " + TABLE));

        passwordResetRequestDao.delete(id);

        assertEquals(0, template.queryForInt("SELECT count(*) FROM " + TABLE));
    }
}
