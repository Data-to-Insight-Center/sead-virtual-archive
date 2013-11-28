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

package org.dataconservancy.ui.services;

import org.dataconservancy.ui.dao.PasswordResetRequestDAO;
import org.dataconservancy.ui.exceptions.PasswordResetServiceException;
import org.dataconservancy.ui.model.PasswordResetRequest;
import org.dataconservancy.ui.model.Person;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;


import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the creation and management of PasswordResetRequests in the PasswordResetService
 */
public class PasswordResetServiceImplTest extends BaseUnitTest {

    @Autowired
    private PasswordResetRequestDAO passwordResetRequestDAO;
    
    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate template;
    
    private PasswordResetService underTest;
    
    static final String PASSWORD_RESET_TABLE = "PASSWORD_RESET";

    static final String PASSWORD_RESET_TABLE_ROW_COUNT_QUERY =
            "SELECT count(*) FROM " + PASSWORD_RESET_TABLE;

    static final String PASSWORD_RESET_TABLE_DELETE_ALL_ROWS_QUERY =
            "DELETE FROM " + PASSWORD_RESET_TABLE;
    
    private Integer resetWindow;

    @Before
    public void setUp() throws IOException {
        template.execute(PASSWORD_RESET_TABLE_DELETE_ALL_ROWS_QUERY);
        assertEquals(0, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
        assertEquals(user, userService.get(user.getEmailAddress()));
        underTest = new PasswordResetServiceImpl(passwordResetRequestDAO, userService);
        resetWindow = underTest.getPasswordResetRequestWindow();
    }

    @Test
    public void testCreateRequestForInvalidUserReturnsNull() throws PasswordResetServiceException {
        Person badUser = new Person();
        badUser.setEmailAddress("bogus@nowhere.edu");
        badUser.setId("id:badUserid");
        badUser.setFirstNames("Marsha");
        badUser.setLastNames("Larts");
        assertNull(underTest.create(badUser));
    }
    
    @Test
    public void testCreateRequestForValidUser() throws PasswordResetServiceException {
        PasswordResetRequest passwordResetRequest = underTest.create(user);
        assertNotNull(passwordResetRequest.getRequestDate());
        assertFalse(passwordResetRequest.getRequestDate().isAfterNow());
        assertEquals(user.getEmailAddress(), passwordResetRequest.getUserEmailAddress());
        assertNotNull(passwordResetRequest.getId());
        assertEquals(36, passwordResetRequest.getId().length());
    }
    
    @Test
    public void testRemoveRequest(){
        PasswordResetRequest request = new PasswordResetRequest("requestId", DateTime.now(), user.getEmailAddress());
        passwordResetRequestDAO.add(request);
        assertEquals(request, passwordResetRequestDAO.get("requestId"));
        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));

        underTest.remove("requestId");

        assertEquals(0, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
    }

    @Test
    public void testRemoveExpiredRequestLeavesActiveRequest(){
        PasswordResetRequest request = new PasswordResetRequest("requestId", DateTime.now().minusHours(resetWindow - 1), user.getEmailAddress());
        passwordResetRequestDAO.add(request);
        assertEquals(request, passwordResetRequestDAO.get("requestId"));
        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));

        underTest.removeExpiredRequests();

        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
    }

    @Test
    public void testRemoveExpiredRequestDeletesExpiredRequest(){
        PasswordResetRequest request = new PasswordResetRequest("requestId", DateTime.now().minusHours(resetWindow + 1), user.getEmailAddress());
        passwordResetRequestDAO.add(request);
        assertEquals(request, passwordResetRequestDAO.get("requestId"));
        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));

        underTest.removeExpiredRequests();

        assertEquals(0, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
    }
    
    @Test
    public void testRetrieveActiveRequestForUser(){
        PasswordResetRequest request = new PasswordResetRequest("requestId", DateTime.now().minusHours(resetWindow - 1), user.getEmailAddress());
        passwordResetRequestDAO.add(request);
        assertEquals(request, passwordResetRequestDAO.get("requestId"));
        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
        
        PasswordResetRequest prr = underTest.getActiveRequest(request.getId());
        
        assertNotNull(prr);

        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
    }

    @Test
    public void testRetrieveInactiveRequestForUserReturnsNull(){
        PasswordResetRequest request = new PasswordResetRequest("requestId", DateTime.now().minusHours(resetWindow + 1), user.getEmailAddress());
        passwordResetRequestDAO.add(request);
        assertEquals(request, passwordResetRequestDAO.get("requestId"));
        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));

        PasswordResetRequest prr = underTest.getActiveRequest(request.getId());

        assertNull(prr);

        assertEquals(1, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
    }

    @Test
     public void testRetrieveExRequestForUserReturnsNull(){
        PasswordResetRequest request = new PasswordResetRequest("badRequestI", DateTime.now(), user.getEmailAddress());
        //don't put in via dao
        PasswordResetRequest prr = underTest.getActiveRequest(request.getId());

        assertNull(prr);

        assertEquals(0, template.queryForInt(PASSWORD_RESET_TABLE_ROW_COUNT_QUERY));
    }
}
